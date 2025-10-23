package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.achievements.AchievementProgression;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.LevelAssetsConfig;
import com.csse3200.game.entities.configs.LevelConfig;
import com.csse3200.game.entities.configs.ParallaxConfig;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.entities.spawn.SpawnRegistry;
import com.csse3200.game.entities.spawn.Spawners;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.rendering.parallax.ParallaxBackgroundComponent;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base game area for standardised JSON-driven levels. This class is responsible for:
 *
 ** <ol>
 *   <li>Loading asset files ({@link LevelAssetsConfig}) and level config ({@link LevelConfig}),</li>
 *   <li>Building terrain and world boundaries,</li>
 *   <li>Spawning HUD/minimap/music,</li>
 *   <li>Spawning entities via {@link SpawnRegistry} and creating the player,</li>
 *   <li>Constructing parallax background from {@link ParallaxConfig}.</li>
 * </ol>
 *
 * <p>Concrete levels provide file paths via {@link #configPath()}, {@link #assetsPath()} ()} and
 * {@link #parallaxPath()}
 */
public abstract class BaseLevelGameArea extends GameArea {
    private static final Logger logger = LoggerFactory.getLogger(BaseLevelGameArea.class);

    protected final TerrainFactory terrainFactory;
    protected final ResourceService rs = ServiceLocator.getResourceService();

    protected LevelConfig cfg;
    protected LevelAssetsConfig assets;
    protected ParallaxConfig parallax;

    protected final GridPoint2 MAP_SIZE = new GridPoint2();
    protected final GridPoint2 PLAYER_SPAWN = new GridPoint2();
    protected float WALL_THICKNESS;

    boolean hasLaser = false;

    /**
     * Create a new base level area.
     *
     * @param tf terrain factory used to construct tile maps and collision
     */
    protected BaseLevelGameArea(TerrainFactory tf) {
        this.terrainFactory = tf;
    }

    protected abstract String configPath();
    protected abstract String assetsPath();
    protected abstract String parallaxPath();

    /**
     * Load all assets declared in {@link LevelAssetsConfig}.
     */
    @Override protected void loadAssets() {
        assets = Objects.requireNonNull(FileLoader.readClass(LevelAssetsConfig.class, assetsPath()));

        rs.loadTextures(assets.textures.toArray(new String[0]));
        rs.loadTextureAtlases(assets.atlases.toArray(new String[0]));
        rs.loadSounds(assets.sounds.toArray(new String[0]));
        rs.loadMusic(assets.music.toArray(new String[0]));

        while (!rs.loadForMillis(10)) {
            logger.info("Loading level assets... {}%", rs.getProgress());
        }
    }

    /**
     * Read level configs, construct terrain, spawn walls/HUD/minimap, and start music.
     *
     * @throws NullPointerException if any required JSON cannot be loaded
     */
    @Override
    protected void loadPrerequisites() {
        cfg = Objects.requireNonNull(FileLoader.readClass(LevelConfig.class, configPath()));
        assets = Objects.requireNonNull(FileLoader.readClass(LevelAssetsConfig.class, assetsPath()));
        parallax  = Objects.requireNonNull(FileLoader.readClass(ParallaxConfig.class, parallaxPath()));

        WALL_THICKNESS = cfg.walls.thickness;
        MAP_SIZE.set(cfg.mapSize[0], cfg.mapSize[1]);
        PLAYER_SPAWN.set(cfg.playerSpawn[0], cfg.playerSpawn[1]);

        this.terrain = buildTerrain(MAP_SIZE);
        spawnEntity(new Entity().addComponent(this.terrain));
        spawnBoundaryWalls();

        spawnEntity(HeadsUpDisplayFactory.createHeadsUpDisplay(cfg.name));
        Texture mini = rs.getAsset(cfg.miniMap, Texture.class);
        createMinimap(mini);

        playMusic();
        AchievementProgression.onLevelStart();
    }

    /**
     * Spawn parallax (if configured) and all entities declared in {@link LevelConfig#entities},
     * then add a ground floor.
     *
     * @throws IllegalStateException if {@link #cfg} has not been loaded
     * @throws IllegalArgumentException if the config has no {@code entities} array
     */
    @Override protected void loadEntities() {
        if (cfg == null) throw new IllegalStateException("Level config not loaded");
        if (cfg.entities == null) throw new IllegalArgumentException("'entities' missing in level config");
        buildParallax();

        for (var e : cfg.entities) {
            var entity = SpawnRegistry.build(e.type, e);
            boolean cx = e.centerX == null || e.centerX;
            boolean cy = e.centerY == null || e.centerY;
            spawnEntityAt(entity, new GridPoint2(e.x, e.y), cx, cy);
        }

        Entity floor = FloorFactory.createGroundFloor();
        spawnEntityAt(floor, new GridPoint2(-10, -20), false, false);
    }

    /**
     * Create a new player with default components and register level spawners.
     *
     * @return the newly created player entity
     */
    @Override protected Entity spawnPlayer() {
        Entity player = PlayerFactory.createPlayer(new ArrayList<>());
        setUpPlayer(player);
        return player;
    }

    /**
     * Create a new player using provided components and register level spawners.
     *
     * @param components list of components to attach to the new player (e.g., inventory)
     * @return the newly created player entity
     */
    @Override protected Entity spawnPlayer(List<Component> components) {
        Entity player = PlayerFactory.createPlayer(components);
        setUpPlayer(player);
        return player;
    }

    /**
     * Dispose of area resources and unload all assets declared in the asset manifest.
     *
     * <p>Calls {@link GameArea#dispose()} to dispose entities, then unloads textures/atlases/sounds/music</p>
     */
    @Override public void dispose() {
        super.dispose();
        if (assets != null) {
            rs.unloadAssets(assets.atlases.toArray(new String[0]));
            rs.unloadAssets(assets.sounds.toArray(new String[0]));
            rs.unloadAssets(assets.textures.toArray(new String[0]));
            rs.unloadAssets(assets.music.toArray(new String[0]));
        }
    }

    /* --- Helpers --- */

    /**
     * Set player placement and wire:
     * position at {@link #PLAYER_SPAWN}, subscribe to {@code "reset"}, and register spawners.
     *
     * @param player player entity to configure
     */
    private void setUpPlayer(Entity player) {
        spawnEntityAt(player, PLAYER_SPAWN, true, true);
        player.getEvents().addListener("reset", this::reset);
        Spawners.registerAll(player, this);
        this.player = player;
    }

    /**
     * Build the basic terrain.
     *
     * <p>Default implementation creates an invisible collision terrain using a placeholder tile
     * sourced from {@code images/empty.png}.</p>
     *
     * @param mapSize desired map size in tiles (width, height)
     * @return constructed terrain component ready to be spawned
     */
    protected TerrainComponent buildTerrain(GridPoint2 mapSize) {
        TextureRegion empty = new TextureRegion(rs.getAsset("images/empty.png", Texture.class));
        GridPoint2 tilePx = new GridPoint2(empty.getRegionWidth(), empty.getRegionHeight());
        TiledMap map = terrainFactory.createDefaultTiles(tilePx, empty, empty, empty, empty, mapSize);
        return terrainFactory.createInvisibleFromTileMap(0.5f, map, tilePx);
    }

    /**
     * Spawn world-boundary walls (left, right, top) derived from current {@link #terrain} bounds.
     *
     * <p>Assumes {@link #terrain} has already been assigned and spawned lol.</p>
     */
    private void spawnBoundaryWalls() {
        float tile = terrain.getTileSize();
        GridPoint2 bounds = terrain.getMapBounds(0);
        Vector2 world = new Vector2(bounds.x * tile, bounds.y * tile);

        spawnEntityAt(ObstacleFactory.createWall(WALL_THICKNESS, world.y), new GridPoint2(0, 0), false, false);
        spawnEntityAt(ObstacleFactory.createWall(WALL_THICKNESS, world.y), new GridPoint2(bounds.x, 0), false, false);
        spawnEntityAt(ObstacleFactory.createWall(world.x, WALL_THICKNESS), new GridPoint2(0, bounds.y - 4), false, false);
    }

    /**
     * Start background music for the level.
     */
    private void playMusic() {
        String track = (assets != null && !assets.music.isEmpty()) ? assets.music.getFirst() : cfg.music;

        if (track == null) return;
        Music music = rs.getAsset(track, Music.class);

        if (music == null) return;
        music.setLooping(true);
        music.setVolume(UserSettings.getMusicVolumeNormalized());
        music.play();
    }

    /**
     * Build a parallax background from {@link ParallaxConfig}, if provided.
     *
     * <p>Each layer is scaled to at least fill the current camera viewport, then
     * multiplied by the per-layer {@code scale}. Missing or unloaded textures are skipped.</p>
     */
    protected void buildParallax() {
        if (parallax == null || parallax.layers.isEmpty()) return;

        Camera cam = ServiceLocator.getRenderService().getRenderer().getCamera().getCamera();
        GridPoint2 mapBounds = terrain.getMapBounds(0);
        float ts = terrain.getTileSize();
        float worldW = mapBounds.x * ts, worldH = mapBounds.y * ts;

        ParallaxBackgroundComponent bg = new ParallaxBackgroundComponent(cam, worldW, worldH);
        float vw = cam.viewportWidth;
        float vh = cam.viewportHeight;

        for (ParallaxConfig.Layer layer : parallax.layers) {
            Texture tex = rs.getAsset(layer.texture, Texture.class);
            if (tex == null) continue;
            tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            tex.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

            float fit = Math.max(vw / tex.getWidth(), vh / tex.getHeight());
            float scale = fit * (layer.scale <= 0f ? 1f : layer.scale);

            System.out.println("SCALE: " + scale);

            if (layer.tiled) {
                bg.addTiledLayer(tex, layer.factor, true, true, 10f, 10f, layer.offsetX, layer.offsetY);
            } else {
                bg.addScaledLayer(tex, layer.factor, layer.offsetX, layer.offsetY, scale);
            }
        }

        spawnEntity(new Entity().addComponent(bg));
    }

    /**
     * Spawns in the laser shower on the level based on the players position.
     * <p> This function is called within {@link MainGameScreen} where it determines what
     * level it is on and the frequency at which to spawn the lasers.</p>
     */
    public void spawnLaserShower(float x , float y) {
        if (player == null) return; // safety check

        // Spawn lasers behind of the player
        for (int i = 0; i <= 5; i++) {
            Entity laser = LaserFactory.createLaserShower(-90f); // Create another downward laser
            float xBehind = x - ((i + 1) * 7.5f); // offset left
            spawnEntityAt(laser, new GridPoint2(Math.round(xBehind+10f), Math.round(y+15f)), true, true);
            laser.getEvents().trigger("shootLaser");

            // Remove laser after 5 seconds
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    laser.dispose();
                }
            }, 5f);
        }

        // Spawn lasers ahead of the player
        for (int i = 0; i <= 5; i++) {
            Entity laser = LaserFactory.createLaserShower(-90f); // Create another downward laser
            float xAhead = x + ((i + 1) * 7.5f); // offset right
            spawnEntityAt(laser, new GridPoint2(Math.round(xAhead+10f), Math.round(y+15f)), true, true);
            laser.getEvents().trigger("shootLaser");

            // Schedule disposal after 5 seconds
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    laser.dispose();
                }
            }, 5f);
        }
    }

    public void laserShowerChecker(float x , float y) {
        if (!hasLaser) { // Only spawn if no active laser
            spawnLaserShower(x,y);
            hasLaser = true; // Mark laser as active
            // Reset the has_laser flag after 5 seconds to allow next spawn
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    hasLaser = false;
                }
            },5f);
        }
    }
}