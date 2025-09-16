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
import com.csse3200.game.components.ButtonManagerComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
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
            "images/Empty.png",
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
            "images/wall.png",
            "images/dash_powerup.png",
            "images/cavelevel/cavebackground.png",
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
            "images/empty.png"
    };
    private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";
    private static final String[] musics = {backgroundMusic};
    private static final String[] gameSounds = {"sounds/Impact4.ogg",
            "sounds/chimesound.mp3"};
    private static final String[] gameTextureAtlases = {
            "images/PLAYER.atlas",
            "images/drone.atlas",
            "images/volatile_platform.atlas",
            "images/flying_bat.atlas" // Bat sprites from https://todemann.itch.io/bat (see Wiki)
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
        spawnParallaxBackground();
        spawnFloorsAndPlatforms();
        spawnVolatilePlatform();
        spawnDeathZone();
        spawnWalls();
        spawnDoor();
        spawnSecurityCams();
        spawnButtons();
        spawnTraps();
        spawnPlatformBat();
        spawnLevelOneBatRoom();
    }

    private void spawnDeathZone() {
        GridPoint2 spawnPos =  new GridPoint2(12,0);
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

        // THESE TWO TO BE REPLACED WITH LADDERS
        GridPoint2 step4Pos = new GridPoint2(48,6);
        Entity step4 = PlatformFactory.createStaticPlatform();
        step4.setScale(1.8f,0.5f);
        spawnEntityAt(step4, step4Pos,false, false);

        GridPoint2 step6Pos = new GridPoint2(42,12);
        Entity step6 = PlatformFactory.createStaticPlatform();
        step6.setScale(1.8f,0.5f);
        spawnEntityAt(step6, step6Pos,false, false);
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
        GridPoint2 buttonPlatformPos = new GridPoint2(53, 18);
        Vector2 offsetWorldButton = new Vector2(9f, 0f);
        float speedButton = 2f;

        Entity buttonPlatform = PlatformFactory.createButtonTriggeredPlatform(offsetWorldButton, speedButton);
        buttonPlatform.setScale(2f, 0.5f);
        spawnEntityAt(buttonPlatform, buttonPlatformPos, false, false);
        logger.info("Moving platform spawned at {}", buttonPlatformPos);

        //start button
        Entity buttonStart = ButtonFactory.createButton(false, "platform", "down");
        buttonStart.addComponent(new TooltipSystem.TooltipComponent(
                "Platform Button\nPress E to interact",
                TooltipSystem.TooltipStyle.DEFAULT
        ));
        GridPoint2 buttonStartPos = new GridPoint2(50, 25);
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
        Entity buttonEnd = ButtonFactory.createButton(false, "platform", "down");
        buttonEnd.addComponent(new TooltipSystem.TooltipComponent(
                "Platform Button \nPress E to interact",
                TooltipSystem.TooltipStyle.DEFAULT
        ));
        GridPoint2 buttonEndPos = new GridPoint2(72, 25);
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

//        GridPoint2 puzzleGroundPos = new GridPoint2(0, 32);
//        Entity puzzleGround = PlatformFactory.createStaticPlatform();
//        puzzleGround.setScale(16,2);
//        spawnEntityAt(puzzleGround, puzzleGroundPos, false, false);

        GridPoint2 removeThis1 = new GridPoint2(48,35);
        Entity removeThis = PlatformFactory.createStaticPlatform();
        removeThis.setScale(2f,0.5f);
        spawnEntityAt(removeThis, removeThis1,false, false);

        GridPoint2 step9Pos = new GridPoint2(57,35);
        Entity step9 = PlatformFactory.createStaticPlatform();
        step9.setScale(4f,0.5f);
        spawnEntityAt(step9, step9Pos,false, false);

        // THESE TWO TO BE REPLACED WITH LADDERS
        GridPoint2 step10Pos = new GridPoint2(63,38);
        Entity step10 = PlatformFactory.createStaticPlatform();
        step10.setScale(1.8f,0.5f);
        spawnEntityAt(step10, step10Pos,false, false);

        GridPoint2 step11Pos = new GridPoint2(58,43);
        Entity step11 = PlatformFactory.createStaticPlatform();
        step11.setScale(1.8f,0.5f);
        spawnEntityAt(step11, step11Pos,false, false);
//      ^^

        GridPoint2 step12Pos = new GridPoint2(52,48);
        Entity step12 = PlatformFactory.createStaticPlatform();
        step12.setScale(3.5f,0.5f);
        spawnEntityAt(step12, step12Pos,false, false);

        GridPoint2 step13Pos = new GridPoint2(70,48);
        Entity step13 = PlatformFactory.createStaticPlatform();
        step13.setScale(2f,0.5f);
        spawnEntityAt(step13, step13Pos,false, false);
    }


    private void spawnPlatforms(){
//        GridPoint2 groundPos1 = new GridPoint2(0, 0);
//        Entity ground1 = PlatformFactory.createStaticPlatform();
//        ground1.setScale(5,2);
//        spawnEntityAt(ground1, groundPos1, false, false);
//
//        GridPoint2 groundPos2 = new GridPoint2(15, 0);
//        Entity ground2 = PlatformFactory.createStaticPlatform();
//        ground2.setScale(25f,2f);
//        spawnEntityAt(ground2, groundPos2, false, false);
//
//        GridPoint2 groundPos3 = new GridPoint2(70, 0);
//        Entity ground3 = PlatformFactory.createStaticPlatform();
//        ground3.setScale(5,2);
//        spawnEntityAt(ground3, groundPos3, false, false);

//        GridPoint2 step1Pos = new GridPoint2(21,6);
//        Entity step1 = PlatformFactory.createStaticPlatform();
//        step1.setScale(2,0.5f);
//        spawnEntityAt(step1, step1Pos,false, false);
//
//        GridPoint2 step2Pos = new GridPoint2(15,9);
//        Entity step2 = PlatformFactory.createStaticPlatform();
//        step2.setScale(1.5f,0.5f);
//        spawnEntityAt(step2, step2Pos,false, false);
//
//        GridPoint2 step3Pos = new GridPoint2(22,12);
//        Entity step3 = PlatformFactory.createStaticPlatform();
//        step3.setScale(1.8f,0.5f);
//        spawnEntityAt(step3, step3Pos,false, false);
//
////        THESE TWO TO BE REPLACED WITH LADDERS
//        GridPoint2 step4Pos = new GridPoint2(48,6);
//        Entity step4 = PlatformFactory.createStaticPlatform();
//        step4.setScale(1.8f,0.5f);
//        spawnEntityAt(step4, step4Pos,false, false);
//
//        GridPoint2 step6Pos = new GridPoint2(42,12);
//        Entity step6 = PlatformFactory.createStaticPlatform();
//        step6.setScale(1.8f,0.5f);
//        spawnEntityAt(step6, step6Pos,false, false);
////      ^
//
//        GridPoint2 step7Pos = new GridPoint2(45,18);
//        Entity step7 = PlatformFactory.createStaticPlatform();
//        step7.setScale(3.5f,0.5f);
//        spawnEntityAt(step7, step7Pos,false, false);
//
////        RIGHT PATH
//        GridPoint2 step8Pos = new GridPoint2(58,18);
//        Entity step8 = PlatformFactory.createStaticPlatform();
//        step8.setScale(2f,0.5f);
//        spawnEntityAt(step8, step8Pos,false, false);
//
//        GridPoint2 buttonPlatformPos = new GridPoint2(63,18);
//        Vector2 offsetWorldButton = new Vector2(2.5f, 0f);
//        float speedButton = 2f;
//        Entity buttonPlatform = PlatformFactory.createButtonTriggeredPlatform(offsetWorldButton, speedButton);
//        buttonPlatform.setScale(2f,0.5f);
//        spawnEntityAt(buttonPlatform, buttonPlatformPos,false, false);
//
////        LEFT PATH
//        GridPoint2 moving1Pos = new GridPoint2(38,26);
//        Vector2 offsetWorld  = new Vector2(0f, 4f);
//        float speed = 2f;
//        Entity moving1 = PlatformFactory.createMovingPlatform(offsetWorld,speed);
//        moving1.setScale(2f,0.5f);
//        spawnEntityAt(moving1, moving1Pos,false, false);
//
//        GridPoint2 puzzleGroundPos = new GridPoint2(0, 32);
//        Entity puzzleGround = PlatformFactory.createStaticPlatform();
//        puzzleGround.setScale(16,2);
//        spawnEntityAt(puzzleGround, puzzleGroundPos, false, false);
//
//        GridPoint2 removeThis1 = new GridPoint2(48,35);
//        Entity removeThis = PlatformFactory.createStaticPlatform();
//        removeThis.setScale(2f,0.5f);
//        spawnEntityAt(removeThis, removeThis1,false, false);
//
//        GridPoint2 step9Pos = new GridPoint2(57,35);
//        Entity step9 = PlatformFactory.createStaticPlatform();
//        step9.setScale(4f,0.5f);
//        spawnEntityAt(step9, step9Pos,false, false);
//
////        THESE TWO TO BE REPLACED WITH LADDERS
//        GridPoint2 step10Pos = new GridPoint2(63,38);
//        Entity step10 = PlatformFactory.createStaticPlatform();
//        step10.setScale(1.8f,0.5f);
//        spawnEntityAt(step10, step10Pos,false, false);
//
//        GridPoint2 step11Pos = new GridPoint2(58,43);
//        Entity step11 = PlatformFactory.createStaticPlatform();
//        step11.setScale(1.8f,0.5f);
//        spawnEntityAt(step11, step11Pos,false, false);
////      ^^
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
//
//        GridPoint2 gatePlatformPos= new GridPoint2(33,60);
//        Entity gatePlatform = PlatformFactory.createStaticPlatform();
//        gatePlatform.setScale(5f,0.5f);
//        spawnEntityAt(gatePlatform, gatePlatformPos,false, false);
    }
    public void spawnDoor() {
        Entity door = ObstacleFactory.createDoor("door", this);
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
        // Bottom
        spawnEntityAt(ObstacleFactory.createWall(worldBounds.x, WALL_THICKNESS),
                new GridPoint2(0, 0), false, false);
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
        parallaxBg.addLayer(backgroundTexture, 0.1f);

        // Layer 6 - Far background
        Texture layer1 = resourceService.getAsset("images/cavelevel/background/6.png", Texture.class);
        parallaxBg.addLayer(layer1, 0.13f);

        // Layer 5 - Mid-far background
        Texture layer2 = resourceService.getAsset("images/cavelevel/background/5.png", Texture.class);
        parallaxBg.addLayer(layer2, 0.15f);

        // Layer 4 - Mid background
        Texture layer3 = resourceService.getAsset("images/cavelevel/background/4.png", Texture.class);
        parallaxBg.addLayer(layer3, 0.17f);

        // Layer 3 - Mid-near background
        Texture layer4 = resourceService.getAsset("images/cavelevel/background/3.png", Texture.class);
        parallaxBg.addLayer(layer4, 0.19f);

        // Layer 2 - Near background
        Texture layer5 = resourceService.getAsset("images/cavelevel/background/2.png", Texture.class);
        parallaxBg.addLayer(layer5, 0.20f);

        // Layer 1 - Nearest background (moves fastest)
        Texture layer6 = resourceService.getAsset("images/cavelevel/background/1.png", Texture.class);
        parallaxBg.addLayer(layer6, 0.21f);

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
        spawnEntityAt(spikesUp, new GridPoint2(54,19), true,  true);

        Entity spikesRight = TrapFactory.createSpikes(safeSpotCamera, 270f);
        spawnEntityAt(spikesRight, new GridPoint2(32,34), true,  true);
    }
    private void spawnButtons() {
        Entity puzzleEntity = new Entity();
        ButtonManagerComponent manager = new ButtonManagerComponent();
        puzzleEntity.addComponent(manager);
        ServiceLocator.getEntityService().register(puzzleEntity);

        Entity button2 = ButtonFactory.createButton(false, "door", "left");
        button2.addComponent(new TooltipSystem.TooltipComponent("Door Button\nPress E to interact", TooltipSystem.TooltipStyle.DEFAULT));
        spawnEntityAt(button2, new GridPoint2(79 ,20), true,  true);

        Entity button = ButtonFactory.createPuzzleButton(false, "nothing", "left", manager);
        spawnEntityAt(button, new GridPoint2(74,50), true,  true);

        Entity button4 = ButtonFactory.createPuzzleButton(false, "nothing", "left", manager);
        button4.addComponent(new TooltipSystem.TooltipComponent("Puzzle Button\nYou have 15 seconds to press all three", TooltipSystem.TooltipStyle.DEFAULT));
        spawnEntityAt(button4, new GridPoint2(67,40), true,  true);

        Entity button5 = ButtonFactory.createPuzzleButton(false, "nothing", "right", manager);
        spawnEntityAt(button5, new GridPoint2(58,45), true,  true);

        //listener to spawn key when door button pushed
        button2.getEvents().addListener("buttonToggled", (Boolean isPushed) -> {
            if (isPushed && !keySpawned) {
                spawnKey();
                keySpawned = true;
            }
        });

        puzzleEntity.getEvents().addListener("puzzleCompleted", () -> {
            //what to do when puzzle completed, probably player upgrade
            //if you want to spawn on platform before door spawn at (46, 56)
            Entity dashUpgrade = CollectableFactory.createDashUpgrade();
            spawnEntityAt(dashUpgrade, new GridPoint2(1,37), true,  true);
        });

    }
    public void spawnKey() {
        Entity key = CollectableFactory.createKey("door");
        spawnEntityAt(key, new GridPoint2(46,56), true, true);
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
