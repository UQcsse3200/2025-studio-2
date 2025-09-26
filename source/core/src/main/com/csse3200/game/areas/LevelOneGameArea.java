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
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.entities.factories.LadderFactory;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.lighting.LightingDefaults;
import com.csse3200.game.rendering.parallax.ParallaxBackgroundComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.GridPoint2Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class LevelOneGameArea extends GameArea {
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
            "images/pressure_plate_unpressed.png",
            "images/pressure_plate_pressed.png",
            "images/mirror-cube-off.png",
            "images/mirror-cube-on.png"
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
    private static final Logger logger = LoggerFactory.getLogger(LevelOneGameArea.class);
    private final TerrainFactory terrainFactory;

    public LevelOneGameArea(TerrainFactory terrainFactory) {
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
        keySpawned = false;
        spawnLadders();
        spawnLowerLadderPressurePlate();
        spawnUpperLadderPressurePlate();
        spawnParallaxBackground();
        spawnFloorsAndPlatforms();
        spawnVolatilePlatform();
        spawnDeathZone();
        spawnWalls();
        spawnDoor();
        //spawnBoxOnlyPlate();
        spawnUpgrade("dash", 9, 6);
        spawnUpgrade("glider", 7, 6);
        spawnUpgrade("jetpack", 5, 6);
        //spawnSecurityCams();
        spawnButtons();
        spawnTraps();
        //spawnPlatformBat();
        spawnLevelOneBatRoom();
        spawnPlayerUpgrades();
        spawnPotion("health", 60, 28);
        spawnPotion("health", 10, 15);
        spawnPotion("dash", 72, 12);
        spawnObjectives();
        spawnBoxes();
        spawnLasers();
    }

    private void spawnBoxes() {
        Entity testing = BoxFactory.createWeightedBox();
        spawnEntityAt(testing, new GridPoint2(15, 15), true, true);

        Entity e1 = BoxFactory.createReflectorBox();
        spawnEntityAt(e1, new GridPoint2(28, 15), true, true);
    }
    private void spawnLasers() {
        Entity e = LaserFactory.createLaserEmitter(-45f);
        spawnEntityAt(e, new GridPoint2(40, 12), true, true);
    }

    private void spawnDeathZone() {
        GridPoint2 spawnPos =  new GridPoint2(12,-10);
        Entity deathZone = DeathZoneFactory.createDeathZone();
        spawnEntityAt(deathZone, spawnPos, true,  true);
    }

    private void spawnWalls(){
        GridPoint2 leftWallPos = new GridPoint2(25,4);
        Entity leftWall = WallFactory.createWall(25,0,1,20f,"");
        leftWall.setScale(1,4.5f);
        spawnEntityAt(leftWall, leftWallPos, false, false);

        GridPoint2 rightWallPos = new GridPoint2(75,4);
        Entity rightWall = WallFactory.createWall(25,0,1,20f,"");
        rightWall.setScale(2.5f,7.5f);
        spawnEntityAt(rightWall, rightWallPos, false, false);
    }

    // Upper Ladder dimensions
    private final int upperLadderX = 59;
    private final int upperLadderY = 36;
    private final int upperLadderHeight = 16;
    private final int upperLadderOffset = 11;
    private boolean isUpperLadderExtended = true;
    // Lower Ladder dimensions
    private final int lowerLadderX = 52;
    private final int lowerLadderY = 4;
    private final int lowerLadderHeight = 18;
    private final int lowerLadderOffset = 13;
    private boolean isLowerLadderExtended = true;

    private void spawnLadders() {
        // Upper ladder
        isUpperLadderExtended = false;
        for(int i = upperLadderOffset; i < upperLadderHeight; i++) {
            GridPoint2 ladderPos = new GridPoint2(upperLadderX, (upperLadderY + i));
            Entity ladder = LadderFactory.createStaticLadder();
            ladder.setScale(1f, 1f);
            spawnEntityAt(ladder, ladderPos, false, false);
        }
        // Lower ladder
        isLowerLadderExtended = false;
        for(int i = lowerLadderOffset; i < lowerLadderHeight; i++) {
            GridPoint2 ladderPosition = new GridPoint2(lowerLadderX, (lowerLadderY + i));
            Entity ladder = LadderFactory.createStaticLadder();
            ladder.setScale(1f, 1f);
            spawnEntityAt(ladder, ladderPosition, false, false);
        }
    }
    private void spawnUpperLadderPressurePlate() {
        int x = 63;
        int y = 36;
        GridPoint2 upperLadderPressurePlatePosition = new GridPoint2(x, y);
        Entity upperLadderPressurePlate = PressurePlateFactory.createBoxOnlyPlate();
        upperLadderPressurePlate.addComponent(new TooltipSystem.TooltipComponent(
                "Push to release ladder",
                TooltipSystem.TooltipStyle.DEFAULT ));
        spawnEntityAt(upperLadderPressurePlate, upperLadderPressurePlatePosition, true, true);

        // Ladder extends on first press (not reversible)
        upperLadderPressurePlate.getEvents().addListener("plateToggled", (Boolean pressed) -> {
            if (pressed && !isUpperLadderExtended) {
                isUpperLadderExtended = true;
                for (int i = upperLadderOffset - 1; i >= 0; i--) {
                    final int rung = i;
                    Timer.schedule(new Timer.Task(){
                        @Override
                        public void run() {
                            GridPoint2 upperLadderPosition = new GridPoint2(upperLadderX, (upperLadderY + rung));
                            Entity upperLadder = LadderFactory.createStaticLadder();
                            upperLadder.setScale(1f, 1f);
                            spawnEntityAt(upperLadder, upperLadderPosition, false, false);
                        }
                    }, 0.05f * (upperLadderOffset - 1 - i));
                }
            }
        });
    }
    private void spawnLowerLadderPressurePlate() {
        int x = 73;
        int y = 4;

        GridPoint2 lowerLadderPressurePlatePosition = new GridPoint2(x, y);
        Entity lowerLadderPressurePlate = PressurePlateFactory.createBoxOnlyPlate();
        lowerLadderPressurePlate.addComponent(new TooltipSystem.TooltipComponent(
                "Push to release ladder",
                TooltipSystem.TooltipStyle.DEFAULT ));
        spawnEntityAt(lowerLadderPressurePlate, lowerLadderPressurePlatePosition, true, true);

        // Ladder extends on first press (not reversible)
        lowerLadderPressurePlate.getEvents().addListener("plateToggled", (Boolean pressed) -> {
            if (pressed && !isLowerLadderExtended) {
                isLowerLadderExtended = true;
                for (int i = lowerLadderOffset - 1; i >= 0; i--) {
                    final int rung = i;
                    Timer.schedule(new Timer.Task(){
                        @Override
                        public void run() {
                            GridPoint2 lowerLadderPosition = new GridPoint2(lowerLadderX, (lowerLadderY + rung));
                            Entity ladder = LadderFactory.createStaticLadder();
                            ladder.setScale(1f, 1f);
                            spawnEntityAt(ladder, lowerLadderPosition, false, false);
                        }
                    }, 0.05f * (lowerLadderOffset - 1 - i));
                }
            }
        });
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

        GridPoint2 groundFloor2Pos = new GridPoint2(15, -20);
        Entity groundFloor2 = FloorFactory.createGroundFloor();
        groundFloor2.setScale(25f, 12f);
        spawnEntityAt(groundFloor2, groundFloor2Pos, false, false);

        GridPoint2 groundFloor3Pos = new GridPoint2(70, -20);
        Entity groundFloor3 = FloorFactory.createGroundFloor();
        groundFloor3.setScale(25, 12);
        spawnEntityAt(groundFloor3, groundFloor3Pos, false, false);

        GridPoint2 gateFloorPos = new GridPoint2(33, 60);
        Entity gateFloor = FloorFactory.createStaticFloor();
        gateFloor.setScale(5f, 0.8f);
        spawnEntityAt(gateFloor, gateFloorPos, false, false);

        GridPoint2 puzzleGroundPos = new GridPoint2(0, 32);
        Entity puzzleGround = FloorFactory.createStaticFloor();
        puzzleGround.setScale(16f,2f);
        spawnEntityAt(puzzleGround, puzzleGroundPos, false, false);

    }

    private void spawnElevatedPlatforms() {
        GridPoint2 step1Pos = new GridPoint2(21,6);
        Entity step1 = PlatformFactory.createStaticPlatform();
        step1.setScale(2,0.5f);
        spawnEntityAt(step1, step1Pos,false, false);

        GridPoint2 step2Pos = new GridPoint2(15,9);
        Entity step2 = PlatformFactory.createStaticPlatform();
        step2.setScale(1.5f,0.5f);
        spawnEntityAt(step2, step2Pos,false, false);

        GridPoint2 step3Pos = new GridPoint2(22,12);
        Entity step3 = PlatformFactory.createStaticPlatform();
        step3.setScale(1.8f,0.5f);
        spawnEntityAt(step3, step3Pos,false, false);

//        THESE TWO TO BE REPLACED WITH LADDERS
        GridPoint2 step4Pos = new GridPoint2(52,23);
        Entity step4 = PlatformFactory.createStaticPlatform();
        step4.setScale(1.8f,0.5f);
        spawnEntityAt(step4, step4Pos,false, false);

        //GridPoint2 step6Pos = new GridPoint2(42,12);
        //Entity step6 = PlatformFactory.createStaticPlatform();
        //step6.setScale(1.8f,0.5f);
        //spawnEntityAt(step6, step6Pos,false, false);
//      ^

        GridPoint2 step7Pos = new GridPoint2(45,18);
        Entity step7 = PlatformFactory.createStaticPlatform();
        step7.setScale(3.5f,0.5f);
        spawnEntityAt(step7, step7Pos,false, false);

//        RIGHT PATH
        //GridPoint2 step8Pos = new GridPoint2(58,18);
       // Entity step8 = PlatformFactory.createStaticPlatform();
       // step8.setScale(2f,0.5f);
       // spawnEntityAt(step8, step8Pos,false, false);

        // MOVING PLATFORM WITH BUTTONS
        GridPoint2 buttonPlatformPos = new GridPoint2(55, 18);
        Vector2 offsetWorldButton = new Vector2(9f, 0f);
        float speedButton = 2f;

        Entity buttonPlatform = PlatformFactory.createButtonTriggeredPlatform(offsetWorldButton, speedButton);
        buttonPlatform.setScale(2f, 0.5f);
        spawnEntityAt(buttonPlatform, buttonPlatformPos, false, false);
        logger.info("Moving platform spawned at {}", buttonPlatformPos);

        //start button
        Entity buttonStart = ButtonFactory.createButton(false, "platform", "right");
        buttonStart.addComponent(new TooltipSystem.TooltipComponent(
                "Platform Button\nPress E to interact",
                TooltipSystem.TooltipStyle.DEFAULT
        ));
        GridPoint2 buttonStartPos = new GridPoint2(56, 23);
        spawnEntityAt(buttonStart, buttonStartPos, true, true);
        logger.info("Platform button spawned at {}", buttonStartPos);

        buttonStart.getEvents().addListener("buttonToggled", (Boolean isPressed) -> {
            if (isPressed) {
                logger.info("Button pressed — activating platform");
                buttonPlatform.getEvents().trigger("activatePlatform");
            } else {
                logger.info("Button unpressed — deactivating platform");
                buttonPlatform.getEvents().trigger("deactivatePlatform");
            }
        });

        //end button (where platform stops)
        Entity buttonEnd = ButtonFactory.createButton(false, "platform", "left");
        buttonEnd.addComponent(new TooltipSystem.TooltipComponent(
                "Platform Button \nPress E to interact",
                TooltipSystem.TooltipStyle.DEFAULT
        ));
        GridPoint2 buttonEndPos = new GridPoint2(76, 23);
        spawnEntityAt(buttonEnd, buttonEndPos, true, true);
        logger.info("Platform button spawned at {}", buttonEndPos);

        buttonEnd.getEvents().addListener("buttonToggled", (Boolean isPressed) -> {
            if (isPressed) {
                logger.info("Button pressed — activating platform");
                buttonPlatform.getEvents().trigger("activatePlatform");
            } else {
                logger.info("Button unpressed — deactivating platform");
                buttonPlatform.getEvents().trigger("deactivatePlatform");
            }
        });

        // LEFT PATH
        GridPoint2 moving1Pos = new GridPoint2(38,26);
        Vector2 offsetWorld  = new Vector2(0f, 4f);
        float speed = 2f;
        Entity moving1 = PlatformFactory.createMovingPlatform(offsetWorld,speed);
        moving1.setScale(2f,0.5f);
        spawnEntityAt(moving1, moving1Pos,false, false);

        GridPoint2 removeThis1 = new GridPoint2(48,35);
        Entity removeThis = PlatformFactory.createStaticPlatform();
        removeThis.setScale(2f,0.5f);
        spawnEntityAt(removeThis, removeThis1,false, false);

        GridPoint2 step9Pos = new GridPoint2(57,35);
        Entity step9 = PlatformFactory.createStaticPlatform();
        step9.setScale(4f,0.5f);
        spawnEntityAt(step9, step9Pos,false, false);


        GridPoint2 step11Pos = new GridPoint2(58,53);
        Entity step11 = PlatformFactory.createStaticPlatform();
        step11.setScale(1.8f,0.5f);
        spawnEntityAt(step11, step11Pos,false, false);

        GridPoint2 step12Pos = new GridPoint2(52,48);
        Entity step12 = PlatformFactory.createStaticPlatform();
        step12.setScale(3.5f,0.5f);
        spawnEntityAt(step12, step12Pos,false, false);

        GridPoint2 step13Pos = new GridPoint2(70,48);
        Entity step13 = PlatformFactory.createStaticPlatform();
        step13.setScale(2f,0.5f);
        spawnEntityAt(step13, step13Pos,false, false);
    }

    public void spawnDoor() {
        Entity door = ObstacleFactory.createDoor("key:door", this);
        door.setScale(1, 2);
        door.addComponent(new TooltipSystem.TooltipComponent("Unlock the door with the key", TooltipSystem.TooltipStyle.DEFAULT));
        //door.getComponent(DoorComponent.class).openDoor();
        spawnEntityAt(door, new GridPoint2(35,62), true, true);
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
        ui.addComponent(new GameAreaDisplay("Level one Game Area"));
        ui.addComponent(new TooltipSystem.TooltipDisplay());
        spawnEntity(ui);
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
        Entity plate = PressurePlateFactory.createBoxOnlyPlate();
        spawnEntityAt(plate, new GridPoint2(6, 5), true, true);

        plate.getEvents().addListener("plateToggled", (Boolean pressed) -> {
            if (pressed) {

            }
        });
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
    private void spawnTraps() {
        Vector2 safeSpotStart = new Vector2(2, 3);

        Entity spikesLeft1 = TrapFactory.createSpikes(safeSpotStart, 90f);
        spawnEntityAt(spikesLeft1, new GridPoint2(24,10), true,  true);
        Entity spikesLeft2 = TrapFactory.createSpikes(safeSpotStart, 90f);
        spawnEntityAt(spikesLeft2, new GridPoint2(24,8), true,  true);

        Entity spikesDown = TrapFactory.createSpikes(safeSpotStart, 180f);
        spawnEntityAt(spikesDown, new GridPoint2(16,8), true,  true);

        Vector2 safeSpotCamera = new Vector2(24, 10);

        Entity spikesUp = TrapFactory.createSpikes(safeSpotCamera, 0f);
        spawnEntityAt(spikesUp, new GridPoint2(60,19), true,  true);

        Entity spikesRight = TrapFactory.createSpikes(safeSpotCamera, 270f);
        spawnEntityAt(spikesRight, new GridPoint2(32,34), true,  true);
    }
    private void spawnButtons() {
        Entity button2 = ButtonFactory.createButton(false, "door", "left");
        button2.addComponent(new TooltipSystem.TooltipComponent("Door Button\nPress E to interact", TooltipSystem.TooltipStyle.DEFAULT));
        spawnEntityAt(button2, new GridPoint2(79 ,20), true,  true);

        //listener to spawn key when door button pushed
        button2.getEvents().addListener("buttonToggled", (Boolean isPushed) -> {
            if (isPushed && !keySpawned) {
                spawnKey();
                keySpawned = true;
            }
        });
    }

    public void spawnPlayerUpgrades() {
        Entity dashUpgrade = CollectableFactory.createDashUpgrade();
        spawnEntityAt(dashUpgrade, new GridPoint2(1,37), true,  true);
    }
    public void spawnKey() {
        Entity key = CollectableFactory.createCollectable("key:door");
        spawnEntityAt(key, new GridPoint2(46,56), true, true);
    }
    public void spawnPotion(String type, int x, int y) {
        Entity potion = CollectableFactory.createCollectable("potion:" + type);
        spawnEntityAt(potion, new GridPoint2(x,y), true, true);
    }

    private void spawnSecurityCams() {
        // see the LightFactory class for more details on spawning these
        Entity securityLight1 = SecurityCameraFactory.createSecurityCamera(player, LightingDefaults.ANGULAR_VEL, "1");
        spawnEntityAt(securityLight1, new GridPoint2(48, 17), true, true);

        Entity securityLight2 = SecurityCameraFactory.createSecurityCamera(player, LightingDefaults.ANGULAR_VEL, 270f, "2");
        spawnEntityAt(securityLight2, new GridPoint2(74, 13), true, true);
    }
    private void spawnPlatformBat() {
        BoxFactory.AutonomousBoxBuilder platformBatBuilder = new BoxFactory.AutonomousBoxBuilder();
        Entity horizontalPlatformBat = platformBatBuilder
                .moveX(15f, 22f).moveY(4f, 4f)
                .texture("images/flying_bat.atlas")
                .tooltip("Beware! Bats bite and knock you back. Stay clear!",
                        TooltipSystem.TooltipStyle.WARNING)
                .build();
        spawnEntityAt(horizontalPlatformBat, new GridPoint2(
                (int) platformBatBuilder.getSpawnX() * 2,
                (int) platformBatBuilder.getSpawnY()), true, true);
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

    private void spawnLevelOneBatRoom() {
        int offsetX = 0;
        int offsetY = 0;

        BoxFactory.AutonomousBoxBuilder batBuilder1 = new BoxFactory.AutonomousBoxBuilder();
        Entity lowHorizontalBat = batBuilder1
                .moveX(3f + offsetX, 10f + offsetX).moveY(36f + offsetY, 36f + offsetY)
                .texture("images/flying_bat.atlas")
                .speed(4f).build();
        spawnEntityAt(lowHorizontalBat, new GridPoint2(
                        (int) batBuilder1.getSpawnX() + offsetX,
                        (int) batBuilder1.getSpawnY() + offsetY),
                true, true);

        BoxFactory.AutonomousBoxBuilder batBuilder2 = new BoxFactory.AutonomousBoxBuilder();
        Entity highHorizontalBat1 = batBuilder2
                .moveX(1f + offsetX, 10f + offsetX).moveY(46f + offsetY, 46f + offsetY)
                .texture("images/flying_bat.atlas")
                .build();
        spawnEntityAt(highHorizontalBat1, new GridPoint2(
                        (int) batBuilder2.getSpawnX() + offsetX,
                        (int) batBuilder2.getSpawnY() + offsetY),
                true, true);

        BoxFactory.AutonomousBoxBuilder batBuilder3 = new BoxFactory.AutonomousBoxBuilder();
        Entity highHorizontalBat2 = batBuilder3
                .moveX(1f + offsetX, 10f + offsetX).moveY(53f + offsetY, 53f + offsetY)
                .texture("images/flying_bat.atlas")
                .speed(6f)
                .build();
        spawnEntityAt(highHorizontalBat2, new GridPoint2(
                        (int) batBuilder3.getSpawnX() + offsetX,
                        (int) batBuilder3.getSpawnY() + offsetY),
                true, true);

        BoxFactory.AutonomousBoxBuilder batBuilder4 = new BoxFactory.AutonomousBoxBuilder();
        Entity diagonalBat1 = batBuilder4
                .moveX(5f + offsetX, 10f + offsetX).moveY(19f + offsetY, 25f + offsetY)
                .texture("images/flying_bat.atlas")
                .speed(4f)
                .build();
        spawnEntityAt(diagonalBat1, new GridPoint2(
                        (int) batBuilder4.getSpawnX() + offsetX,
                        (int) batBuilder4.getSpawnY() + offsetY),
                true, true);

        BoxFactory.AutonomousBoxBuilder batBuilder5 = new BoxFactory.AutonomousBoxBuilder();
        Entity verticalBat1 = batBuilder5
                .moveX(3f + offsetX, 3f + offsetX).moveY(19f + offsetY, 25f + offsetY)
                .texture("images/flying_bat.atlas")
                .speed(4f)
                .build();
        spawnEntityAt(verticalBat1, new GridPoint2(
                        (int) batBuilder5.getSpawnX() + offsetX,
                        (int) batBuilder5.getSpawnY() + offsetY),
                true, true);
    }


    public void spawnUpgrade(String upgradeID, int posx, int posy) {
        if (upgradeID == "dash") {
            Entity upgrade = CollectableFactory.createDashUpgrade();
            upgrade.addComponent(new TooltipSystem.TooltipComponent("Collect Dash Upgrade", TooltipSystem.TooltipStyle.SUCCESS));
            spawnEntityAt(upgrade, new GridPoint2(posx, posy), true, true);
        }

        if (upgradeID == "glider") {
            Entity upgrade = CollectableFactory.createGlideUpgrade();
            upgrade.addComponent(new TooltipSystem.TooltipComponent("Collect Glider Upgrade", TooltipSystem.TooltipStyle.SUCCESS));
            spawnEntityAt(upgrade, new GridPoint2(posx, posy), true, true);
        }
//        if (upgradeID == "jetpack") {
//            Entity upgrade = CollectableFactory.createJetpackUpgrade();
//            spawnEntityAt(upgrade, new GridPoint2(posx, posy), true, true);
//        }
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
