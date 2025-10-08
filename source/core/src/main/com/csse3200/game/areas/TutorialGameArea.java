package com.csse3200.game.areas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.platforms.VolatilePlatformComponent;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.entities.factories.LadderFactory;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.rendering.parallax.ParallaxBackgroundComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.GridPoint2Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TutorialGameArea extends GameArea {
    private static final GridPoint2 mapSize = new GridPoint2(80,70);
    private static final float WALL_THICKNESS = 0.1f;
    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(1, 10);
    private static boolean keySpawned;
    private static final String[] gameTextures = {
        "images/box_boy_leaf.png",
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
        "images/box_boy_leaf.png",
        "images/button.png",
        "images/button_pushed.png",
        "images/blue_button_pushed.png",
        "images/blue_button.png",
        "images/drone.png",
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
        "images/terminal_on.png",
        "images/terminal_off.png",
        "images/plate.png",
        "images/plate-pressed.png",
        "images/mirror-cube-off.png",
        "images/mirror-cube-on.png",
        "images/cube.png",
        "images/heavy-cube.png",
        "images/laser-detector-off.png",
        "images/laser-detector-on.png",
        "images/laser-end.png",
        "images/upSignpost.png",
        "images/downSignpost.png",
        "images/rightSignpost.png",
        "images/leftSignpost.png",
        "images/signpost.png",
        "images/lost_hardware.png",
        "images/tutorials/jump.png",
        "images/tutorials/double_jump.png",
    };
    private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";
    private static final String[] musics = {backgroundMusic};
    private static final String[] gameSounds = {"sounds/Impact4.ogg",
        "sounds/chimesound.mp3"};
    private static final String[] gameTextureAtlases = {
        "images/PLAYER.atlas",
        "images/drone.atlas",
        "images/volatile_platform.atlas",
        "images/health-potion.atlas",
        "images/speed-potion.atlas",
        "images/flying_bat.atlas", // Bat sprites from https://todemann.itch.io/bat (see Wiki)
        "images/doors.atlas",
        "images/laser.atlas"
    };
    private static final Logger logger = LoggerFactory.getLogger(TutorialGameArea.class);
    private final TerrainFactory terrainFactory;

    public TutorialGameArea(TerrainFactory terrainFactory) {
        super();
        this.terrainFactory = terrainFactory;
    }
    protected void loadPrerequisites() {
        displayUI();
        spawnTerrain();
        createMinimap(ServiceLocator.getResourceService().getAsset("images/minimap_forest_area.png", Texture.class));
        playMusic();
    }

    protected void loadEntities() {
        cancelPlateDebouncers();
        keySpawned = false;
        spawnLadders();
//        spawnSignposts();
        spawnLowerLadderPressurePlate();
//        spawnUpperLadderPressurePlate();
//        spawnBoxOnlyPlate();
        spawnParallaxBackground();
        spawnFloorsAndPlatforms();
//        spawnVolatilePlatform();
        spawnDeathZone();
//        spawnWalls();
        spawnDoor();
        spawnButtons();
//        spawnObjectives();
//        spawnTerminals();
        spawnBoxes();
        spawnTutorialBats();
//        spawnCollectables();
        spawnTutorials();
         spawnUpgrade("glider", 9, 39);
         spawnUpgrade("jetpack", 76, 4);
    }

    private void spawnTutorials() {
        spawnEntityAt(TutorialFactory.createJumpTutorial(), new GridPoint2(6, 5), true, true);
        spawnEntityAt(TutorialFactory.createDoubleJumpTutorial(), new GridPoint2(11, 16), true, true);
    }

    private void spawnTerminals() {
        Entity terminal1 = CodexTerminalFactory.createTerminal(ServiceLocator.getCodexService().getEntry("test"));
        Entity terminal2 = CodexTerminalFactory.createTerminal(ServiceLocator.getCodexService().getEntry("test2"));
        Entity terminal3 = CodexTerminalFactory.createTerminal(ServiceLocator.getCodexService().getEntry("test3"));
        Entity terminal4 = CodexTerminalFactory.createTerminal(ServiceLocator.getCodexService().getEntry("test4"));
        Entity terminal5 = CodexTerminalFactory.createTerminal(ServiceLocator.getCodexService().getEntry("test5"));
        spawnEntityAt(terminal1, new GridPoint2(2, 4), true, true);
        spawnEntityAt(terminal2, new GridPoint2(6, 4), true, true);
        spawnEntityAt(terminal3, new GridPoint2(10, 4), true, true);
        spawnEntityAt(terminal4, new GridPoint2(14, 4), true, true);
        spawnEntityAt(terminal5, new GridPoint2(18, 4), true, true);

    }

    private void spawnBoxes() {
        Entity one = BoxFactory.createWeightedBox();
        one.addComponent(new TooltipSystem.TooltipComponent(
            "This might come in handy...",
            TooltipSystem.TooltipStyle.DEFAULT ));
        spawnEntityAt(one, new GridPoint2(4, 4), true, true);
    }

    private void spawnDeathZone() {
        GridPoint2 spawnPos =  new GridPoint2(12,-10);
        Entity deathZone = DeathZoneFactory.createDeathZone();
        spawnEntityAt(deathZone, spawnPos, true,  true);
    }

    private static final float PLATE_DEBOUNCE = 0.12f;
    // Lower Ladder dimensions
    private final int lowerLadderX = 13;
    private final int lowerLadderY = 19;
    private final int lowerLadderHeight = 18;
    private final int lowerLadderOffset = 13;
    private boolean isLowerLadderExtended = false;
    private boolean isLowerLadderSpawning = false;
    private final List<Entity> lowerLadderBottomSegments = new ArrayList<>();
    private Timer.Task lowerLadderTask;
    private boolean lowerPlateDown = false;
    private Timer.Task lowerPressDebounceTask, lowerReleaseDebounceTask;


    private void spawnLadders() {
        // Lower ladder
        lowerLadderBottomSegments.clear();
        isLowerLadderExtended = true;
        isLowerLadderSpawning = false;
        for(int i = lowerLadderOffset; i < lowerLadderHeight; i++) {
            GridPoint2 ladderPosition = new GridPoint2(lowerLadderX, (lowerLadderY + i));
            Entity ladder = LadderFactory.createStaticLadder();
            ladder.setScale(1f, 1f);
            spawnEntityAt(ladder, ladderPosition, false, false);

        }
    }

    private void spawnLowerLadderPressurePlate() {
        int x = 17;
        int y = 19;
        GridPoint2 lowerLadderPressurePlatePosition = new GridPoint2(x, y);
        Entity lowerLadderPressurePlate = PressurePlateFactory.createBoxOnlyPlate();
        lowerLadderPressurePlate.addComponent(new TooltipSystem.TooltipComponent(
            "Push to release ladder",
            TooltipSystem.TooltipStyle.DEFAULT ));
        spawnEntityAt(lowerLadderPressurePlate, lowerLadderPressurePlatePosition, true, true);

        // Ladder extends and retracts when activating pressure plate
        lowerLadderPressurePlate.getEvents().addListener("platePressed", () -> {
            // cancel any release confirmation that might be pending
            if (lowerReleaseDebounceTask != null) {
                lowerReleaseDebounceTask.cancel();
                lowerReleaseDebounceTask = null;
            }
            if (lowerPlateDown) return;

            // replace prior press debounce
            if (lowerPressDebounceTask != null) {
                lowerPressDebounceTask.cancel();
            }
            lowerPressDebounceTask = new Timer.Task() {
                @Override
                public void run() {
                    lowerPlateDown = true;
                    extendLowerLadder();
                    lowerPressDebounceTask = null;
                }
            };
            Timer.schedule(lowerPressDebounceTask, PLATE_DEBOUNCE);
        });

        lowerLadderPressurePlate.getEvents().addListener("plateReleased", () -> {
            // cancel any press confirmation that might be pending
            if (lowerPressDebounceTask != null) {
                lowerPressDebounceTask.cancel();
                lowerPressDebounceTask = null;
            }
            if (!lowerPlateDown) return;

            if (lowerReleaseDebounceTask != null) {
                lowerReleaseDebounceTask.cancel();
            }
            lowerReleaseDebounceTask = new Timer.Task() {
                @Override
                public void run() {
                    lowerPlateDown = false;

                    if (isLowerLadderSpawning && lowerLadderTask != null) {
                        lowerLadderTask.cancel();
                        lowerLadderTask = null;
                        isLowerLadderSpawning = false;
                    }
                    // retract only if not currently resetting level
                    if (isLowerLadderExtended && !isLowerLadderSpawning && !isResetting) {
                        retractLowerLadder();
                    }
                    lowerReleaseDebounceTask = null;
                }
            };
            Timer.schedule(lowerReleaseDebounceTask, PLATE_DEBOUNCE);
        });
    }

    private void extendLowerLadder() {
        if (!isLowerLadderExtended && lowerLadderBottomSegments.isEmpty() && !isLowerLadderSpawning) {
            isLowerLadderExtended = true;
            isLowerLadderSpawning = true;

            lowerLadderTask = new Timer.Task() {
                int rung = lowerLadderOffset - 1;

                @Override
                public void run() {
                    if (rung < 0) {
                        this.cancel();
                        lowerLadderTask = null;
                        isLowerLadderSpawning = false;
                        return;
                    }
                    GridPoint2 lowerLadderPosition = new GridPoint2(lowerLadderX, lowerLadderY + rung);
                    Entity lowerLadderRung = LadderFactory.createStaticLadder();
                    lowerLadderRung.setScale(1f, 1f);
                    spawnEntityAt(lowerLadderRung, lowerLadderPosition, false, false);
                    lowerLadderBottomSegments.add(lowerLadderRung);
                    rung--;
                }
            };
            // Initial delay, and delay between rungs (avoid partially spawned rungs)
            Timer.schedule(lowerLadderTask, 0.05f, 0.05f);
        }
    }

    private void retractLowerLadder() {
        isLowerLadderExtended = false;
        if (isResetting) return;

        // Delays disposal of bottom ladder rungs until next frame to avoid physics engine lock
        Gdx.app.postRunnable(() -> {
            for (Entity rung : lowerLadderBottomSegments) {
                rung.dispose();
                areaEntities.remove(rung);
            }
            isLowerLadderSpawning = false;
            isLowerLadderExtended = false;
            lowerLadderBottomSegments.clear();
        });
    }

    private void cancelPlateDebouncers() {
        if (lowerPressDebounceTask != null) {
            lowerPressDebounceTask.cancel();
            lowerPressDebounceTask = null;
        }
        if (lowerReleaseDebounceTask != null) {
            lowerReleaseDebounceTask.cancel();
            lowerReleaseDebounceTask = null;
        }
    }

    private void spawnFloorsAndPlatforms(){
        spawnFloors();

        spawnElevatedPlatforms();
    }

    private void spawnFloors() {

        GridPoint2 groundFloor1Pos = new GridPoint2(-20, -20);
        Entity groundFloor1 = FloorFactory.createGroundFloor();
        groundFloor1.setScale(16, 12);
        spawnEntityAt(groundFloor1, groundFloor1Pos, false, false);

//        GridPoint2 groundFloor2Pos = new GridPoint2(15, -20);
//        Entity groundFloor2 = FloorFactory.createGroundFloor();
//        groundFloor2.setScale(25f, 12f);
//        spawnEntityAt(groundFloor2, groundFloor2Pos, false, false);

        GridPoint2 groundFloor3Pos = new GridPoint2(70, -20);
        Entity groundFloor3 = FloorFactory.createGroundFloor();
        groundFloor3.setScale(25, 12);
        spawnEntityAt(groundFloor3, groundFloor3Pos, false, false);

        GridPoint2 doorFloorPos = new GridPoint2(62, 51);
        Entity gateFloor = FloorFactory.createStaticFloor();
        gateFloor.setScale(5f, 0.8f);
        spawnEntityAt(gateFloor, doorFloorPos, false, false);

        GridPoint2 puzzleGroundPos = new GridPoint2(15, 32);
        Entity puzzleGround = FloorFactory.createStaticFloor();
        puzzleGround.setScale(8,1);
        spawnEntityAt(puzzleGround, puzzleGroundPos, false, false);

    }

    private void spawnElevatedPlatforms() {

        // steps up to bottom of ladder
        GridPoint2 step1Pos = new GridPoint2(8,6);
        Entity step1 = PlatformFactory.createStaticPlatform();
        step1.setScale(2,0.5f);
        spawnEntityAt(step1, step1Pos,false, false);

        GridPoint2 step2Pos = new GridPoint2(2,9);
        Entity step2 = PlatformFactory.createStaticPlatform();
        step2.setScale(1.5f,0.5f);
        spawnEntityAt(step2, step2Pos,false, false);

        GridPoint2 step3Pos = new GridPoint2(8,12);
        Entity step3 = PlatformFactory.createStaticPlatform();
        step3.setScale(1.8f,0.5f);
        spawnEntityAt(step3, step3Pos,false, false);

        GridPoint2 step4Pos = new GridPoint2(12,18);
        Entity step4 = PlatformFactory.createStaticPlatform();
        step4.setScale(3.5f,0.5f);
        spawnEntityAt(step4, step4Pos,false, false);


        // Top of ladder platform
        GridPoint2 step6Pos = new GridPoint2(8,38);
        Entity step6 = PlatformFactory.createStaticPlatform();
        step6.setScale(5f,0.5f);
        spawnEntityAt(step6, step6Pos,false, false);


//        GridPoint2 step7Pos = new GridPoint2(45,18);
//        Entity step7 = PlatformFactory.createStaticPlatform();
//        step7.setScale(3.5f,0.5f);
//        spawnEntityAt(step7, step7Pos,false, false);

//        RIGHT PATH
        //GridPoint2 step8Pos = new GridPoint2(58,18);
        // Entity step8 = PlatformFactory.createStaticPlatform();
        // step8.setScale(2f,0.5f);
        // spawnEntityAt(step8, step8Pos,false, false);

//        // MOVING PLATFORM WITH BUTTONS
//        GridPoint2 buttonPlatformPos = new GridPoint2(55, 18);
//        Vector2 offsetWorldButton = new Vector2(9f, 0f);
//        float speedButton = 2f;
//
//        Entity buttonPlatform = PlatformFactory.createButtonTriggeredPlatform(offsetWorldButton, speedButton);
//        buttonPlatform.setScale(2f, 0.5f);
//        spawnEntityAt(buttonPlatform, buttonPlatformPos, false, false);
//        logger.info("Moving platform spawned at {}", buttonPlatformPos);
//
//        //start button
//        Entity buttonStart = ButtonFactory.createButton(false, "platform", "right");
//        buttonStart.addComponent(new TooltipSystem.TooltipComponent(
//            "Platform Button\nPress E to interact",
//            TooltipSystem.TooltipStyle.DEFAULT
//        ));
//        GridPoint2 buttonStartPos = new GridPoint2(56, 23);
//        spawnEntityAt(buttonStart, buttonStartPos, true, true);
//        logger.info("Platform button spawned at {}", buttonStartPos);
//
//        buttonStart.getEvents().addListener("buttonToggled", (Boolean isPressed) -> {
//            if (isPressed) {
//                logger.info("Button pressed — activating platform");
//                buttonPlatform.getEvents().trigger("activatePlatform");
//            } else {
//                logger.info("Button unpressed — deactivating platform");
//                buttonPlatform.getEvents().trigger("deactivatePlatform");
//            }
//        });
//
//        //end button (where platform stops)
//        Entity buttonEnd = ButtonFactory.createButton(false, "platform", "left");
//        buttonEnd.addComponent(new TooltipSystem.TooltipComponent(
//            "Platform Button \nPress E to interact",
//            TooltipSystem.TooltipStyle.DEFAULT
//        ));
//        GridPoint2 buttonEndPos = new GridPoint2(76, 23);
//        spawnEntityAt(buttonEnd, buttonEndPos, true, true);
//        logger.info("Platform button spawned at {}", buttonEndPos);
//
//        buttonEnd.getEvents().addListener("buttonToggled", (Boolean isPressed) -> {
//            if (isPressed) {
//                logger.info("Button pressed — activating platform");
//                buttonPlatform.getEvents().trigger("activatePlatform");
//            } else {
//                logger.info("Button unpressed — deactivating platform");
//                buttonPlatform.getEvents().trigger("deactivatePlatform");
//            }
//        });

//        // LEFT PATH
//        GridPoint2 moving1Pos = new GridPoint2(38,26);
//        Vector2 offsetWorld  = new Vector2(0f, 4f);
//        float speed = 2f;
//        Entity moving1 = PlatformFactory.createMovingPlatform(offsetWorld,speed);
//        moving1.setScale(2f,0.5f);
//        spawnEntityAt(moving1, moving1Pos,false, false);
//
//        GridPoint2 removeThis1 = new GridPoint2(48,35);
//        Entity removeThis = PlatformFactory.createStaticPlatform();
//        removeThis.setScale(2f,0.5f);
//        spawnEntityAt(removeThis, removeThis1,false, false);
//
//        GridPoint2 step9Pos = new GridPoint2(57,35);
//        Entity step9 = PlatformFactory.createStaticPlatform();
//        step9.setScale(5f,0.5f);
//        spawnEntityAt(step9, step9Pos,false, false);
//
//
//        GridPoint2 step11Pos = new GridPoint2(58,53);
//        Entity step11 = PlatformFactory.createStaticPlatform();
//        step11.setScale(1.8f,0.5f);
//        spawnEntityAt(step11, step11Pos,false, false);
//
//        GridPoint2 step12Pos = new GridPoint2(52,48);
//        Entity step12 = PlatformFactory.createStaticPlatform();
//        step12.setScale(3.5f,0.5f);
//        spawnEntityAt(step12, step12Pos,false, false);
//
//        GridPoint2 step13Pos = new GridPoint2(70,48);
//        Entity step13 = PlatformFactory.createStaticPlatform();
//        step13.setScale(2f,0.5f);
//        spawnEntityAt(step13, step13Pos,false, false);
    }

    private void spawnTutorialBats() {
        // Bat 1
        BoxFactory.AutonomousBoxBuilder batBuilder1 = new BoxFactory.AutonomousBoxBuilder();
        Entity horizontalBat1 = batBuilder1
            .moveX(35f, 40f).moveY(15f, 15f)
            .texture("images/flying_bat.atlas")
            .speed(2f)
            .build();
        spawnEntityAt(horizontalBat1, new GridPoint2(
                (int) batBuilder1.getSpawnX(),
                (int) batBuilder1.getSpawnY()),
            true, true);

        // Bat 2
        BoxFactory.AutonomousBoxBuilder batBuilder2 = new BoxFactory.AutonomousBoxBuilder();
        Entity horizontalBat2 = batBuilder2
            .moveX(35f, 40f).moveY(23f, 23f)
            .texture("images/flying_bat.atlas")
            .speed(3.5f)
            .build();
        spawnEntityAt(horizontalBat2, new GridPoint2(
                (int) batBuilder2.getSpawnX(),
                (int) batBuilder2.getSpawnY()),
            true, true);

        // Bat 3
        BoxFactory.AutonomousBoxBuilder batBuilder3 = new BoxFactory.AutonomousBoxBuilder();
        Entity horizontalBat3 = batBuilder3
            .moveX(35f, 40f).moveY(36f, 36f)
            .texture("images/flying_bat.atlas")
            .speed(5f)
            .build();
        spawnEntityAt(horizontalBat3, new GridPoint2(
                (int) batBuilder3.getSpawnX(),
                (int) batBuilder3.getSpawnY()),
            true, true);

        // Bat 4
        BoxFactory.AutonomousBoxBuilder batBuilder4 = new BoxFactory.AutonomousBoxBuilder();
        Entity horizontalBat4 = batBuilder4
            .moveX(35f, 40f).moveY(50f, 50f)
            .texture("images/flying_bat.atlas")
            .speed(6f)
            .build();
        spawnEntityAt(horizontalBat4, new GridPoint2(
                (int) batBuilder4.getSpawnX(),
                (int) batBuilder4.getSpawnY()),
            true, true);
    }

    public void spawnDoor() {
        Entity door = ObstacleFactory.createDoor("key:door", this);
        door.setScale(1, 2);
        door.addComponent(new TooltipSystem.TooltipComponent("Unlock the door with the key", TooltipSystem.TooltipStyle.DEFAULT));
        //door.getComponent(DoorComponent.class).openDoor();
        spawnEntityAt(door, new GridPoint2(63,53), true, true);
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
        ui.addComponent(new GameAreaDisplay("Tutorial"));
        ui.addComponent(new TooltipSystem.TooltipDisplay());
        spawnEntity(ui);
    }

    private void spawnSignposts(){
        Entity rightSign = SignpostFactory.createSignpost("right");
        spawnEntityAt(rightSign, new GridPoint2(8,4), true, false);

        Entity upSign = SignpostFactory.createSignpost("up");
        spawnEntityAt(upSign, new GridPoint2(55,4), true, false);

        Entity leftSign = SignpostFactory.createSignpost("left");
        spawnEntityAt(leftSign, new GridPoint2(29,36), true, false);
    }
    private void spawnTerrain() {
        // Need to decide how large each area is going to be
        terrain = createDefaultTerrain();
        spawnEntity(new Entity().addComponent(terrain));

        // Terrain walls
        float tileSize = terrain.getTileSize();
        GridPoint2 tileBounds = terrain.getMapBounds(0);
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
//        spawnEntityAt(ObstacleFactory.createWall(worldBounds.x, WALL_THICKNESS),
//                new GridPoint2(0, 0), false, false);
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

    private void spawnBoxOnlyPlate() {
        Entity pressurePlatePlatform = PlatformFactory.createPressurePlatePlatform();
        pressurePlatePlatform.setScale(2f,0.5f);
        spawnEntityAt(pressurePlatePlatform, new GridPoint2(32,17), true, true);

        Entity plate = PressurePlateFactory.createBoxOnlyPlate();
        plate.addComponent(new TooltipSystem.TooltipComponent("Platform Plate\nPress to reveal platform", TooltipSystem.TooltipStyle.DEFAULT));
        spawnEntityAt(plate, new GridPoint2(24, 13), true, true);

        pressurePlatePlatform.getComponent(VolatilePlatformComponent.class).linkToPlate(plate);
    }

    private TerrainComponent createDefaultTerrain() {
        // Use empty texture for invisible terrain grid
        final ResourceService resourceService = ServiceLocator.getResourceService();
        TextureRegion emptyTile = new TextureRegion(resourceService.getAsset("images/empty.png", Texture.class));

        GridPoint2 tilePixelSize = new GridPoint2(emptyTile.getRegionWidth(), emptyTile.getRegionHeight());
        TiledMap tiledMap = terrainFactory.createDefaultTiles(tilePixelSize, emptyTile, emptyTile, emptyTile, emptyTile, mapSize);
        return terrainFactory.createInvisibleFromTileMap(0.5f, tiledMap, tilePixelSize);
    }
    private void spawnVolatilePlatform(){
        GridPoint2 volatile1Pos = new GridPoint2(38,21);
        Entity volatile1 = PlatformFactory.createVolatilePlatform(2f,1.5f);
        volatile1.setScale(2f,0.5f);
        spawnEntityAt(volatile1, volatile1Pos,false, false);

        GridPoint2 volatile2Pos = new GridPoint2(32,24);
        Entity volatile2 = PlatformFactory.createVolatilePlatform(2f,1.5f);
        volatile2.setScale(1.8f,0.5f);
        spawnEntityAt(volatile2, volatile2Pos,false, false);

        GridPoint2 volatile3Pos = new GridPoint2(45,55);
        Entity volatile3 = PlatformFactory.createVolatilePlatform(2f,1.5f);
        volatile3.setScale(1.8f,0.5f);
        spawnEntityAt(volatile3, volatile3Pos,false, false);
    }

    private void spawnButtons() {
        Entity button2 = ButtonFactory.createButton(false, "door", "left");
        button2.addComponent(new TooltipSystem.TooltipComponent("Door Button\nPress E to interact", TooltipSystem.TooltipStyle.DEFAULT));
        spawnEntityAt(button2, new GridPoint2(79 ,4), true,  true);

        //listener to spawn key when door button pushed
        button2.getEvents().addListener("buttonToggled", (Boolean isPushed) -> {
            if (isPushed && !keySpawned) {
                spawnKey();
                keySpawned = true;
            }
        });
    }

    //    public void spawnPlayerUpgrades() {
//        Entity dashUpgrade = CollectableFactory.createDashUpgrade();
//        spawnEntityAt(dashUpgrade, new GridPoint2(1,37), true,  true);
//    }
    public void spawnKey() {
        Entity key = CollectableFactory.createCollectable("key:door");
        spawnEntityAt(key, new GridPoint2(68,53), true, true);
    }

    private void spawnObjectives() {
        // Large, invisible sensors — easy to grab, no textures.
        // IDs chosen to match the ObjectiveTab banner map.
        Gdx.app.log("LevelOne", "Spawning objectives…");
//        spawnEntityAt(CollectableFactory.createObjective("dash", 2.0f, 2.0f),    new GridPoint2(14, 19), true, true);
//        spawnEntityAt(CollectableFactory.createObjective("glider", 2.0f, 2.0f),  new GridPoint2(15, 17), true, true);
//        spawnEntityAt(CollectableFactory.createObjective("jetpack", 2.0f, 2.0f), new GridPoint2(18, 17), true, true);
        spawnEntityAt(CollectableFactory.createObjective("keycard", 2.0f, 2.0f), new GridPoint2(1, 3), true, true);
        spawnEntityAt(CollectableFactory.createObjective("door", 2.0f, 2.0f), new GridPoint2(1, 3), true, true);
        spawnEntityAt(CollectableFactory.createObjective("tutorial", 2.0f, 2.0f), new GridPoint2(1, 3), true, true);
    }


    public void spawnUpgrade(String upgradeID, int posx, int posy) {
//        if (upgradeID == "dash") {
//            Entity upgrade = CollectableFactory.createDashUpgrade();
//            upgrade.addComponent(new TooltipSystem.TooltipComponent("Collect Dash Upgrade", TooltipSystem.TooltipStyle.SUCCESS));
//            spawnEntityAt(upgrade, new GridPoint2(posx, posy), true, true);
//        }

        if (upgradeID == "glider") {
            Entity upgrade = CollectableFactory.createGlideUpgrade();
            upgrade.addComponent(new TooltipSystem.TooltipComponent("Collect Glider Upgrade", TooltipSystem.TooltipStyle.SUCCESS));
            spawnEntityAt(upgrade, new GridPoint2(posx, posy), true, true);
        }
        if (upgradeID == "jetpack") {
            Entity upgrade = CollectableFactory.createJetpackUpgrade();
            spawnEntityAt(upgrade, new GridPoint2(posx, posy), true, true);
        }
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
        isResetting = true;
        retractLowerLadder();
        cancelPlateDebouncers();
        super.dispose();
        ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class).stop();
        this.unloadAssets();
        isResetting = false;
    }
}
