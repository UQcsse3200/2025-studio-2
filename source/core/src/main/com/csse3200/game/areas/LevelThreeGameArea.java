package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Camera;
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
import com.csse3200.game.entities.spawn.SpawnRegistry;
import com.csse3200.game.entities.spawn.Spawners;
import com.csse3200.game.rendering.parallax.ParallaxBackgroundComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.files.FileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Game area implementation for Level Three (Surface level).
 * <p>
 * Loads level configuration and asset manifests from JSON, initialises terrain and
 * world boundaries, spawns entities via {@link SpawnRegistry}, and creates the player.
 * </p>
 */
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

    /**
     * Creates a Level Three game area.
     *
     * @param terrainFactory factory used to build tile maps and terrain components
     */
    public LevelThreeGameArea(TerrainFactory terrainFactory) {
        super();
        this.terrainFactory = terrainFactory;

    }

    /**
     * Loads config files, derives level constants (map size, spawn, wall thickness),
     * sets up UI/minimap, and spawns terrain.
     */
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

    /**
     * Loads textures, atlases, and sounds declared in the asset json.
     */
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

    /**
     * Spawns all entities defined in {@link LevelConfig#entities} via {@link SpawnRegistry},
     * respecting per-entity centering flags, and adds a base ground floor.
     *
     * @throws IllegalStateException    if the level config hasn't been loaded
     * @throws IllegalArgumentException if the config is missing the {@code entities} array
     */
    @Override
    protected void loadEntities() {
        if (cfg == null) throw new IllegalStateException("Level config not loaded");
        if (cfg.entities == null) throw new IllegalArgumentException("'entities' missing in level config");
        spawnParallaxBackground();

        for (var e : cfg.entities) {
            var entity = SpawnRegistry.build(e.type, e);
            var centerX = e.centerX == null || e.centerX;
            var centerY = e.centerY == null || e.centerY;
            spawnEntityAt(entity, new GridPoint2(e.x, e.y), centerX, centerY);
        }

        Entity floor = FloorFactory.createGroundFloor();
        spawnEntityAt(floor, new GridPoint2(-10, -20), false, false);
    }

    /**
     * Creates and spawns the default player at {@link #PLAYER_SPAWN}
     *
     * @return the created player entity
     */
    @Override
    protected Entity spawnPlayer() {
        Entity newPlayer = PlayerFactory.createPlayer(new ArrayList<>());
        spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
        newPlayer.getEvents().addListener("reset", this::reset);
        Spawners.registerAll(newPlayer, this);
        return newPlayer;
    }

    /**
     * Creates and spawns the player with a given component list,
     * and registers level spawners.
     *
     * @param componentList components to add to the player
     * @return the created player entity
     */
    @Override
    protected Entity spawnPlayer(java.util.List<Component> componentList) {
        Entity newPlayer = PlayerFactory.createPlayer(componentList);
        spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
        newPlayer.getEvents().addListener("reset", this::reset);
        Spawners.registerAll(newPlayer, this);
        return newPlayer;
    }

    // --- Helpers ---

    /**
     * Spawns HUD for this level.
     *
     * @param name level name to display
     */
    private void displayUI(String name) {
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay(name));
        ui.addComponent(new TooltipSystem.TooltipDisplay());
        spawnEntity(ui);
    }

    private void spawnParallaxBackground() {
        Entity background = new Entity();

        Camera gameCamera = ServiceLocator.getRenderService().getRenderer().getCamera().getCamera();

        // Get map dimensions from terrain
        GridPoint2 mapBounds = terrain.getMapBounds(0);
        float tileSize = terrain.getTileSize();
        float mapWorldWidth = mapBounds.x * tileSize;
        float mapWorldHeight = mapBounds.y * tileSize;

        ParallaxBackgroundComponent parallaxBg = new ParallaxBackgroundComponent(gameCamera, mapWorldWidth, mapWorldHeight);

        ResourceService rs = ServiceLocator.getResourceService();

        final float scale = 0.08f;
        final float offsetX = 0;
        final float offsetY = -11;

        // layer 1 (furthest away)
        Texture layer1 = rs.getAsset("images/surfacelevel/surface-1.png", Texture.class);
        parallaxBg.addScaledLayer(layer1, 0f, offsetX, offsetY, scale);

        // layer 2
        Texture layer2 =  rs.getAsset("images/surfacelevel/surface-2.png", Texture.class);
        parallaxBg.addScaledLayer(layer2, 0.2f, offsetX, offsetY, scale);

        // layer3
        Texture layer3 =  rs.getAsset("images/surfacelevel/surface-3.png", Texture.class);
        parallaxBg.addScaledLayer(layer3, 0.4f, offsetX, offsetY, scale);

        // layer4
        Texture layer4 =  rs.getAsset("images/surfacelevel/surface-4.png", Texture.class);
        parallaxBg.addScaledLayer(layer4, 0.6f, offsetX, offsetY, scale);

        // layer5
        Texture layer5 =  rs.getAsset("images/surfacelevel/surface-5.png", Texture.class);
        parallaxBg.addScaledLayer(layer5, 0.8f, offsetX, offsetY, scale);

        background.addComponent(parallaxBg);
        spawnEntity(background);
    }

    /**
     * Builds the underlying terrain and spawns world-boundary walls
     * using the current tile map bounds.
     */
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

    /**
     * Creates an invisible collision terrain from a generated tile map
     * using a placeholder tile for layout.
     *
     * @return the constructed {@link TerrainComponent}
     */
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

    private void unloadAssets() {
        logger.debug("Unloading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.unloadAssets(assets.atlases.toArray(new String[0]));
        resourceService.unloadAssets(assets.sounds.toArray(new String[0]));
        resourceService.unloadAssets(assets.textures.toArray(new String[0]));
    }
    @Override
    public void dispose() {
        isResetting = true;
        super.dispose();
        this.unloadAssets();
        isResetting = false;
    }

//    /**
//     * Spawns a parallax background defined by an external config,
//     * bound to the main camera and scaled to {@link #MAP_SIZE}.
//     */
//    private void spawnParallaxBackground() {
//        Entity background = ParallaxFactory.createParallax(
//                "configs/surface_parallax.json",
//                ServiceLocator.getRenderService().getRenderer().getCamera().getCamera(),
//                MAP_SIZE
//        );
//        spawnEntity(background);
//    }
}
