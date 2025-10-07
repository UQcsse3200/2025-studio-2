package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.LevelAssetsConfig;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.entities.configs.LevelConfig;
import com.csse3200.game.entities.spawn.EntitySpawner;
import com.csse3200.game.entities.spawn.EntitySpawner.Subtype;
import com.csse3200.game.entities.spawn.EntitySpawner.eType;
import com.csse3200.game.entities.spawn.SpawnRegistry;
import com.csse3200.game.entities.spawn.Spawners;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.files.FileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LevelThreeGameArea extends GameArea {
    private static final Logger logger = LoggerFactory.getLogger(LevelThreeGameArea.class);

    private final GridPoint2 MAP_SIZE = new GridPoint2(0, 0);
    private final GridPoint2 PLAYER_SPAWN = new GridPoint2(0, 0);
    private float WALL_THICKNESS;

    private final ResourceService rs = ServiceLocator.getResourceService();
    private final TerrainFactory terrainFactory;

    protected final String cfgFileName = "level-assets/surface-level/config.json";
    protected final String assetsFileName = "level-assets/surface-level/assets.json";

    private LevelConfig cfg;
    private LevelAssetsConfig assets;

    public LevelThreeGameArea(TerrainFactory terrainFactory) {
        super();
        this.terrainFactory = terrainFactory;

    }

    @Override
    protected void loadPrerequisites() {
        cfg = Objects.requireNonNull(FileLoader.readClass(LevelConfig.class, cfgFileName));
        assets = Objects.requireNonNull(FileLoader.readClass(LevelAssetsConfig.class, assetsFileName));

        WALL_THICKNESS = cfg.walls.thickness;
        MAP_SIZE.set(cfg.mapSize[0], cfg.mapSize[1]);
        PLAYER_SPAWN.set(cfg.playerSpawn[0], cfg.playerSpawn[1]);

        displayUI(cfg.name);
        spawnTerrain();
        createMinimap(ServiceLocator.getResourceService().getAsset(cfg.miniMap, Texture.class));
        //        spawnParallaxBackground();
    }

    @Override
    protected void loadAssets() {
        assets = Objects.requireNonNull(FileLoader.readClass(LevelAssetsConfig.class, assetsFileName));

        rs.loadTextures(assets.textures.toArray(new String[0]));
        rs.loadTextureAtlases(assets.atlases.toArray(new String[0]));
        rs.loadSounds(assets.sounds.toArray(new String[0]));

        while (!rs.loadForMillis(10)) {
            logger.info("Loading level assets... {}%", rs.getProgress());
        }
    }

    @Override
    protected void loadEntities() {
        if (cfg == null) throw new IllegalStateException("Level config not loaded");
        if (cfg.entities == null) throw new IllegalArgumentException("'entities' missing in level config");

        for (var e : cfg.entities) {
            var args = new EntitySpawner.Args();

            args.type = eType.fromString(e.type);
            args.subtype = (e.subtype == null) ? null : Subtype.fromString(e.subtype);
            Subtype.validateMatch(args.type, args.subtype);

            args.id = e.id;
            args.tooltip = e.tooltip;
            args.linked = e.linked;
            args.target = e.target;
            args.extra = e.extra;
            args.x = e.x;
            args.y = e.y;
            args.sx = e.sx;
            args.sy = e.sy;
            args.speed = e.speed;
            args.rotation = e.rotation;
            args.dx = e.dx;
            args.dy = e .dy;
            args.linked = e.linked;

            var entity = SpawnRegistry.build(args.type.toString(), args);

            var centerX = e.centerX == null || e.centerX;
            var centerY = e.centerY == null || e.centerY;

            spawnEntityAt(entity, new GridPoint2(args.x, args.y), centerX, centerY);
        }
    }

    @Override
    protected Entity spawnPlayer() {
        Entity newPlayer = PlayerFactory.createPlayer(new ArrayList<>());
        spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
        newPlayer.getEvents().addListener("reset", this::reset);
        Spawners.registerAll(newPlayer, this);
        return newPlayer;
    }

    @Override
    protected Entity spawnPlayer(java.util.List<Component> componentList) {
        Entity newPlayer = PlayerFactory.createPlayer(componentList);
        spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
        newPlayer.getEvents().addListener("reset", this::reset);
        Spawners.registerAll(newPlayer, this);
        return newPlayer;
    }

    // --- Helpers ---

    private void displayUI(String name) {
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay(name));
        ui.addComponent(new TooltipSystem.TooltipDisplay());
        spawnEntity(ui);
    }

    private void spawnTerrain() {
        terrain = createSurfaceTerrain();
        spawnEntity(new Entity().addComponent(terrain));

        // Terrain walls from map bounds
        float tileSize = terrain.getTileSize();
        GridPoint2 tileBounds = terrain.getMapBounds(0);
        Vector2 worldBounds = new Vector2(tileBounds.x * tileSize, tileBounds.y * tileSize);

        // Left
        spawnEntityAt(
                ObstacleFactory.createWall(WALL_THICKNESS, worldBounds.y),
                GridPoint2Utils.ZERO, false, false
        );
        // Right
        spawnEntityAt(
                ObstacleFactory.createWall(WALL_THICKNESS, worldBounds.y),
                new GridPoint2(tileBounds.x, 0), false, false
        );
        // Top
        spawnEntityAt(
                ObstacleFactory.createWall(worldBounds.x, WALL_THICKNESS),
                new GridPoint2(0, tileBounds.y - 4), false, false
        );
    }

    private TerrainComponent createSurfaceTerrain() {
        TextureRegion emptyTile =
                new TextureRegion(rs.getAsset("images/empty.png", Texture.class));
        GridPoint2 tilePixelSize = new GridPoint2(
                emptyTile.getRegionWidth(), emptyTile.getRegionHeight()
        );
        TiledMap tiledMap = terrainFactory.createDefaultTiles(
                tilePixelSize, emptyTile, emptyTile, emptyTile, emptyTile, MAP_SIZE
        );
        return terrainFactory.createInvisibleFromTileMap(0.5f, tiledMap, tilePixelSize);
    }

//    private void spawnParallaxBackground() {
//        Entity background = ParallaxFactory.createParallax(
//                "configs/surface_parallax.json",
//                ServiceLocator.getRenderService().getRenderer().getCamera().getCamera(),
//                MAP_SIZE
//        );
//        spawnEntity(background);
//    }
}
