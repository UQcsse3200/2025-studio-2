package com.csse3200.game.areas;


import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.GridFactory;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.GridPoint2Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TemplateGameArea extends GameArea {
    private static final GridPoint2 mapSize = new GridPoint2(100,70);
    private static final float WALL_THICKNESS = 0.1f;
    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

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
            "images/spikes_sprite.png",
            "images/TechWallBase.png",
            "images/TechWallVariant1.png",
            "images/TechWallVariant2.png",
            "images/TechWallVariant3.png",
            "images/platform.png",
            "images/gate.png",
            "images/door_open.png",
            "images/door_closed.png",
            "images/Gate_open.png",
            "images/box_boy_leaf.png",
            "images/button.png",
            "images/button_pushed.png",
            "images/blue_button_pushed.png",
            "images/drone.png",
            "images/bomb.png",
            "images/camera-body.png",
            "images/camera-lens.png"
    };
    private static final String BACKGROUND_MUSIC = "sounds/BGM_03_mp3.mp3";
    private static final String[] musics = {BACKGROUND_MUSIC};
    private static final String[] gameSounds = {"sounds/Impact4.ogg",
            "sounds/chimesound.mp3"};
    private static final String[] gameTextureAtlases = {
            "images/PLAYER.atlas"
    };
    private static final Logger logger = LoggerFactory.getLogger(TemplateGameArea.class);
    private final GridFactory gridFactory;

    public TemplateGameArea(GridFactory gridFactory) {
        super();
        this.gridFactory = gridFactory;
    }
    protected void loadPrerequisites() {
        displayUI();
        spawnGrid();
        createMinimap(ServiceLocator.getResourceService().getAsset("images/minimap_forest_area.png", Texture.class));
        playMusic();
    }

    private void spawnGrid() {
        grid = gridFactory.createGrid(mapSize, 0.5f);
        spawnEntity(new Entity().addComponent(grid));

        // Grid walls
        float tileSize = grid.getTileSize();
        GridPoint2 tileBounds = grid.getMapBounds();
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
            new GridPoint2(0, tileBounds.y),
            false,
            false);
        // Bottom
        spawnEntityAt(ObstacleFactory.createWall(worldBounds.x, WALL_THICKNESS),
            new GridPoint2(0, 4), false, false);
    }

    protected void loadEntities() {
        // Add entities to be loaded here
    }
    private void playMusic() {
        Music music = ServiceLocator.getResourceService().getAsset(BACKGROUND_MUSIC, Music.class);
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
        ui.addComponent(new GameAreaDisplay("Template Game Area"));
        ui.addComponent(new TooltipSystem.TooltipDisplay());
        spawnEntity(ui);
    }

    protected void loadAssets() {
        logger.debug("Loading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.loadTextures(gameTextures);
        resourceService.loadMusic(musics);
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
        ServiceLocator.getResourceService().getAsset(BACKGROUND_MUSIC, Music.class).stop();
        this.unloadAssets();
    }
}
