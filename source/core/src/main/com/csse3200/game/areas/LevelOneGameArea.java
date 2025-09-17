package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.DoorControlComponent;
import com.csse3200.game.components.enemy.ActivationComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.minimap.MinimapDisplay;
import com.csse3200.game.components.obstacles.DoorComponent;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.lighting.LightingDefaults;
import com.csse3200.game.services.MinimapService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.GridPoint2Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class LevelOneGameArea extends GameArea {
    private static final GridPoint2 mapSize = new GridPoint2(80,70);
    private static final float WALL_WIDTH = 0.1f;
    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(1, 10);

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
            "images/wall.png"
    };
    private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";
    private static final String[] musics = {backgroundMusic};
    private static final String[] gameSounds = {"sounds/Impact4.ogg",
            "sounds/chimesound.mp3"};
    private static final String[] gameTextureAtlases = {
            "images/PLAYER.atlas",
            "images/drone.atlas",
            "images/volatile_platform.atlas"
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
        spawnPlatforms();
        spawnVolatilePlatform();
        spawnDeathZone();
        spawnWalls();
        spawnDoor();
        spawnLights();
        spawnPatrollingDrone();
        //spawnBomberDrone();
    }

    private void spawnDeathZone() {
        GridPoint2 spawnPos =  new GridPoint2(12,0);
        Entity deathZone = DeathZoneFactory.createDeathZone(spawnPos, new Vector2(5,10));
        spawnEntityAt(deathZone, spawnPos, true,  true);

        //Uncomment with deathzonefactory changes for a second small deathzone over second gap
        //GridPoint2 spawn2Pos =  new GridPoint2(67,0);
        //Entity deathZone2 = DeathZoneFactory.createDeathZone(spawnPos, new Vector2(5,10));
        //spawnEntityAt(deathZone2, spawn2Pos, true,  true);
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
    private void spawnPlatforms(){
        GridPoint2 groundPos1 = new GridPoint2(0, 0);
        Entity ground1 = PlatformFactory.createStaticPlatform();
        ground1.setScale(5,2);
        spawnEntityAt(ground1, groundPos1, false, false);

        GridPoint2 groundPos2 = new GridPoint2(15, 0);
        Entity ground2 = PlatformFactory.createStaticPlatform();
        ground2.setScale(25f,2f);
        spawnEntityAt(ground2, groundPos2, false, false);

        GridPoint2 groundPos3 = new GridPoint2(70, 0);
        Entity ground3 = PlatformFactory.createStaticPlatform();
        ground3.setScale(5,2);
        spawnEntityAt(ground3, groundPos3, false, false);

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
        GridPoint2 step8Pos = new GridPoint2(58,18);
        Entity step8 = PlatformFactory.createStaticPlatform();
        step8.setScale(2f,0.5f);
        spawnEntityAt(step8, step8Pos,false, false);

        GridPoint2 buttonPlatformPos = new GridPoint2(63,18);
        Vector2 offsetWorldButton = new Vector2(2.5f, 0f);
        float speedButton = 2f;
        Entity buttonPlatform = PlatformFactory.createButtonTriggeredPlatform(offsetWorldButton, speedButton);
        buttonPlatform.setScale(2f,0.5f);
        spawnEntityAt(buttonPlatform, buttonPlatformPos,false, false);

//        LEFT PATH
        GridPoint2 moving1Pos = new GridPoint2(38,26);
        Vector2 offsetWorld  = new Vector2(0f, 4f);
        float speed = 2f;
        Entity moving1 = PlatformFactory.createMovingPlatform(offsetWorld,speed);
        moving1.setScale(2f,0.5f);
        spawnEntityAt(moving1, moving1Pos,false, false);

        GridPoint2 puzzleGroundPos = new GridPoint2(0, 32);
        Entity puzzleGround = PlatformFactory.createStaticPlatform();
        puzzleGround.setScale(16,2);
        spawnEntityAt(puzzleGround, puzzleGroundPos, false, false);

        GridPoint2 removeThis1 = new GridPoint2(48,35);
        Entity removeThis = PlatformFactory.createStaticPlatform();
        removeThis.setScale(2f,0.5f);
        spawnEntityAt(removeThis, removeThis1,false, false);

        GridPoint2 step9Pos = new GridPoint2(57,35);
        Entity step9 = PlatformFactory.createStaticPlatform();
        step9.setScale(4f,0.5f);
        spawnEntityAt(step9, step9Pos,false, false);

//        THESE TWO TO BE REPLACED WITH LADDERS
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

        GridPoint2 gatePlatformPos= new GridPoint2(33,60);
        Entity gatePlatform = PlatformFactory.createStaticPlatform();
        gatePlatform.setScale(5f,0.5f);
        spawnEntityAt(gatePlatform, gatePlatformPos,false, false);
    }
    public void spawnDoor() {
        Entity door = ObstacleFactory.createDoor("door", this, "sprint1");
        door.setScale(1, 2);
        door.addComponent(new TooltipSystem.TooltipComponent("Unlock the door with the key", TooltipSystem.TooltipStyle.DEFAULT));
        door.getComponent(DoorComponent.class).openDoor();
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
    private void spawnLights() {
        // see the LightFactory class for more details on spawning these
        Entity securityLight = SecurityCameraFactory.createSecurityCamera(player, LightingDefaults.ANGULAR_VEL, "1");
        spawnEntityAt(securityLight, new GridPoint2(20, 10), true, true);
    }
    private void spawnPatrollingDrone() {
        GridPoint2 spawnTile = new GridPoint2(11, 4);

        Vector2[] patrolRoute = {
                terrain.tileToWorldPosition(spawnTile),
                terrain.tileToWorldPosition(new GridPoint2(14, 4))
        };
        Entity patrolDrone = EnemyFactory.createPatrollingDrone(player, patrolRoute)
                .addComponent(new ActivationComponent("1"));
        spawnEntityAt(patrolDrone, spawnTile, true, true);
    }

    private void spawnBomberDrone() {
        // First bomber with cone light detection - patrols and uses its downward cone light
        GridPoint2 spawnTile = new GridPoint2(3, 15);
        Vector2[] patrolRoute = {
                terrain.tileToWorldPosition(spawnTile),
                terrain.tileToWorldPosition(new GridPoint2(11, 13))
        };

        // Create bomber with unique ID "bomber1"
        Entity bomberDrone = EnemyFactory.createPatrollingBomberDrone(player, patrolRoute, "bomber1");
        spawnEntityAt(bomberDrone, spawnTile, true, true);

        /*GridPoint2 spawnTile2 = new GridPoint2(30, 25);
        Vector2[] patrolRoute2 = {
                terrain.tileToWorldPosition(spawnTile2),
                terrain.tileToWorldPosition(new GridPoint2(38, 25)),
                terrain.tileToWorldPosition(new GridPoint2(38, 30)),
                terrain.tileToWorldPosition(new GridPoint2(30, 30))
        };

        // Create second bomber with unique ID "bomber2"
        Entity bomberDrone2 = EnemyFactory.createPatrollingBomberDrone(player, patrolRoute2, "bomber2");
        spawnEntityAt(bomberDrone2, spawnTile2, true, true);*/
    }

    // Optional: Method for spawning a stationary bomber at a specific position
    private void spawnStationaryBomber(GridPoint2 position, String bomberId) {
        Entity bomberDrone = EnemyFactory.createBomberDrone(
                player,
                terrain.tileToWorldPosition(position),
                bomberId
        );
        spawnEntityAt(bomberDrone, position, true, true);
    }

    private void displayUI() {
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Level one Game Area"));
        ui.addComponent(new TooltipSystem.TooltipDisplay());
        spawnEntity(ui);
    }
    private void spawnTerrain() {
        // Need to decide how large each area is going to be
        terrain = createDefaultTerrain(mapSize);
        spawnEntity(new Entity().addComponent(terrain));

        // Terrain walls
        float tileSize = terrain.getTileSize();
        GridPoint2 tileBounds = terrain.getMapBounds(0);
        Vector2 worldBounds = new Vector2(tileBounds.x * tileSize, tileBounds.y * tileSize);

        // Left
        spawnEntityAt(
                ObstacleFactory.createWall(WALL_WIDTH, worldBounds.y), GridPoint2Utils.ZERO, false, false);
        // Right
        spawnEntityAt(
                ObstacleFactory.createWall(WALL_WIDTH, worldBounds.y),
                new GridPoint2(tileBounds.x, 0),
                false,
                false);
        // Top
        spawnEntityAt(
                ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH),
                new GridPoint2(0, tileBounds.y - 4),
                false,
                false);
        // Bottom
        spawnEntityAt(ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH),
                new GridPoint2(0, 0), false, false);
    }
    private TerrainComponent createDefaultTerrain(GridPoint2 mapSize) {
        TextureRegion variant1, variant2, variant3, baseTile;
        final ResourceService resourceService = ServiceLocator.getResourceService();

        baseTile =
                new TextureRegion(resourceService.getAsset("images/TechWallBase.png", Texture.class));
        variant1 =
                new TextureRegion(resourceService.getAsset("images/TechWallVariant1.png", Texture.class));
        variant2 =
                new TextureRegion(resourceService.getAsset("images/TechWallVariant2.png", Texture.class));
        variant3 =
                new TextureRegion(resourceService.getAsset("images/TechWallVariant3.png", Texture.class));
        GridPoint2 tilePixelSize = new GridPoint2(baseTile.getRegionWidth(), baseTile.getRegionHeight());
        TiledMap tiledMap = terrainFactory.createDefaultTiles(tilePixelSize, baseTile, variant1, variant2, variant3, mapSize);
        return terrainFactory.createFromTileMap(0.5f, tiledMap, tilePixelSize);
    }
    private void spawnVolatilePlatform(){
//        GridPoint2 platformPos = new GridPoint2(5, 8);
//        Entity volplatform = PlatformFactory.createVolatilePlatform(2,2);
//        volplatform.setScale(3,1);
//        spawnEntityAt(volplatform, platformPos, true, true);

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
    protected void loadAssets() {
        logger.debug("Loading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.loadTextures(gameTextures);
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
