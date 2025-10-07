package com.csse3200.game.areas;

import com.badlogic.gdx.Gdx;
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
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.parallax.ParallaxBackgroundComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.GridPoint2Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BossLevelGameArea extends GameArea {
    private static final GridPoint2 mapSize = new GridPoint2(80,57);
    private static final float WALL_THICKNESS = 0.1f;
    private static GridPoint2 PLAYER_SPAWN;
    private static boolean keySpawned;
    private static final String[] gameTextures = {
            "images/button.png",
            "images/key.png",
            "images/button_pushed.png",
            "images/blue_button.png",
            "images/blue_button_pushed.png",
            "images/red_button.png",
            "images/red_button_pushed.png",
            "images/box_orange.png",
            "images/minimap_player_marker.png",
            "images/minimap_forest_area.png",
            "images/box_blue.png",
            "images/box_white.png",
            "images/blue_button.png",
            "images/spikes_sprite.png",
            "images/TechWallBase.png",
            "images/TechWallVariant1.png",
            "images/TechWallVariant2.png",
            "images/TechWallVariant3.png",
            "images/platform.png",
            "images/empty.png",
            "images/gate.png",
            "images/door_open.png",
            "images/door_closed.png",
            "images/Gate_open.png",
            "images/button.png",
            "images/button_pushed.png",
            "images/blue_button_pushed.png",
            "images/blue_button.png",
            "images/drone.png",
            "images/boss.png",
            "images/bomb.png",
            "images/camera-body.png",
            "images/camera-lens.png",
            "images/glide_powerup.png",
            "images/wall.png",
            "images/dash_powerup.png",
            "images/ladder.png",
            "images/ladder-base.png",
            "images/cavelevel/tile000.png",
            "images/cavelevel/tile001.png",
            "images/cavelevel/tile002.png",
            "images/cavelevel/tile014.png",
            "images/cavelevel/tile015.png",
            "images/cavelevel/tile016.png",
            "images/cavelevel/tile028.png",
            "images/cavelevel/tile029.png",
            "images/cavelevel/tile030.png",
            "images/cavelevel/background/1.png",
            "images/cavelevel/background/2.png",
            "images/cavelevel/background/3.png",
            "images/cavelevel/background/4.png",
            "images/cavelevel/background/5.png",
            "images/cavelevel/background/6.png",
            "images/cavelevel/background/7.png",
            "images/pressure_plate_unpressed.png",
            "images/pressure_plate_pressed.png",
            "images/mirror-cube-off.png",
            "images/mirror-cube-on.png",
            "images/boss.png"
    };
    private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";
    private static final String[] musics = {backgroundMusic};
    private static final String[] gameSounds = {"sounds/Impact4.ogg",
            "sounds/chimesound.mp3"};
    private static final String[] gameTextureAtlases = {
            "images/PLAYER.atlas",
            "images/drone.atlas",
            "images/boss.atlas",
            "images/volatile_platform.atlas",
            "images/health-potion.atlas",
            "images/speed-potion.atlas",
            "images/flying_bat.atlas", // Bat sprites from https://todemann.itch.io/bat (see Wiki)
            "images/doors.atlas",
            "images/laser.atlas"
    };
    private static final Logger logger = LoggerFactory.getLogger(BossLevelGameArea.class);
    private final TerrainFactory terrainFactory;

    public BossLevelGameArea(TerrainFactory terrainFactory) {
        super();
        this.terrainFactory = terrainFactory;
    }
    protected void loadPrerequisites() {
        displayUI();
        spawnTerrain();
        PLAYER_SPAWN  = new GridPoint2(5, tileBounds.y - 5);
        createMinimap(ServiceLocator.getResourceService().getAsset("images/minimap_forest_area.png", Texture.class));
        playMusic();
    }
    protected void loadEntities() {
        spawnParallaxBackground();
        spawnPlatforms();
        spawnStaticObstacles();
        spawnGate();
        spawnObjectives();
        spawnLasers();
//        spawnBoss();
    }

    /**
     * Spawn all lasers used in game
     */
    private void spawnLasers() {
        spawnFirstBlockingLasers();
        spawnLaserPuzzle();
    }

    /**
     * Spawn the first laser(s) that go through into the left half of screen
     */
    private void spawnFirstBlockingLasers() {

    }

    /**
     * Create the laser puzzle room
     */
    private void spawnLaserPuzzle() {

    }

    /**
     * Spawn the gate that goes between the two stages, as well as the wall below it
     */
    private void spawnGate() {
        // Lower wall
        Entity wall = WallFactory.createWall(20,0,1,5f,"");
        wall.setScale(2f,11f);
        spawnEntityAt(wall, new GridPoint2(65, -3),
                false, false);

        // Upper "wall" (gate stand-in)
        Entity gate = WallFactory.createWall(20,tileBounds.y - 40,1,5f,"");
        gate.setScale(2f,17f);
        spawnEntityAt(gate, new GridPoint2(65, tileBounds.y - 35),
                false, false);
    }

    private void spawnStaticObstacles() {
        spawnDeathZone();
        spawnTraps();
        spawnBats();
    }

    private void spawnPlatforms() {
        spawnFirstDrop();
        spawnRedHerringPath();
        spawnButtonPuzzleRoom();
    }

    /**
     * Spawns the platforms (left half of screen, upper path)
     * leading towards the shut gate and evil button.
     */
    private void spawnRedHerringPath() {
        // Jumping along four platforms:
        GridPoint2 firstPos = new GridPoint2(40, tileBounds.y - 27);
        Entity firstPlatform = PlatformFactory.createStaticPlatform();
        firstPlatform.setScale(1, 0.5f);
        spawnEntityAt(firstPlatform, firstPos,false, false);

        GridPoint2 secondPos = new GridPoint2(50, tileBounds.y - 25);
        Entity secondPlatform = PlatformFactory.createVolatilePlatform(1f, 3f);
        secondPlatform.setScale(2, 0.5f);
        spawnEntityAt(secondPlatform, secondPos,false, false);

        GridPoint2 thirdPos = new GridPoint2(62, tileBounds.y - 23);
        Entity thirdPlatform = PlatformFactory.createStaticPlatform();
        thirdPlatform.setScale(1.5f, 0.5f);
        spawnEntityAt(thirdPlatform, thirdPos,false, false);

        GridPoint2 fourthPos = new GridPoint2(57, tileBounds.y - 16);
        Entity fourthPlatform = PlatformFactory.createStaticPlatform();
        fourthPlatform.setScale(1.5f, 0.5f);
        spawnEntityAt(fourthPlatform, fourthPos,false, false);

        spawnEvilMovingPlatform();
    }

    /**
     * Spawns the floor and platforms for the timed-button puzzle room in the first section.
     */
    private void spawnButtonPuzzleRoom() {
        // Spawn floor to act as room's ceiling
        GridPoint2 ceilingPos = new GridPoint2(25, tileBounds.y - 40);
        Entity gateFloor = FloorFactory.createStaticFloor();
        gateFloor.setScale(20f, 1);
        spawnEntityAt(gateFloor, ceilingPos, false, false);
    }

    /**
     * Spawn platforms for the initial drop-down and move forward (first 5 platforms)
     * section of the level.
     */
    private void spawnFirstDrop() {
        // Volatile platform player initially spawns on
        GridPoint2 initialPos = new GridPoint2(3,tileBounds.y - 10);
        Entity initialPlatform = PlatformFactory.createVolatilePlatform(0.6f, 1f);
        initialPlatform.setScale(2,0.5f);
        spawnEntityAt(initialPlatform, initialPos,false, false);

        // Jump-down platforms
        GridPoint2 firstJumpPos = new GridPoint2(13, tileBounds.y - 18);
        Entity firstJumpPlatform = PlatformFactory.createStaticPlatform();
        firstJumpPlatform.setScale(1.8f, 0.5f);
        spawnEntityAt(firstJumpPlatform, firstJumpPos,false, false);

        GridPoint2 secondJumpPos = new GridPoint2(8, tileBounds.y - 23);
        Entity secondJumpPlatform = PlatformFactory.createStaticPlatform();
        secondJumpPlatform.setScale(1, 0.5f);
        spawnEntityAt(secondJumpPlatform, secondJumpPos,false, false);
        // Pressure plate on top of this one todo

        // Volatile platform over death pit
        GridPoint2 deathPitPos = new GridPoint2(13, tileBounds.y - 33);
        Entity deathPitPlatform = PlatformFactory.createVolatilePlatform(30.3f, 2);
        deathPitPlatform.setScale(1, 0.5f);
        spawnEntityAt(deathPitPlatform, deathPitPos,false, false);

        // Safe standing platform
        GridPoint2 safePlatformPos = new GridPoint2(28, tileBounds.y - 30);
        Entity safePlatform = PlatformFactory.createStaticPlatform();
        safePlatform.setScale(2, 0.5f);
        spawnEntityAt(safePlatform, safePlatformPos,false, false);
    }

    /**
     * Spawns the traps at the start of the boss level
     */
    private void spawnTraps() {
        Vector2 firstSafePos = new Vector2((float) PLAYER_SPAWN.x / 2, (float) (PLAYER_SPAWN.y) / 2);
        Entity spikes = TrapFactory.createSpikes(firstSafePos, 90f);
        spawnEntityAt(spikes,
                new GridPoint2(15,tileBounds.y - 17), true,  true);

        Entity wall = WallFactory.createWall(10,0,1,5f,"");
        wall.setScale(1f,8f);
        spawnEntityAt(wall, new GridPoint2(16, tileBounds.y - 18),
                false, false);

    }

    private void spawnBats() {
        spawnBatsInitial();
    }

    /**
     * Spawn the bats at the beginning of the level (during the FirstDrop platforming section).
     */
    private void spawnBatsInitial() {
//         First bat blocking jumps between initial platforms
        BoxFactory.AutonomousBoxBuilder firstBatBuilder = new BoxFactory.AutonomousBoxBuilder();
        Entity verticalBat = firstBatBuilder
                .moveX(5.5f, 5.5f).moveY(13f, 25f)
                .texture("images/flying_bat.atlas")
                .speed(15f) // very hyperactive bat but it's ok we don't need realism
                .build();
        spawnEntityAt(verticalBat, new GridPoint2(
                (int) (firstBatBuilder.getSpawnX() * 2),
                (int) firstBatBuilder.getSpawnY()), true, true);

        // Second bat over jump to safe platform
        BoxFactory.AutonomousBoxBuilder safeBatBuilder = new BoxFactory.AutonomousBoxBuilder();
        Entity horizontalBat = safeBatBuilder
                .moveX(8, 13).moveY(tileBounds.y - 30, tileBounds.y - 30)
                .texture("images/flying_bat.atlas")
                .build();
        spawnEntityAt(horizontalBat, new GridPoint2(
                (int) safeBatBuilder.getSpawnX() * 2,
                (int) safeBatBuilder.getSpawnY()), true, true);
    }

    private void spawnBoss() {
        GridPoint2 spawnPos = new GridPoint2(35, 9);
        Entity boss = EnemyFactory.createBossEnemy(
                player,
                terrain.tileToWorldPosition(spawnPos)
        );
        spawnEntityAt(boss, spawnPos, true, true);
    }

    private void spawnDeathZone() {
        GridPoint2 spawnPos =  new GridPoint2(12,(tileBounds.y - 34));
        Entity deathZone = DeathZoneFactory.createDeathZone();
        deathZone.setScale(10,0.5f);
        deathZone.getComponent(ColliderComponent.class).setAsBoxAligned(deathZone.getScale().scl(0.8f),
                PhysicsComponent.AlignX.CENTER,
                PhysicsComponent.AlignY.BOTTOM);
        spawnEntityAt(deathZone, spawnPos, true,  false);
        // TODO add visuals to indicate this is a death zone & the surrounding areas aren't
    }

    private void playMusic() {
        Music music = ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class);
        music.setLooping(true);
        music.setVolume(UserSettings.getMusicVolumeNormalized());
        music.play();
    }

    protected Entity spawnPlayer() {
        Entity newPlayer = PlayerFactory.createPlayer(new ArrayList<>());
        spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
        newPlayer.getEvents().addListener("reset", this::reset);
        return newPlayer;
    }

    protected Entity spawnPlayer(List<Component> componentList) {
        Entity newPlayer = PlayerFactory.createPlayer(componentList);
        spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
        newPlayer.getEvents().addListener("reset", this::reset);
        return newPlayer;
    }

    private void displayUI() {
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("RUN. SURVIVE. [Boss Level]"));
        ui.addComponent(new TooltipSystem.TooltipDisplay());
        spawnEntity(ui);
    }
    private void spawnTerrain() {
        // Need to decide how large the area is going to be todo make screen shorter (y)
        terrain = createDefaultTerrain();
        spawnEntity(new Entity().addComponent(terrain));
        /*todo: fix lower wall accordingly*/

        // Terrain walls
        float tileSize = terrain.getTileSize();
        tileBounds = terrain.getMapBounds(0);
        Vector2 worldBounds = new Vector2(tileBounds.x * tileSize, tileBounds.y * tileSize);

        // Left
        spawnEntityAt(
                ObstacleFactory.createWall(WALL_THICKNESS, worldBounds.y), GridPoint2Utils.ZERO, false, false);
        // Right
        spawnEntityAt(
                ObstacleFactory.createWall(WALL_THICKNESS, worldBounds.y),
                new GridPoint2(tileBounds.x, 0),
                false,
                false);
        // Top
        spawnEntityAt(
                ObstacleFactory.createWall(worldBounds.x, WALL_THICKNESS),
                new GridPoint2(0, tileBounds.y - 4),
                false,
                false);
//        // Bottom
        spawnEntityAt(ObstacleFactory.createWall(worldBounds.x, WALL_THICKNESS)
                        .addComponent(new TextureRenderComponent("images/gate.png")),
                new GridPoint2(0, -3), false, false);
    }

    private void spawnParallaxBackground() {
        Entity backgroundEntity = new Entity();

        // Get the camera from the player entity
        Camera gameCamera = ServiceLocator.getRenderService().getRenderer().getCamera().getCamera();

        // Get map dimensions from terrain
        GridPoint2 mapBounds = terrain.getMapBounds(0);
        float tileSize = terrain.getTileSize();
        float mapWorldWidth = mapBounds.x * tileSize;
        float mapWorldHeight = mapBounds.y * tileSize;

        ParallaxBackgroundComponent parallaxBg = new ParallaxBackgroundComponent(gameCamera, mapWorldWidth, mapWorldHeight);


        ResourceService resourceService = ServiceLocator.getResourceService();

        // todo find backgrounds
        // Layer 7 - Farthest background (barely moves)
        Texture backgroundTexture = resourceService.getAsset("images/cavelevel/background/7.png", Texture.class);
        parallaxBg.addScaledLayer(backgroundTexture, 0f, -17, -14, 0.18f);

        // Layer 6 - Far background
        Texture layer1 = resourceService.getAsset("images/cavelevel/background/6.png", Texture.class);
        parallaxBg.addScaledLayer(layer1, 0.2f, -17, -14, 0.18f);

        // Layer 5 - Mid-far background
        Texture layer2 = resourceService.getAsset("images/cavelevel/background/5.png", Texture.class);
        parallaxBg.addScaledLayer(layer2, 0.3f, -17, -14, 0.18f);

        // Layer 4 - Mid background
        Texture layer3 = resourceService.getAsset("images/cavelevel/background/4.png", Texture.class);
        parallaxBg.addScaledLayer(layer3, 0.4f, -17, -14, 0.18f);

        // Layer 3 - Mid-near background
        Texture layer4 = resourceService.getAsset("images/cavelevel/background/3.png", Texture.class);
        parallaxBg.addScaledLayer(layer4, 0.6f, -17, -14, 0.18f);

        // Layer 2 - Near background
        Texture layer5 = resourceService.getAsset("images/cavelevel/background/2.png", Texture.class);
        parallaxBg.addScaledLayer(layer5, 0.8f, -17, -14, 0.18f);

        // Layer 1 - Nearest background (moves fastest)
        Texture layer6 = resourceService.getAsset("images/cavelevel/background/1.png", Texture.class);
        parallaxBg.addScaledLayer(layer6, 1f, -17, -14, 0.18f);

        backgroundEntity.addComponent(parallaxBg);
        spawnEntity(backgroundEntity);
    }

    private TerrainComponent createDefaultTerrain() {
        // Use empty texture for invisible terrain grid
        final ResourceService resourceService = ServiceLocator.getResourceService();
        TextureRegion emptyTile = new TextureRegion(resourceService.getAsset("images/empty.png", Texture.class));

        GridPoint2 tilePixelSize = new GridPoint2(emptyTile.getRegionWidth(), emptyTile.getRegionHeight());
        TiledMap tiledMap = terrainFactory.createDefaultTiles(tilePixelSize, emptyTile, emptyTile, emptyTile, emptyTile, mapSize);
        return terrainFactory.createInvisibleFromTileMap(0.5f, tiledMap, tilePixelSize);
    }

    private void spawnEvilMovingPlatform() {
        GridPoint2 buttonPlatformPos = new GridPoint2(62, tileBounds.y - 9);
        Vector2 offsetWorldButton = new Vector2(-15f, 0f);
        float speed = 5f;

        Entity buttonPlatform = PlatformFactory.createButtonTriggeredPlatform(offsetWorldButton, speed);
        buttonPlatform.setScale(1.5f, 0.5f);
        spawnEntityAt(buttonPlatform, buttonPlatformPos, false, false);
        logger.info("Moving platform spawned at {}", buttonPlatformPos);

        //start button
        Entity button = ButtonFactory.createButton(false, "platform", "left");
        GridPoint2 buttonStartPos = new GridPoint2(64, 50);
        spawnEntityAt(button, buttonStartPos, true, true);
        logger.info("Platform button spawned at {}", buttonStartPos);

        button.getEvents().addListener("buttonToggled", (Boolean isPressed) -> {
            if (isPressed) {
                logger.info("Button pressed — activating platform");
                buttonPlatform.getEvents().trigger("activatePlatform");
            } else {
                logger.info("Button unpressed — deactivating platform");
                buttonPlatform.getEvents().trigger("deactivatePlatform");
            }
        });
    }

    private void spawnObjectives() {
        // Large, invisible sensors — easy to grab, no textures.
        // IDs chosen to match the ObjectiveTab banner map.
        Gdx.app.log("BossLevel", "Spawning objectives…");
    }

    protected void loadAssets() {
        logger.debug("Loading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.loadMusic(musics);
        resourceService.loadTextures(gameTextures);
        resourceService.loadTextureAtlases(gameTextureAtlases);
        resourceService.loadSounds(gameSounds);

        while (!resourceService.loadForMillis(10)) {
            // This could be upgraded to a loading screen
            logger.info("Loading... {}%", resourceService.getProgress());
        }
    }

    private void unloadAssets() {
        logger.debug("Unloading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.unloadAssets(gameTextures);
        resourceService.unloadAssets(gameTextureAtlases);
        resourceService.unloadAssets(gameSounds);
        resourceService.unloadAssets(musics);
    }

    @Override
    public void dispose() {
        super.dispose();
        ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class).stop();
        this.unloadAssets();
    }
}
