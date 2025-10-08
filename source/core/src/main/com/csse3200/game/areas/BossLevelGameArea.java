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
import com.csse3200.game.components.BossLaserAttack;
import com.csse3200.game.components.ButtonManagerComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.LaserRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.parallax.ParallaxBackgroundComponent;
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
            "images/cube.png",
            "images/laser-end.png",
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
            "sounds/buttonsound.mp3",
            "sounds/chimesound.mp3",
            "sounds/CircuitGoodness.mp3",
            "sounds/damagesound.mp3",
            "sounds/deathsound.mp3",
            "sounds/doorsound.mp3",
            "sounds/explosion.mp3",
            "sounds/Flow.mp3",
            "sounds/gamemusic.mp3",
            "sounds/hurt.mp3",
            "sounds/interactsound.mp3",
            "sounds/jetpacksound.mp3",
            "sounds/KindaLikeTycho.mp3",
            "sounds/laddersound.mp3",
            "sounds/pickupsound.mp3",
            "sounds/thudsound.mp3",
            "sounds/walksound.mp3",
            "sounds/whooshsound.mp3"
    };
    private static final String[] gameTextureAtlases = {
            "images/PLAYER.atlas",
            "images/drone.atlas",
            "images/boss.atlas",
            "images/volatile_platform.atlas",
            "images/timer.atlas",
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
        spawnWalls();
        spawnStaticObstacles();
        Entity[] toBeDestroyed = spawnCeilingObstacles();
        // Pass toBeDestroyed to this.destroyFloor() when triggering.
        spawnButtonPuzzleRoom(toBeDestroyed);
        spawnObjectives();
        spawnLaserPuzzle();
        spawnEndgameButton();
        spawnBoss();

    }

    /**
     * Create the laser puzzle (actual lasers, objects, etc)
     * (AIM: REACH THE END WITH THE BOX)
     */
    private void spawnLaserPuzzle() {
        // Box at start
        Entity reflectorBox = BoxFactory.createReflectorBox();
        spawnEntityAt(reflectorBox, new GridPoint2(63, 15), false, false);

        // Laser attached to the upper wall
        Entity laser0 = LaserFactory.createLaserEmitter(335f);
        spawnEntityAt(laser0, new GridPoint2(63, 25), false, false);

        // Laser attached to the end platform
        Entity laser1 = LaserFactory.createLaserEmitter(180f);
        spawnEntityAt(laser1, new GridPoint2(tileBounds.x - 10, 23), false, false);

        // Laser attached to the lower wall
        Entity laser2 = LaserFactory.createLaserEmitter(65f);
        spawnEntityAt(laser2, new GridPoint2(63, 2), false, false);

        // Button-blocking laser at end
        Entity endLaser = LaserFactory.createLaserEmitter(270f);
        spawnEntityAt(endLaser, new GridPoint2(tileBounds.x - 5, tileBounds.y - 5), false, false);
    }

    /**
     * Destroys the floor object and the items on it.
     */
    private void destroyFloor(Entity[] toDestroy) {
        for (Entity entity: toDestroy) {
            entity.dispose();
        }
    }

    /**
     * It's finally laser room time! Platforms so it doesn't need cheat code lol
     */
    private void spawnLaserRoomPlatforms() {
        // Platform immediately upon entering; holds reflector box that will be used.
        GridPoint2 boxPos = new GridPoint2(63, 12);
        Entity firstPlatform = PlatformFactory.createStaticPlatform();
        firstPlatform.setScale(2f, 0.5f);
        spawnEntityAt(firstPlatform, boxPos,false, false);

        // Normal jumping platforms are numbered by the order in which they should be traversed
        GridPoint2 pos1 = new GridPoint2(74, 17); // I swear I'm not 1-indexing; boxPos is platform0
        Entity platform1 = PlatformFactory.createStaticPlatform();
        platform1.setScale(1f, 0.5f);
        spawnEntityAt(platform1, pos1,false, false);

        GridPoint2 pos2 = new GridPoint2(80, 20);
        Entity platform2 = PlatformFactory.createStaticPlatform();
        platform2.setScale(1f, 0.5f);
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

    /**
     * Spawn the walls between the two halves of the level, as well as next to the death pit
     */
    private void spawnWalls() {
        // Lower wall between level halves
        Entity lowerWall = WallFactory.createWall(15,0,1,5f,"");
        lowerWall.setScale(2f,10f);
        spawnEntityAt(lowerWall, new GridPoint2(60, -3),
                false, false);

        // Upper wall between level halves
        Entity upperWall = WallFactory.createWall(20,tileBounds.y - 40,1,5f,"");
        upperWall.setScale(2f,15f);
        spawnEntityAt(upperWall, new GridPoint2(60, tileBounds.y - 34),
                false, false);

        // Wall blocking death pit
        Entity deathPitWall = WallFactory.createWall(20,tileBounds.y - 40,1,5f,"");
        deathPitWall.setScale(1.3f,13f);
        spawnEntityAt(deathPitWall, new GridPoint2(20, -3),
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
        spawnButtonPlatforms();
        spawnLaserRoomPlatforms();
    }

    /**
     * Spawns the platforms (left half of screen, upper path)
     * leading towards the shut gate and evil button.
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

        GridPoint2 thirdPos = new GridPoint2(57, tileBounds.y - 23);
        Entity thirdPlatform = PlatformFactory.createStaticPlatform();
        thirdPlatform.setScale(1.5f, 0.5f);
        spawnEntityAt(thirdPlatform, thirdPos,false, false);

        GridPoint2 fourthPos = new GridPoint2(50, tileBounds.y - 16);
        Entity fourthPlatform = PlatformFactory.createStaticPlatform();
        fourthPlatform.setScale(1.5f, 0.5f);
        spawnEntityAt(fourthPlatform, fourthPos,false, false);

        spawnEvilMovingPlatform();
    }

    /**
     * Spawns everything for the timed-button puzzle in the first section.
     * Includes: Moving platform to exit room, the buttons themselves,
     * and the lasers that will be turned off upon puzzle completion.
     * Does NOT include the platforms within the area, which are part of spawnPlatforms()
     *
     * @param toDestroy: The floor object and objects on it, to be destroyed when the bomber drones
     *                 blow up the floor. For demonstration purposes only.
     */
    private void spawnButtonPuzzleRoom(Entity[] toDestroy) {
        spawnExitMovingPlatform();
        Entity laser1 = LaserFactory.createLaserEmitter(0f);
        spawnEntityAt(laser1, new GridPoint2(28, tileBounds.y - 36), false, false);
        Entity laser2 = LaserFactory.createLaserEmitter(0f);
        spawnEntityAt(laser2, new GridPoint2(28, tileBounds.y - 38), false, false);

        // The button puzzle itself to generate a reflective box next to the moving platform
        Entity puzzleEntity = new Entity();
        ButtonManagerComponent manager = new ButtonManagerComponent();
        puzzleEntity.addComponent(manager);
        // Prevent leak
        this.spawnEntityAt(puzzleEntity, new GridPoint2(0, 0), true, true);

        // Spawn buttons
        // First button (easy enough to reach hopefully)
        Entity button1 = ButtonFactory.createPuzzleButton(false, "nothing", "left", manager);
        spawnEntityAt(button1, new GridPoint2(32,1), true,  true);

        // Centre button
        Entity button2 = ButtonFactory.createPuzzleButton(false, "nothing", "right", manager);
        spawnEntityAt(button2, new GridPoint2(38,8), true,  true);

        // Ceiling button - NOT FOR USE IN-GAME UNLESS HIGH DIFFICULTY WANTED
//        Entity button3 = ButtonFactory.createPuzzleButton(false, "nothing", "down", manager);
//        spawnEntityAt(button3, new GridPoint2(43,16), true,  true);

        // Buttons next to each other
        Entity button4 = ButtonFactory.createPuzzleButton(false, "nothing", "left", manager);
        spawnEntityAt(button4, new GridPoint2(55,8), true,  true);
        Entity button5 = ButtonFactory.createPuzzleButton(false, "nothing", "right", manager);
        spawnEntityAt(button5, new GridPoint2(51,8), true,  true);

        // Button in the wall
        Entity button6 = ButtonFactory.createPuzzleButton(false, "nothing", "left", manager);
        spawnEntityAt(button6, new GridPoint2(59,13), true,  true);

        puzzleEntity.getEvents().addListener("puzzleCompleted", () -> {
            laser1.dispose();
            laser2.dispose();
            destroyFloor(toDestroy); // TODO this is ONLY for demonstration purposes. Should be boss-triggered
        });
    }

    /**
     * Spawn the platforms for the button puzzle room
     */
    private void spawnButtonPlatforms() {
        // Stable platform in the centre of the room
        GridPoint2 firstPos = new GridPoint2(43, 2);
        Entity centrePlatform = PlatformFactory.createStaticPlatform();
        centrePlatform.setScale(2f, 0.5f);
        spawnEntityAt(centrePlatform, firstPos,false, false);

        // Platform beneath the second button
        GridPoint2 secondPos = new GridPoint2(38, 7);
        Entity secondPlatform = PlatformFactory.createVolatilePlatform(0.5f, 1f);
        secondPlatform.setScale(1.5f, 0.5f);
        spawnEntityAt(secondPlatform, secondPos,false, false);

        // Platform beneath the ceiling button - commented out because I think the ceiling button makes it too hard
//        GridPoint2 thirdPos = new GridPoint2(43, 13);
//        Entity ceilingPlatform = PlatformFactory.createVolatilePlatform(0.5f, 1f);
//        ceilingPlatform.setScale(2f, 0.5f);
//        spawnEntityAt(ceilingPlatform, thirdPos,true, false);

        // Platform beneath the twin buttons
        GridPoint2 fourthPos = new GridPoint2(51, 7);
        Entity twinButtonPlatform = PlatformFactory.createStaticPlatform();
        twinButtonPlatform.setScale(2.5f, 0.5f);
        spawnEntityAt(twinButtonPlatform, fourthPos,false, false);

        // Platform beneath the wall button
        GridPoint2 fifthPos = new GridPoint2(58, 12);
        Entity wallPlatform = PlatformFactory.createStaticPlatform();
        wallPlatform.setScale(1f, 0.5f);
        spawnEntityAt(wallPlatform, fifthPos,false, false);
    }

    /**
     * Spawn moving platform that'll carry player up to the top of puzzle room
     */
    private void spawnExitMovingPlatform() {
        // Set up variables
        GridPoint2 buttonPlatformPos = new GridPoint2(23, -3);
        GridPoint2 buttonPos = new GridPoint2(22, -1);
        Vector2 movementAmount = new Vector2(0f, 16f);
        float speed = 20f;

        // Spawn the platform
        Entity buttonPlatform = PlatformFactory.createButtonTriggeredPlatform(movementAmount, speed);
        buttonPlatform.setScale(1.5f, 0.5f);
        spawnEntityAt(buttonPlatform, buttonPlatformPos, false, false);

        // Spawn the button
        Entity button = ButtonFactory.createButton(false, "platform", "right");
        spawnEntityAt(button, buttonPos, true, true);

        button.getEvents().addListener("buttonToggled", (Boolean isPressed) -> {
            if (isPressed) {
                buttonPlatform.getEvents().trigger("activatePlatform");
            } else {
                buttonPlatform.getEvents().trigger("deactivatePlatform");
            }
        });
    }

    /**
     * Create the obstacles over the ceiling of the button puzzle room/floor of red herring room,
     * including the trap, wall, and ceiling itself. Does NOT include the lasers,
     * as these are part of the timed button puzzle.
     */
    private Entity[] spawnCeilingObstacles() {
        Entity[] floorObjects = new Entity[3];

        // Spawn floor
        GridPoint2 ceilingPos = new GridPoint2(26, tileBounds.y - 40);
        Entity gateFloor = FloorFactory.createStaticFloor();
        floorObjects[0] = gateFloor;
        gateFloor.setScale(19f, 1);
        spawnEntityAt(gateFloor, ceilingPos, false, false);

        Vector2 safePlatformPos = new Vector2(14, 14.5f);
        Entity spikesGround = TrapFactory.createSpikes(safePlatformPos, 0f);
        floorObjects[1] = spikesGround;
        spawnEntityAt(spikesGround,
                new GridPoint2(26, tileBounds.y - 34), false, true);

        // Create mini wall at edge (for lasers)
        Entity wall = WallFactory.createWall(10,0,1,5f,"");
        wall.setScale(1f,2f);
        floorObjects[2] = wall;
        spawnEntityAt(wall, new GridPoint2(26, tileBounds.y - 38),
                false, false);

        return floorObjects;
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

        // Spawn trap on wall
        Vector2 highPlatformSafePos = new Vector2(28, 17);
        Entity spikes3 = TrapFactory.createSpikes(highPlatformSafePos, 0f);
        spawnEntityAt(spikes3,
                new GridPoint2(60,17), false,  true);
        Entity spikes4 = TrapFactory.createSpikes(highPlatformSafePos, 0f);
        spawnEntityAt(spikes4,
                new GridPoint2(62,17), false,  true);
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
                .moveX(22f, 24f).moveY(18f, 25f)
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
                .moveX(8, 10.5f).moveY(tileBounds.y - 30, tileBounds.y - 30)
                .texture("images/flying_bat.atlas")
                .build();
        spawnEntityAt(horizontalBat, new GridPoint2(
                (int) safeBatBuilder.getSpawnX() * 2,
                (int) safeBatBuilder.getSpawnY()), true, true);
    }

    private void spawnBoss() {
        // Spawn position in grid coordinates
        GridPoint2 spawnPos = new GridPoint2(35, 9);

        // Convert to world coordinates
        Vector2 worldPos = terrain.tileToWorldPosition(spawnPos);

        // Create the boss entity
        Entity boss = EnemyFactory.createBossEnemy(player, worldPos);

        // Add BossLaserAttack to track the player and render the laser
        BossLaserAttack laserAttack = new BossLaserAttack(player);
        boss.addComponent(laserAttack);

        // Add event to automatically start shooting after spawn
        boss.getEvents().addListener("spawned", () -> {
            laserAttack.getPositions().clear();
        });

        // Add rendering component to display the laser
        boss.addComponent(new LaserRenderComponent());

        // Spawn the boss in the world
        spawnEntityAt(boss, spawnPos, true, true);
    }
    private void spawnDeathZone() {
        // Death zone at the start of level
        GridPoint2 spawnPos =  new GridPoint2(12,(tileBounds.y - 34));
        Entity deathZone = DeathZoneFactory.createDeathZone();
        deathZone.setScale(10,0.5f);
        deathZone.getComponent(ColliderComponent.class).setAsBoxAligned(deathZone.getScale().scl(0.8f),
                PhysicsComponent.AlignX.CENTER,
                PhysicsComponent.AlignY.BOTTOM);
        spawnEntityAt(deathZone, spawnPos, true,  false);

        // Death zone beneath lasers
        Entity deathZone2 = DeathZoneFactory.createDeathZone();
        deathZone2.setScale(100,0.5f);
        deathZone2.getComponent(ColliderComponent.class).setAsBoxAligned(deathZone2.getScale().scl(0.8f),
                PhysicsComponent.AlignX.CENTER,
                PhysicsComponent.AlignY.BOTTOM);
        spawnEntityAt(deathZone2, new GridPoint2(42, -3), false,  false);

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
        GridPoint2 buttonPlatformPos = new GridPoint2(57, tileBounds.y - 9);
        GridPoint2 buttonStartPos = new GridPoint2(59, 50);
        Vector2 offsetWorldButton = new Vector2(-10f, 0f);
        float speed = 5f;

        Entity buttonPlatform = PlatformFactory.createButtonTriggeredPlatform(offsetWorldButton, speed);
        buttonPlatform.setScale(1.5f, 0.5f);
        spawnEntityAt(buttonPlatform, buttonPlatformPos, false, false);
        logger.info("Moving platform spawned at {}", buttonPlatformPos);

        //start button
        Entity button = ButtonFactory.createButton(false, "platform", "left");
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

    /**
     * Spawns the comically huge button that ends the game.
     * TODO replace with Shane's captcha.
     */
    private void spawnEndgameButton() {
        GridPoint2 buttonPos = new GridPoint2(tileBounds.x, tileBounds.y / 2);
        Entity winGameButton = ButtonFactory.createButton(false, "platform", "left");
        winGameButton.setScale(2f, 5f);
        spawnEntityAt(winGameButton, buttonPos, true, true);

        winGameButton.getEvents().addListener("buttonToggled", (Boolean isPressed) -> {
            this.trigger("doorEntered"); // it's not a door but it changes level so
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
