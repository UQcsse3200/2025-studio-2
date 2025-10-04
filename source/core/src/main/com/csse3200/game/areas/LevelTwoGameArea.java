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
import com.csse3200.game.components.ButtonManagerComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.obstacles.DoorComponent;
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

public class LevelTwoGameArea extends GameArea {
    private static final GridPoint2 mapSize = new GridPoint2(100,70);
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
            "images/wall.png",
            "images/lablevel/level2background.png",
            "images/lablevel/background/bgtile1.png",
            "images/lablevel/background/bgtile2.png",
            "images/lablevel/background/bgtile3.png",
            "images/lablevel/background/bgtile4.png",
            "images/lablevel/background/bgtile5.png",
            "images/lablevel/background/bgtile6.png",
            "images/lablevel/background/labforeground.png",
            "images/lablevel/background/level2background.png",
            "images/lablevel/background/background2.png",
            "images/glide_powerup.png"

    };
    private static final String backgroundMusic = "sounds/Flow.mp3";
    private static final String[] musics = {backgroundMusic};
    private static final String[] gameSounds = {"sounds/Impact4.ogg",
            "sounds/chimesound.mp3",
            "sounds/doorsound.mp3",
            "sounds/walksound.mp3",
            "sounds/whooshsound.mp3",
            "sounds/jetpacksound.mp3",
            "sounds/thudsound.mp3"};
    private static final String[] gameTextureAtlases = {
            "images/PLAYER.atlas",
            "images/volatile_platform.atlas",
            "images/doors.atlas"
    };
    private static final Logger logger = LoggerFactory.getLogger(LevelTwoGameArea.class);
    private final TerrainFactory terrainFactory;

    public LevelTwoGameArea(TerrainFactory terrainFactory) {
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
        spawnParallaxBackground();
        spawnFloorsAndPlatforms();
        spawnVolatilePlatform();
        spawnDeathZone();
        spawnWalls();
        spawnDoor();
        spawnTraps();
        spawnButtons();
        spawnSecurityCams();
        spawnObjectives();
    }

    private void spawnDeathZone() {
        GridPoint2 spawnPos =  new GridPoint2(5,0);
        Entity deathZone = DeathZoneFactory.createDeathZone();
        spawnEntityAt(deathZone, spawnPos, true,  true);
    }

    private void spawnWalls(){
        GridPoint2 bottomWallPos = new GridPoint2(80,6);
        Entity bottomWall = WallFactory.createWall(25,0,1,20f,"");
        bottomWall.setScale(2f,7f);
        spawnEntityAt(bottomWall, bottomWallPos, false, false);

        GridPoint2 middleWallPos = new GridPoint2(54,24);
        Entity middleWall = WallFactory.createWall(25,0,1,20f,"");
        middleWall.setScale(2f,6f);
        spawnEntityAt(middleWall, middleWallPos, false, false);

//        TOP LEVEL
        GridPoint2 wall1Pos = new GridPoint2(33,44);
        Entity wall1 = WallFactory.createWall(25,0,1,20f,"");
        wall1.setScale(2f,8f);
        spawnEntityAt(wall1, wall1Pos, false, false);

        GridPoint2 wall2Pos = new GridPoint2(46,44);
        Entity wall2 = WallFactory.createWall(25,0,1,20f,"");
        wall2.setScale(2f,6.5f);
        spawnEntityAt(wall2, wall2Pos, false, false);

        GridPoint2 wall3Pos = new GridPoint2(59,44);
        Entity wall3 = WallFactory.createWall(25,0,1,20f,"");
        wall3.setScale(2f,5f);
        spawnEntityAt(wall3, wall3Pos, false, false);

        GridPoint2 wall4Pos = new GridPoint2(72,44);
        Entity wall4 = WallFactory.createWall(25,0,1,20f,"");
        wall4.setScale(2f,3.5f);
        spawnEntityAt(wall4, wall4Pos, false, false);

        GridPoint2 wall5Pos = new GridPoint2(85,44);
        Entity wall5 = WallFactory.createWall(25,0,1,20f,"");
        wall5.setScale(2f,2f);
        spawnEntityAt(wall5, wall5Pos, false, false);
    }

    public void spawnDoor() {
        Entity door = ObstacleFactory.createDoor("key:door", this);
        door.setScale(1, 2);
        door.addComponent(new TooltipSystem.TooltipComponent("Unlock the door with the key", TooltipSystem.TooltipStyle.DEFAULT));
        // door.getComponent(DoorComponent.class).openDoor();
        spawnEntityAt(door, new GridPoint2(98,45), true, true);
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
        ui.addComponent(new GameAreaDisplay("Level Two Game Area"));
        ui.addComponent(new TooltipSystem.TooltipDisplay());
        spawnEntity(ui);
    }

    private void spawnTerrain() {
        // Need to decide how large each area is going to be
        terrain = createLabTerrain();
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

    private TerrainComponent createLabTerrain() {
        final ResourceService resourceService = ServiceLocator.getResourceService();
        // Use empty texture for invisible terrain grid
        TextureRegion emptyTile = new TextureRegion(resourceService.getAsset("images/empty.png", Texture.class));

        GridPoint2 tilePixelSize = new GridPoint2(emptyTile.getRegionWidth(), emptyTile.getRegionHeight());
        TiledMap tiledMap = terrainFactory.createDefaultTiles(tilePixelSize, emptyTile, emptyTile, emptyTile, emptyTile, mapSize);
        return terrainFactory.createInvisibleFromTileMap(0.5f, tiledMap, tilePixelSize);
    }

    private void spawnParallaxBackground() {
        Entity backgroundEntity = new Entity();

        // Get the camera from the render service
        Camera gameCamera = ServiceLocator.getRenderService().getRenderer().getCamera().getCamera();

        // Get map dimensions from terrain
        GridPoint2 mapBounds = terrain.getMapBounds(0);
        float tileSize = terrain.getTileSize();
        float mapWorldWidth = mapBounds.x * tileSize;
        float mapWorldHeight = mapBounds.y * tileSize;

        ParallaxBackgroundComponent parallaxBg = new ParallaxBackgroundComponent(gameCamera, mapWorldWidth, mapWorldHeight);

        ResourceService resourceService = ServiceLocator.getResourceService();

        // Layer 1: Far background - moves slightly
        Texture farBackground = resourceService.getAsset("images/lablevel/background/level2background.png", Texture.class);
        parallaxBg.addTiledLayer(farBackground, 0.12f, true, true, 10f, 10f, 0, -2.8f);

        // Layer 2: Near background - doesnt move
        Texture nearBackground = resourceService.getAsset("images/lablevel/background/background2.png", Texture.class);
        parallaxBg.addScaledLayer(nearBackground, 1f, -20, -14, 0.057f);

        backgroundEntity.addComponent(parallaxBg);
        spawnEntity(backgroundEntity);
    }

    private void spawnFloorsAndPlatforms(){
        spawnFloors();

        spawnElevatedPlatforms();

        spawnVolatilePlatform();

    }

    private void spawnFloors() {
        GridPoint2 groundPos1 = new GridPoint2(0, 0);
        Entity ground1 = FloorFactory.createGroundFloor();
        ground1.setScale(5,2);
        spawnEntityAt(ground1, groundPos1, false, false);

        GridPoint2 groundPos2 = new GridPoint2(25, 0);
        Entity ground2 = FloorFactory.createGroundFloor();
        ground2.setScale(37.5f,2f);
        spawnEntityAt(ground2, groundPos2, false, false);

        GridPoint2 middleGroundPos = new GridPoint2(0, 20);
        Entity middleGround = FloorFactory.createStaticFloor();
        middleGround.setScale(42f,2);
        spawnEntityAt(middleGround, middleGroundPos, false, false);

        GridPoint2 topGroundPos = new GridPoint2(16, 40);
        Entity topGround = FloorFactory.createStaticFloor();
        topGround.setScale(42f,2);
        spawnEntityAt(topGround, topGroundPos, false, false);
    }

    private void spawnElevatedPlatforms() {
        //        RIGHT GRAPPLE SECTION
        GridPoint2 step1Pos = new GridPoint2(93,6);
        Entity step1 = PlatformFactory.createStaticPlatform();
        step1.setScale(2,0.5f);
        spawnEntityAt(step1, step1Pos,false, false);

        GridPoint2 step2Pos = new GridPoint2(90,10);
        Entity step2 = PlatformFactory.createStaticPlatform();
        step2.setScale(2,0.5f);
        spawnEntityAt(step2, step2Pos,false, false);

        GridPoint2 step3Pos = new GridPoint2(93,15);
        Entity step3 = PlatformFactory.createStaticPlatform();
        step3.setScale(2,0.5f);
        spawnEntityAt(step3, step3Pos,false, false);

        GridPoint2 step4Pos = new GridPoint2(90,19);
        Entity step4 = PlatformFactory.createStaticPlatform();
        step4.setScale(2,0.5f);
        spawnEntityAt(step4, step4Pos,false, false);

//        MIDDLE LEVEL
        GridPoint2 moving1Pos = new GridPoint2(75,27);
        Vector2 offsetWorld  = new Vector2(0f, 4f);
        float speed = 2f;
        Entity moving1 = PlatformFactory.createMovingPlatform(offsetWorld,speed);
        moving1.setScale(2f,0.5f);
        spawnEntityAt(moving1, moving1Pos,false, false);

        GridPoint2 moving2Pos = new GridPoint2(60,27);
        Vector2 offsetWorld2 = new Vector2(0f, 4f);
        float speed2 = 2f;
        Entity moving2 = PlatformFactory.createMovingPlatform(offsetWorld2, speed2);
        moving2.setScale(2f,0.5f);
        spawnEntityAt(moving2, moving2Pos,false, false);

//        LEFT GRAPPLE SECTION
        GridPoint2 left1Pos = new GridPoint2(8,26);
        Entity left1 = PlatformFactory.createStaticPlatform();
        left1.setScale(2,0.5f);
        spawnEntityAt(left1, left1Pos,false, false);

        GridPoint2 left2Pos = new GridPoint2(5,30);
        Entity left2 = PlatformFactory.createStaticPlatform();
        left2.setScale(2,0.5f);
        spawnEntityAt(left2, left2Pos,false, false);

        GridPoint2 left3Pos = new GridPoint2(8,35);
        Entity left3 = PlatformFactory.createStaticPlatform();
        left3.setScale(2,0.5f);
        spawnEntityAt(left3, left3Pos,false, false);

        GridPoint2 left4Pos = new GridPoint2(5,39);
        Entity left4 = PlatformFactory.createStaticPlatform();
        left4.setScale(2,0.5f);
        spawnEntityAt(left4, left4Pos,false, false);

        GridPoint2 left5Pos = new GridPoint2(5,52);
        Entity left5 = PlatformFactory.createStaticPlatform();
        left5.setScale(2,0.5f);
        spawnEntityAt(left5, left5Pos,false, false);

        GridPoint2 buttonBalconyPos = new GridPoint2(0,55);
        Entity buttonBalcony = PlatformFactory.createStaticPlatform();
        buttonBalcony.setScale(2,1f);
        spawnEntityAt(buttonBalcony, buttonBalconyPos,false, false);

//        TOP LEVEL
        GridPoint2 top1Pos = new GridPoint2(34,47);
        Entity top1 = PlatformFactory.createStaticPlatform();
        top1.setScale(2,0.5f);
        spawnEntityAt(top1, top1Pos,false, false);

        GridPoint2 top2Pos = new GridPoint2(41,52);
        Entity top2 = PlatformFactory.createStaticPlatform();
        top2.setScale(2,0.5f);
        spawnEntityAt(top2, top2Pos,false, false);

        GridPoint2 top3Pos = new GridPoint2(47,50);
        Entity top3 = PlatformFactory.createStaticPlatform();
        top3.setScale(2,0.5f);
        spawnEntityAt(top3, top3Pos,false, false);

        //platforms for button puzzle
        GridPoint2 buttonPlat1Pos = new GridPoint2(53,5);
        Entity buttonPlat1 = PlatformFactory.createStaticPlatform();
        buttonPlat1.setScale(2,0.5f);
        spawnEntityAt(buttonPlat1, buttonPlat1Pos,false, false);

        GridPoint2 buttonPlat2Pos = new GridPoint2(70,8);
        Entity buttonPlat2 = PlatformFactory.createStaticPlatform();
        buttonPlat2.setScale(2,0.5f);
        spawnEntityAt(buttonPlat2, buttonPlat2Pos,false, false);

        GridPoint2 buttonPlat3Pos = new GridPoint2(53,11);
        Entity buttonPlat3 = PlatformFactory.createStaticPlatform();
        buttonPlat3.setScale(2,0.5f);
        spawnEntityAt(buttonPlat3, buttonPlat3Pos,false, false);

        GridPoint2 buttonPlat4Pos = new GridPoint2(70,15);
        Entity ButtonPlat4 = PlatformFactory.createStaticPlatform();
        ButtonPlat4.setScale(2,0.5f);
        spawnEntityAt(ButtonPlat4, buttonPlat4Pos,false, false);
    }

    private void spawnSecurityCams() {
        Entity cam1 = SecurityCameraFactory.createSecurityCamera(player, LightingDefaults.ANGULAR_VEL, "1");
        Entity cam2 = SecurityCameraFactory.createSecurityCamera(player, LightingDefaults.ANGULAR_VEL, "2");
        Entity cam3 = SecurityCameraFactory.createStaticSecurityCam(player, 25f, -135f, 0f, "3");

        spawnEntityAt(cam1, new GridPoint2(34,19), true, true);
        spawnEntityAt(cam2, new GridPoint2(83,39), true, true);
        spawnEntityAt(cam3, new GridPoint2(75,65), true, true);
    }


    private void spawnTraps() {
        Vector2 safeSpotStart = new Vector2(41, 12);

        for(int i=59; i<=77; i++) {
            Entity spikesUp = TrapFactory.createSpikes(safeSpotStart, 0f);
            spawnEntityAt(spikesUp, new GridPoint2(i,24), true,  true);
        }
    }

    private void spawnButtons() {
        //key button and spawning
        Entity button2 = ButtonFactory.createButton(false, "door", "right");
        button2.addComponent(new TooltipSystem.TooltipComponent("Door Button\nPress E to interact", TooltipSystem.TooltipStyle.DEFAULT));
        spawnEntityAt(button2, new GridPoint2(0 ,58), true,  true);

        button2.getEvents().addListener("buttonToggled", (Boolean isPushed) -> {
            if (isPushed && !keySpawned) {
                spawnKey();
                keySpawned = true;
            }
        });

        //white button puzzle and spawning upgrade
        //puzzle setup
        Entity puzzleEntity = new Entity();
        ButtonManagerComponent manager = new ButtonManagerComponent();
        puzzleEntity.addComponent(manager);
        // Prevent leak
        this.spawnEntityAt(puzzleEntity, new GridPoint2(0, 0), true, true);

        //spawn buttons
        Entity button = ButtonFactory.createPuzzleButton(false, "nothing", "right", manager);
        button.addComponent(new TooltipSystem.TooltipComponent("Puzzle Button\nYou have 15 seconds to press all four", TooltipSystem.TooltipStyle.DEFAULT));
        spawnEntityAt(button, new GridPoint2(53,6), true,  true);

        Entity button4 = ButtonFactory.createPuzzleButton(false, "nothing", "left", manager);
        spawnEntityAt(button4, new GridPoint2(73,10), true,  true);

        Entity button5 = ButtonFactory.createPuzzleButton(false, "nothing", "right", manager);
        spawnEntityAt(button5, new GridPoint2(53,13), true,  true);

        Entity button6 = ButtonFactory.createPuzzleButton(false, "nothing", "left", manager);
        spawnEntityAt(button6, new GridPoint2(73,17), true,  true);

        //spawn upgrade
        puzzleEntity.getEvents().addListener("puzzleCompleted", () -> {
            Entity dashUpgrade = CollectableFactory.createJetpackUpgrade();
            spawnEntityAt(dashUpgrade, new GridPoint2(91,6), true,  true);
        });
    }

    public void spawnKey() {
        Entity key = CollectableFactory.createKey("key:door");
        spawnEntityAt(key, new GridPoint2(93,50), true, true);
        spawnEntityAt(CollectableFactory.createObjective("keycard_completed", 0.2f, 0.2f), new GridPoint2(92, 50), true, true);
    }

    private void spawnVolatilePlatform(){
        GridPoint2 volatile1Pos = new GridPoint2(68,31);
        Entity volatile1 = PlatformFactory.createVolatilePlatform(2f,1.5f);
        volatile1.setScale(2f,0.5f);
        spawnEntityAt(volatile1, volatile1Pos,false, false);

        GridPoint2 topVolatile1Pos = new GridPoint2(25,48);
        Entity topVolatile1 = PlatformFactory.createVolatilePlatform(2f,1.5f);
        topVolatile1.setScale(2f,0.5f);
        spawnEntityAt(topVolatile1, topVolatile1Pos,false, false);

        GridPoint2 topVolatile2Pos = new GridPoint2(21,53);
        Entity topVolatile2 = PlatformFactory.createVolatilePlatform(2f,1.5f);
        topVolatile2.setScale(2f,0.5f);
        spawnEntityAt(topVolatile2, topVolatile2Pos,false, false);

        GridPoint2 topVolatile3Pos = new GridPoint2(25,58);
        Entity topVolatile3 = PlatformFactory.createVolatilePlatform(2f,1.5f);
        topVolatile3.setScale(2f,0.5f);
        spawnEntityAt(topVolatile3, topVolatile3Pos,false, false);
    }

    private void spawnObjectives() {
        // Large, invisible sensors — easy to grab, no textures.
        // IDs chosen to match the ObjectiveTab banner map.
        Gdx.app.log("LevelOne", "Spawning objectives…");
        spawnEntityAt(CollectableFactory.createObjective("glider", 2.0f, 2.0f), new GridPoint2(1, 3), true, true);
        spawnEntityAt(CollectableFactory.createObjective("keycard", 2.0f, 2.0f), new GridPoint2(1, 3), true, true);
        spawnEntityAt(CollectableFactory.createObjective("door", 2.0f, 2.0f), new GridPoint2(1, 3), true, true);

        spawnEntityAt(CollectableFactory.createObjective("glider_completed", 2.0f, 2.0f), new GridPoint2(1, 3), true, true);
    }

    protected void loadAssets() {
        logger.debug("Loading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.loadTextures(gameTextures);
        resourceService.loadMusic(musics);
        resourceService.loadTextureAtlases(gameTextureAtlases);
        resourceService.loadSounds(gameSounds);
        resourceService.loadMusic(musics);

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
