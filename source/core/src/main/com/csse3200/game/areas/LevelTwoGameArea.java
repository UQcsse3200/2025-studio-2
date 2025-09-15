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
            "images/PLAYER.atlas"
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
        GridPoint2 spawnPos =  new GridPoint2(12,0);
        Entity deathZone = DeathZoneFactory.createDeathZone(spawnPos, new Vector2(5,10));
        spawnEntityAt(deathZone, spawnPos, true,  true);
    }
    private void spawnWalls(){
    }
    private void spawnPlatforms(){
        GridPoint2 groundPos1 = new GridPoint2(0, 0);
        Entity ground1 = PlatformFactory.createStaticPlatform();
        ground1.setScale(40,2);
        spawnEntityAt(ground1, groundPos1, false, false);
    }
    public void spawnDoor() {
        Entity door = ObstacleFactory.createDoor("door", this);
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
    private void displayUI() {
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Level Two Game Area"));
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
