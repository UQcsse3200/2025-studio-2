package com.csse3200.game.areas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.ButtonManagerComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.boss.BossSpawnerComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.minimap.MinimapComponent;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.parallax.ParallaxBackgroundComponent;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BossLevelGameArea extends GameArea {
    private static final GridPoint2 mapSize = new GridPoint2(100,57);
    private static final float WALL_THICKNESS = 0.1f;
    private static GridPoint2 PLAYER_SPAWN;
    private boolean keySpawned;
    private static final String[] gameTextures = {
            "images/button.png",
            "images/key.png",
            "images/button_pushed.png",
            "images/blue_button.png",
            "images/blue_button_pushed.png",
            "images/red_button.png",
            "images/red_button_pushed.png",
            "images/box_orange.png",
            "images/box_blue.png",
            "images/box_white.png",
            "images/blue_button.png",
            "images/spikes_sprite.png",
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
            "images/bomb.png",
            "images/cube.png",
            "images/laser-end.png",
            "images/LaserShower-end.png",
            "images/camera-body.png",
            "images/camera-lens.png",
            "images/glide_powerup.png",
            "images/wall.png",
            "images/dash_powerup.png",
            "images/ladder.png",
            "images/ladder-base.png",
            "images/blackSquare.png",
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
            "images/laser-end",
            "images/minimap_forest_area.png",
            "images/minimap_player_marker.png",
    };
    private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";
    private static final String[] musics = {backgroundMusic};
    private static final String[] gameSounds = {"sounds/Impact4.ogg",
            "sounds/buttonsound.mp3",
            "sounds/damagesound.mp3",
            "sounds/deathsound.mp3",
            "sounds/doorsound.mp3",
            "sounds/explosion.mp3",
            "sounds/hurt.mp3",
//            "sounds/interactsound.mp3",
            "sounds/jetpacksound.mp3",
            "sounds/thudsound.mp3",
            "sounds/walksound.mp3",
            "sounds/laserShower.mp3",
            "sounds/pickupsound.mp3"
    };
    private static final String[] gameTextureAtlases = {
            "images/PLAYER.atlas",
            "images/drone.atlas", // <---
            "images/boss.atlas", // Comment out these lines to fix the loading time
            "images/doors.atlas",
            "images/volatile_platform.atlas",
            "images/timer.atlas",
            "images/flying_bat.atlas", // Bat sprites from https://todemann.itch.io/bat (see Wiki)
            "images/laser.atlas",
            "images/drone_scout.atlas",
            "images/drone_chaser.atlas",
            "images/drone_brutal.atlas"
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
        keySpawned = false;
        spawnParallaxBackground();
        spawnPlatforms();
        spawnWalls();
        spawnStaticObstacles();
        spawnObjectives();
        spawnLaserPuzzle();
        spawnEndgameButton();
        spawnBoss(); // Comment out this line if removing the long-loading assets
    }

    /**
     * Create the laser puzzle (actual lasers, objects, etc)
     * (AIM: REACH THE END WITH THE BOX)
     */
    private void spawnLaserPuzzle() {
        // Box at start
        Entity box = BoxFactory.createMoveableBox();
        spawnEntityAt(box, new GridPoint2(64, 15), false, false);

        // Laser attached to the upper wall
        Entity laser0 = LaserFactory.createLaserEmitter(335f);
        spawnEntityAt(laser0, new GridPoint2(63, 25), false, false);

        // Laser attached to the end platform
        Entity laser1 = LaserFactory.createLaserEmitter(180f);
        spawnEntityAt(laser1, new GridPoint2(tileBounds.x - 10, 23), false, false);

        // Laser attached to the lower wall
        Entity laser2 = LaserFactory.createLaserEmitter(65f);
        spawnEntityAt(laser2, new GridPoint2(63, 3), false, false);

        // Button-blocking laser at end
        Entity endLaser = LaserFactory.createLaserEmitter(270f);
        spawnEntityAt(endLaser, new GridPoint2(tileBounds.x - 5, tileBounds.y + 5), false, false);

        // Laser to stop you from getting yeeted off the right
        Entity safeLaser = LaserFactory.createLaserEmitter(270f);
        spawnEntityAt(safeLaser, new GridPoint2(tileBounds.x - 4, 21), false, false);
    }

    /**
     * It's finally laser room time! Platforms so it doesn't need cheat code lol
     */
    private void spawnLaserRoomPlatforms() {
        // Platform immediately upon entering
        GridPoint2 boxPos = new GridPoint2(63, 12);
        Entity firstPlatform = PlatformFactory.createStaticPlatform();
        firstPlatform.setScale(2f, 0.5f);
        spawnEntityAt(firstPlatform, boxPos,false, false);

        // Normal jumping platforms are numbered by the order in which they should be traversed
        GridPoint2 pos1 = new GridPoint2(74, 16);
        Entity platform1 = PlatformFactory.createStaticPlatform();
        platform1.setScale(1f, 0.5f);
        spawnEntityAt(platform1, pos1,false, false);

        GridPoint2 pos2 = new GridPoint2(80, 20);
        Entity platform2 = PlatformFactory.createStaticPlatform();
        platform2.setScale(1.5f, 0.5f);
        spawnEntityAt(platform2, pos2,false, false);

        GridPoint2 pos3 = new GridPoint2(77, 25);
        Entity platform3 = PlatformFactory.createStaticPlatform();
        platform3.setScale(1f, 0.5f);
        spawnEntityAt(platform3, pos3,false, true);

        GridPoint2 pos4 = new GridPoint2(70, 30);
        Entity platform4 = PlatformFactory.createStaticPlatform();
        platform4.setScale(1f, 0.5f);
        spawnEntityAt(platform4, pos4,false, true);

        // Reflector platform for the laser at the top
        GridPoint2 reflectorPos = new GridPoint2(78, 35);
        Entity reflectorPlatform = PlatformFactory.createReflectivePlatform();
        reflectorPlatform.setScale(3f, 0.5f);
        spawnEntityAt(reflectorPlatform, reflectorPos,true, true);

        // Platform to stand on to hit endgame button
        GridPoint2 endgamePos = new GridPoint2(tileBounds.x - 10, 23);
        Entity endgamePlatform = PlatformFactory.createStaticPlatform();
        endgamePlatform.setScale(6f, 0.8f);
        spawnEntityAt(endgamePlatform, endgamePos,false, false);
    }
    public void spawnLaserShower(float X , float Y) {
        if (player == null) return; // safety check

        // Spawn lasers behind of the player
        for (int i = 0; i <= 5; i++) {
            Entity laser = LaserFactory.createLaserShower(-90f); // Create another downward laser
            float xBehind = X - ((i + 1) * 7.5f); // offset left
            spawnEntityAt(laser, new GridPoint2(Math.round(xBehind+10f), Math.round(Y+15f)), true, true);
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
            float xAhead = X + ((i + 1) * 7.5f); // offset right
            spawnEntityAt(laser, new GridPoint2(Math.round(xAhead+10f), Math.round(Y+15f)), true, true);
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


    /**
     * Spawn the walls between the two halves of the level, as well as next to the death pit
     */
    private void spawnWalls() {
        // Lower wall between level halves
        Entity lowerWall = WallFactory.createWall(15,0,1,5f,"");
        lowerWall.setScale(2f,12f);
        spawnEntityAt(lowerWall, new GridPoint2(60, -4),
                false, false);

        // Door between levels
        Entity door = ObstacleFactory.createDoor("key:door", this, null, true);
        door.setScale(1f, 1.5f);
        spawnEntityAt(door, new GridPoint2(62, 20), true, false);

        // Upper wall between level halves
        Entity upperWall = WallFactory.createWall(20, 30,  1,5f,"");
        upperWall.setScale(2f,15f);
        spawnEntityAt(upperWall, new GridPoint2(60, 23),
                false, false);
    }

    private void spawnStaticObstacles() {
        spawnDeathZone();
        spawnTraps();
        spawnBats();
    }

    private void spawnPlatforms() {
        spawnFirstDrop();
        spawnUpwardPath();
        spawnLaserRoomPlatforms();
    }

    /**
     * Spawns the platforms (left half of screen, upper path) leading towards the door
     */
    private void spawnUpwardPath() {
        GridPoint2 firstPos = new GridPoint2(40, tileBounds.y - 27);
        Entity firstPlatform = PlatformFactory.createStaticPlatform();
        firstPlatform.setScale(1, 0.5f);
        spawnEntityAt(firstPlatform, firstPos,false, false);

        GridPoint2 secondPos = new GridPoint2(47, tileBounds.y - 25);
        Entity secondPlatform = PlatformFactory.createVolatilePlatform(0.5f, 3f);
        secondPlatform.setScale(1.5f, 0.5f);
        spawnEntityAt(secondPlatform, secondPos,false, false);

        spawnEvilPlatform();

        GridPoint2 fourthPos = new GridPoint2(50, tileBounds.y - 16);
        Entity fourthPlatform = PlatformFactory.createStaticPlatform();
        fourthPlatform.setScale(1.5f, 0.5f);
        spawnEntityAt(fourthPlatform, fourthPos,false, false);

        spawnKeyButtonAndPlatform();
    }

    /**
     * The evil platform is a moving platform that glows red.
     * Pressing its accompanying button absolutely launches the player towards the boss.
     */
    private void spawnEvilPlatform() {
        // The glow component because it's cool
        ConeLightComponent evilGlow = new ConeLightComponent(
                ServiceLocator.getLightingService().getEngine().getRayHandler(),
                128,
                new Color().set(1f, 0f, 0f, 0.6f),
                2.5f,
                0f,
                180f);

        // The platform
        GridPoint2 pos = new GridPoint2(57, tileBounds.y - 23);
        Entity platform = PlatformFactory.createButtonTriggeredPlatform(new Vector2(-20f, 0f), 20f);
        platform.setScale(1.5f, 0.5f);
        spawnEntityAt(platform, pos,false, false);

        // The button
        Entity button = ButtonFactory.createButton(false, "platform", "left");
        button.addComponent(evilGlow);
        spawnEntityAt(button, new GridPoint2(59, tileBounds.y - 22), true, true);

        button.getEvents().addListener("buttonToggled", (Boolean isPressed) -> {
            if (isPressed) {
                platform.getEvents().trigger("activatePlatform");
            } else {
                platform.getEvents().trigger("deactivatePlatform");
            }
        });

    }

    /**
     * Spawn platforms for the initial drop-down and move forward (first 5 platforms)
     * section of the level.
     */
    private void spawnFirstDrop() {
        // Volatile platform player initially spawns on
        GridPoint2 initialPos = new GridPoint2(3,tileBounds.y - 10);
        Entity initialPlatform = PlatformFactory.createStaticPlatform();
        initialPlatform.setScale(2,0.5f);
        spawnEntityAt(initialPlatform, initialPos,false, false);

        // Jump-down platforms
        GridPoint2 firstJumpPos = new GridPoint2(13, tileBounds.y - 18);
        Entity firstJumpPlatform = PlatformFactory.createStaticPlatform();
        firstJumpPlatform.setScale(1.8f, 0.5f);
        spawnEntityAt(firstJumpPlatform, firstJumpPos,false, false);

        // TODO when landing on this platform, trigger self-destruct drone at player's location
        GridPoint2 secondJumpPos = new GridPoint2(8, tileBounds.y - 23);
        Entity secondJumpPlatform = PlatformFactory.createStaticPlatform();
        secondJumpPlatform.setScale(1, 0.5f);
        spawnEntityAt(secondJumpPlatform, secondJumpPos,false, false);

        // Volatile platform over death pit
        GridPoint2 deathPitPos = new GridPoint2(13, tileBounds.y - 33);
        Entity deathPitPlatform = PlatformFactory.createVolatilePlatform(0.3f, 2);
        deathPitPlatform.setScale(1, 0.5f);
        spawnEntityAt(deathPitPlatform, deathPitPos,false, false);

        // Safe standing platform
        GridPoint2 safePlatformPos = new GridPoint2(28, tileBounds.y - 30);
        Entity safePlatform = PlatformFactory.createStaticPlatform();
        safePlatform.setScale(2, 0.5f);
        spawnEntityAt(safePlatform, safePlatformPos,false, false);
    }

    /**
     * Spawns the traps in the boss level, with the exception of the one on the floor that will be destroyed,
     * which is created in that method so it can be in the same list of entities to be destroyed
     */
    private void spawnTraps() {
        // Spawn trap on first platform
        Vector2 firstSafePos = new Vector2((float) PLAYER_SPAWN.x / 2, (float) (PLAYER_SPAWN.y) / 2);
        Entity spikes1 = TrapFactory.createSpikes(firstSafePos, 90f);
        spawnEntityAt(spikes1,
                new GridPoint2(15,tileBounds.y - 17), true,  true);
        Entity spikes2 = TrapFactory.createSpikes(firstSafePos, 90f);
        spawnEntityAt(spikes2,
                new GridPoint2(15,tileBounds.y - 15), true,  true);

        Entity wall = WallFactory.createWall(10,0,1,5f,"");
        wall.setScale(1f,8f);
        spawnEntityAt(wall, new GridPoint2(16, tileBounds.y - 18),
                false, false);

        // Spawn trap on wall - removed to make easier
//        Vector2 highPlatformSafePos = new Vector2(28, 17);
//        Entity spikes3 = TrapFactory.createSpikes(highPlatformSafePos, 0f);
//        spawnEntityAt(spikes3,
//                new GridPoint2(60,17), false,  true);
//        Entity spikes4 = TrapFactory.createSpikes(highPlatformSafePos, 0f);
//        spawnEntityAt(spikes4,
//                new GridPoint2(62,17), false,  true);
    }

    private void spawnBats() {
        spawnBatsInitial();
        spawnBatsUpwardPath();
    }

    /**
     * Spawns the bats over and around the red herring path area
     */
    private void spawnBatsUpwardPath() {
        // Bat over first jump (stick low in the double jump to avoid it!
        BoxFactory.AutonomousBoxBuilder horizontalBatBuilder = new BoxFactory.AutonomousBoxBuilder();
        Entity horizontalBat = horizontalBatBuilder
                .moveX(15, 18).moveY(tileBounds.y - 23, tileBounds.y - 23)
                .texture("images/flying_bat.atlas")
                .speed(10f).build();
        spawnEntityAt(horizontalBat, new GridPoint2(
                (int) horizontalBatBuilder.getSpawnX() * 2,
                (int) horizontalBatBuilder.getSpawnY()), true, true);

        // Bat flying around in next jump area
        BoxFactory.AutonomousBoxBuilder chaoticBatBuilder = new BoxFactory.AutonomousBoxBuilder();
        Entity chaoticBat = chaoticBatBuilder
                .moveX(22f, 24f).moveY(5f, 12f)
                .texture("images/flying_bat.atlas")
                .speed(5f).build();
        spawnEntityAt(chaoticBat, new GridPoint2(
                (int) (chaoticBatBuilder.getSpawnX() * 2),
                (int) chaoticBatBuilder.getSpawnY()), true, true);
    }

    /**
     * Spawn the bats at the beginning of the level (during the FirstDrop platforming section).
     */
    private void spawnBatsInitial() {
//         First bat blocking jumps between initial platforms
        BoxFactory.AutonomousBoxBuilder firstBatBuilder = new BoxFactory.AutonomousBoxBuilder();
        Entity verticalBat = firstBatBuilder
                .moveX(5.5f, 5.5f).moveY(3f, 15f)
                .texture("images/flying_bat.atlas")
                .speed(15f) // very hyperactive bat but it's ok we don't need realism
                .build();
        spawnEntityAt(verticalBat, new GridPoint2(
                (int) (firstBatBuilder.getSpawnX() * 2),
                (int) firstBatBuilder.getSpawnY()), true, true);

        // Second bat over jump to safe platform
        BoxFactory.AutonomousBoxBuilder safeBatBuilder = new BoxFactory.AutonomousBoxBuilder();
        Entity horizontalBat = safeBatBuilder
                .moveX(8, 10.5f).moveY(tileBounds.y - 30, tileBounds.y - 30)
                .texture("images/flying_bat.atlas")
                .build();
        spawnEntityAt(horizontalBat, new GridPoint2(
                (int) safeBatBuilder.getSpawnX() * 2,
                (int) safeBatBuilder.getSpawnY()), true, true);
    }

    private void spawnBoss() {
        GridPoint2 spawnPos = new GridPoint2(1, 40);

        Entity boss = EnemyFactory.createBossEnemy(player, terrain.tileToWorldPosition(spawnPos));

        BossSpawnerComponent spawnComp = boss.getComponent(BossSpawnerComponent.class);
        if (spawnComp != null) {
            spawnComp.resetTriggers();

            // You can change these values to trigger when it spawns the drones
            spawnComp.addSpawnTrigger(new Vector2(20f, 0f));
            spawnComp.addSpawnTrigger(new Vector2(40f, 0f));
            spawnComp.addSpawnTrigger(new Vector2(60f, 0f));

        }
        spawnEntityAt(boss, spawnPos, true, true);

        boss.getEvents().addListener("reset", () -> {
            BossSpawnerComponent spawnComponent = boss.getComponent(BossSpawnerComponent.class);
            if (spawnComponent != null) {
                spawnComponent.resetTriggers();
                spawnComponent.cleanupDrones();
            }
        });
    }

    private void spawnDeathZone() {
        Entity deathZone2 = DeathZoneFactory.createDeathZone();
        deathZone2.setScale(100,0.5f);
        deathZone2.getComponent(ColliderComponent.class).setAsBoxAligned(deathZone2.getScale().scl(0.8f),
                PhysicsComponent.AlignX.LEFT,
                PhysicsComponent.AlignY.BOTTOM);
        spawnEntityAt(deathZone2, new GridPoint2(0, -2), false,  false);

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
        terrain = createDefaultTerrain();
        spawnEntity(new Entity().addComponent(terrain));

        // Terrain walls
        float tileSize = terrain.getTileSize();
        tileBounds = terrain.getMapBounds(0);
        Vector2 worldBounds = new Vector2(tileBounds.x * tileSize, tileBounds.y * tileSize);

        // Left
        spawnEntityAt(
                ObstacleFactory.createWall(WALL_THICKNESS, worldBounds.y), new GridPoint2(0, -4), false, false);
        // Right
        spawnEntityAt(
                ObstacleFactory.createWall(WALL_THICKNESS, worldBounds.y),
                new GridPoint2(tileBounds.x, -4),
                false,
                false);
        // Top
        spawnEntityAt(
                ObstacleFactory.createWall(worldBounds.x, WALL_THICKNESS),
                new GridPoint2(0, tileBounds.y + 10),
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
        final ResourceService resourceService = ServiceLocator.getResourceService();
        TextureRegion baseTile =
                new TextureRegion(resourceService.getAsset("images/empty.png", Texture.class));
        GridPoint2 tilePixelSize = new GridPoint2(baseTile.getRegionWidth(), baseTile.getRegionHeight());
        TiledMap tiledMap = terrainFactory.createDefaultTiles(tilePixelSize, baseTile, baseTile, baseTile, baseTile, mapSize);
        return terrainFactory.createFromTileMap(0.5f, tiledMap, tilePixelSize);
    }

    private void spawnKeyButtonAndPlatform() {
        Entity platform = PlatformFactory.createStaticPlatform();
        platform.setScale(1.5f, 0.5f);
        spawnEntityAt(platform, new GridPoint2(57, tileBounds.y - 9), false, false);

        // Button
        Entity button = ButtonFactory.createButton(false, "door", "left");
        spawnEntityAt(button, new GridPoint2(59, tileBounds.y - 8), true, true);

        button.getEvents().addListener("buttonToggled", (Boolean isPushed) -> {
            if (isPushed && !keySpawned) {
                spawnKey();
                keySpawned = true;
            }
        });
    }

    private void spawnKey() {
        // Code from key spawn in Level One
        Entity key = CollectableFactory.createCollectable("key:door");
        key.addComponent(new MinimapComponent("images/key.png"));
        spawnEntityAt(key, new GridPoint2(50, tileBounds.y - 15), true, true);
    }

    /**
     * Spawns the comically huge button that ends the game.
     * TODO replace with Shane's captcha.
     */
    private void spawnEndgameButton() {
        GridPoint2 buttonPos = new GridPoint2(tileBounds.x, 24);
        Entity winGameButton = ButtonFactory.createButton(false, "platform", "left");
        winGameButton.setScale(2f, 5f);
        spawnEntityAt(winGameButton, buttonPos, true, false);

        winGameButton.getEvents().addListener("buttonToggled", (Boolean isPushed) -> {
            endLevel();
        });
    }

    /**
     * End the boss level and go to the post-game cutscene.
     * TODO: Replace the placeholder SPRINT_ONE area with the actual end-game cutscene.
     */
    public void endLevel() {
        Entity fakeDoor = ObstacleFactory.createDoor("key", this, String.valueOf(MainGameScreen.Areas.SPRINT_ONE), false);
        getEvents().trigger("doorEntered", player, fakeDoor);
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