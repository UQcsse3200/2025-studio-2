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
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.obstacles.DoorComponent;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.GridPoint2Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class LevelTwoGameArea extends GameArea {
    private static final GridPoint2 mapSize = new GridPoint2(100,70);
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
            "images/volatile_platform.atlas"
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
        spawnPlatforms();
        spawnVolatilePlatform();
        spawnDeathZone();
        spawnWalls();
        spawnDoor();
    }
    private void spawnDeathZone() {
        GridPoint2 spawnPos =  new GridPoint2(0,0);
        Entity deathZone = DeathZoneFactory.createDeathZone(spawnPos, new Vector2(5,10));
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
    }
    private void spawnPlatforms(){
        GridPoint2 groundPos1 = new GridPoint2(0, 0);
        Entity ground1 = PlatformFactory.createStaticPlatform();
        ground1.setScale(5,2);
        spawnEntityAt(ground1, groundPos1, false, false);

        GridPoint2 groundPos2 = new GridPoint2(25, 0);
        Entity ground2 = PlatformFactory.createStaticPlatform();
        ground2.setScale(37.5f,2f);
        spawnEntityAt(ground2, groundPos2, false, false);

        GridPoint2 middleGroundPos = new GridPoint2(0, 20);
        Entity middleGround = PlatformFactory.createStaticPlatform();
        middleGround.setScale(42f,2);
        spawnEntityAt(middleGround, middleGroundPos, false, false);

        GridPoint2 topGroundPos = new GridPoint2(16, 40);
        Entity topGround = PlatformFactory.createStaticPlatform();
        topGround.setScale(42f,2);
        spawnEntityAt(topGround, topGroundPos, false, false);

//        RIGHT GRAPPLE SECTION
        GridPoint2 step1Pos = new GridPoint2(93,6);
        Entity step1 = PlatformFactory.createStaticPlatform();
        step1.setScale(2,0.5f);
        spawnEntityAt(step1, step1Pos,false, false);

        GridPoint2 step2Pos = new GridPoint2(90,10);
        Entity step2 = PlatformFactory.createStaticPlatform();
        step1.setScale(2,0.5f);
        spawnEntityAt(step1, step2Pos,false, false);

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

        GridPoint2 moving2Pos = new GridPoint2(60,35);
        Vector2 offsetWorld2 = new Vector2(0f, -4f);
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
    }
    public void spawnDoor() {
        Entity door = ObstacleFactory.createDoor("door", this, "sprint1");
        door.setScale(1, 2);
        door.addComponent(new TooltipSystem.TooltipComponent("Unlock the door with the key", TooltipSystem.TooltipStyle.DEFAULT));
        door.getComponent(DoorComponent.class).openDoor();
        spawnEntityAt(door, new GridPoint2(90,44), true, true);
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
        GridPoint2 volatile1Pos = new GridPoint2(68,31);
        Entity volatile1 = PlatformFactory.createVolatilePlatform(2f,1.5f);
        volatile1.setScale(2f,0.5f);
        spawnEntityAt(volatile1, volatile1Pos,false, false);
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
