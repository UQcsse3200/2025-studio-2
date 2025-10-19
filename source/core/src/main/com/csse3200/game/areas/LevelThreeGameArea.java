package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.achievements.AchievementProgression;
import com.csse3200.game.areas.terrain.GridFactory;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.LevelAssetsConfig;
import com.csse3200.game.entities.configs.LevelConfig;
import com.csse3200.game.entities.factories.FloorFactory;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.entities.spawn.SpawnRegistry;
import com.csse3200.game.entities.spawn.Spawners;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.rendering.parallax.ParallaxBackgroundComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.achievements.AchievementToastUI;
import com.csse3200.game.ui.achievements.AchievementsMenuUI;
import com.csse3200.game.utils.math.GridPoint2Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Game area implementation for Level Three (Surface level).
 * <p>
 * Loads level configuration and asset manifests from JSON, initialises game grid and
 * world boundaries, spawns entities via {@link SpawnRegistry}, and creates the player.
 * </p>
 */
public class LevelThreeGameArea extends GameArea {
    private static final Logger logger = LoggerFactory.getLogger(LevelThreeGameArea.class);

    private final GridPoint2 MAP_SIZE = new GridPoint2(0, 0);
    private final GridPoint2 PLAYER_SPAWN = new GridPoint2(0, 0);
    private float WALL_THICKNESS;

    private final ResourceService rs = ServiceLocator.getResourceService();
    private final GridFactory gridFactory;

    protected final String cfgFileName = "level-assets/surface-level/config.json";
    protected final String assetsFileName = "level-assets/surface-level/assets.json";

    private LevelConfig cfg;
    private LevelAssetsConfig assets;

    /**
     * Creates a Level Three game area.
     *
     * @param gridFactory factory used to build world grid
     */
    public LevelThreeGameArea(GridFactory gridFactory) {
        super();
        this.gridFactory = gridFactory;

    }

    /**
     * Loads config files, derives level constants (map size, spawn, wall thickness),
     * sets up UI/minimap, and spawns grid.
     */
    @Override
    protected void loadPrerequisites() {
        cfg = Objects.requireNonNull(FileLoader.readClass(LevelConfig.class, cfgFileName));
        assets = Objects.requireNonNull(FileLoader.readClass(LevelAssetsConfig.class, assetsFileName));

        WALL_THICKNESS = cfg.walls.thickness;
        MAP_SIZE.set(cfg.mapSize[0], cfg.mapSize[1]);
        PLAYER_SPAWN.set(cfg.playerSpawn[0], cfg.playerSpawn[1]);

        displayUI(cfg.name);
        spawnGrid();
        createMinimap(ServiceLocator.getResourceService().getAsset(cfg.miniMap, Texture.class));
        playMusic();
        AchievementProgression.onLevelStart();
    }

    private void spawnGrid() {
        grid = gridFactory.createGrid(MAP_SIZE, 0.5f);
        spawnEntity(new Entity().addComponent(grid));

        // Grid walls
        float tileSize = grid.getTileSize();
        GridPoint2 tileBounds = grid.getMapBounds();
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
     * Loads textures, atlases, and sounds declared in the asset json.
     */
    @Override
    protected void loadAssets() {
        assets = Objects.requireNonNull(FileLoader.readClass(LevelAssetsConfig.class, assetsFileName));

        rs.loadTextures(assets.textures.toArray(new String[0]));
        rs.loadTextureAtlases(assets.atlases.toArray(new String[0]));
        rs.loadSounds(assets.sounds.toArray(new String[0]));
        rs.loadMusic(assets.music.toArray(new String[0]));

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
        ui.addComponent(new AchievementToastUI());
        ui.addComponent(new AchievementsMenuUI());
        spawnEntity(ui);
    }

    private void spawnParallaxBackground() {
        Entity background = new Entity();

        Camera gameCamera = ServiceLocator.getRenderService()
            .getRenderer().getCamera().getCamera();

        Vector2 worldBounds = grid.getWorldBounds();
        ParallaxBackgroundComponent parallaxBg =
            new ParallaxBackgroundComponent(gameCamera, worldBounds.x, worldBounds.y);

        ResourceService rs = ServiceLocator.getResourceService();

        String[] paths = {
            "images/surfacelevel/background/1.png", // sky
            "images/surfacelevel/background/2.png", // distant skyline
            "images/surfacelevel/background/3.png", // mid skyline
            "images/surfacelevel/background/4.png"  // near skyline
        };

        float[] factors   = {0.0f, 0.2f, 0.4f, 0.6f};
        float[] offsetYs  = {2.0f, 1.5f, 1.0f, 0.5f};
        float[] scaleMods = {0.9f, 1.0f, 1.05f, 1.1f};

        final float offsetX = 0f;
        float viewportW = gameCamera.viewportWidth;
        float viewportH = gameCamera.viewportHeight;

        for (int i = 0; i < paths.length; i++) {
            Texture tex = rs.getAsset(paths[i], Texture.class);
            if (tex == null) {
                logger.warn("Parallax texture missing: {}", paths[i]);
                continue;
            }

            tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            tex.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

            float baseScaleX = viewportW / tex.getWidth();
            float baseScaleY = viewportH / tex.getHeight();
            float scale = Math.max(baseScaleX, baseScaleY) * scaleMods[i];

            parallaxBg.addScaledLayer(tex, factors[i], offsetX, offsetYs[i], scale);
        }

        background.addComponent(parallaxBg);
        spawnEntity(background);
    }

    private void unloadAssets() {
        logger.debug("Unloading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.unloadAssets(assets.atlases.toArray(new String[0]));
        resourceService.unloadAssets(assets.sounds.toArray(new String[0]));
        resourceService.unloadAssets(assets.textures.toArray(new String[0]));
        resourceService.unloadAssets(assets.music.toArray(new String[0]));
    }
    @Override
    public void dispose() {
        isResetting = true;
        super.dispose();
        this.unloadAssets();
        isResetting = false;
    }

    private void playMusic() {
        String track = !assets.music.isEmpty() ? assets.music.getFirst() : cfg.music;
        Music music = ServiceLocator.getResourceService().getAsset(track, Music.class);
        if (music == null) {
            logger.error("Music not loaded: {}", track);
            return;
        }
        music.setLooping(true);
        music.setVolume(UserSettings.getMusicVolumeNormalized());
        music.play();
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
