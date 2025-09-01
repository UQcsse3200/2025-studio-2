package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.utils.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SprintOneGameArea extends GameArea {
    private static final Logger logger = LoggerFactory.getLogger(SprintOneGameArea.class);
    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);
    private static final float WALL_WIDTH = 0.1f;
    private static final String[] gameTextures = {
            "images/cave_1.png",
            "images/cave_2.png",
            "images/TechWallBase.png",
            "images/TechWallVariant1.png",
            "images/TechWallVariant2.png",
            "images/TechWallVariant3.png",
            "images/platform.png",
            "images/gate.png",
            "images/box_boy_leaf.png",
            "images/tree.png",
            "images/ghost_king.png",
            "images/ghost_1.png",
            "images/grass_1.png",
            "images/grass_2.png",
            "images/grass_3.png",
            "images/hex_grass_1.png",
            "images/hex_grass_2.png",
            "images/hex_grass_3.png",
            "images/iso_grass_1.png",
            "images/iso_grass_2.png",
            "images/iso_grass_3.png"
    };
    private static final String[] forestTextureAtlases = {
            "images/terrain_iso_grass.atlas", "images/ghost.atlas", "images/ghostKing.atlas"
    };
    private static final String[] forestSounds = {"sounds/Impact4.ogg", "sounds" +
            "/chimesound.mp3"};
    private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";
    private static final String[] forestMusic = {backgroundMusic};

    private final TerrainFactory terrainFactory;

    private Entity player;

    /**
     * Initialise this ForestGameArea to use the provided TerrainFactory.
     * @param terrainFactory TerrainFactory used to create the terrain for the GameArea.
     * @requires terrainFactory != null
     */
    public SprintOneGameArea(TerrainFactory terrainFactory) {
        super();
        this.terrainFactory = terrainFactory;
    }

    /** Create the game area, including terrain, static entities (trees), dynamic entities (player) */
    @Override
    public void create() {
        loadAssets();

        displayUI();

        spawnTerrain();
        player = spawnPlayer();
        spawnPlatform();
        spawnGate();
        spawnBoxes();
        playMusic();
    }

    private void displayUI() {
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Sprint one demo level"));
        spawnEntity(ui);
    }

    private void spawnTerrain() {
        // Background terrain
        terrain = terrainFactory.createTerrain(TerrainType.SPRINT_ONE_ORTHO);
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
                new GridPoint2(0, tileBounds.y),
                false,
                false);
        // Bottom
        spawnEntityAt(
                ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH), GridPoint2Utils.ZERO, false, false);
    }

    private Entity spawnPlayer() {
        Entity newPlayer = PlayerFactory.createPlayer();
        spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
        return newPlayer;
    }
    //Platform spawn in testing
    private void spawnPlatform() {
    /*
    Creates floor and several steps to test jumping
    */
        GridPoint2 groundPos = new GridPoint2(0, 2);
        Entity ground = PlatformFactory.createStaticPlatform();
        ground.setScale(15,1);
        spawnEntityAt(ground, groundPos, false, false);

        GridPoint2 step1Pos = new GridPoint2(5,3);
        Entity step1 = PlatformFactory.createStaticPlatform();
        step1.setScale(2,1);
        spawnEntityAt(step1, step1Pos, false, false);

        float ts = terrain.getTileSize();
        GridPoint2 movingPos = new GridPoint2(8,6);
        Vector2 offsetWorld  = new Vector2(6f * ts, 1f);
        float speed = 2f;
        Entity movingPlatform = PlatformFactory.createMovingPlatform(offsetWorld, speed);
        movingPlatform.setScale(2,1);
        spawnEntityAt(movingPlatform, movingPos, false, false);

    }
    private void spawnBoxes() {

        // Static box
        Entity staticBox = BoxFactory.createStaticBox();
        staticBox.addComponent(new TooltipSystem.TooltipComponent("Static Box\nThis box is fixed, you cannot push it!", TooltipSystem.TooltipStyle.DEFAULT));
        spawnEntityAt(staticBox, new GridPoint2(13,13), true,  true);

        // Moveable box
        Entity moveableBox = BoxFactory.createMoveableBox();
        moveableBox.addComponent(new TooltipSystem.TooltipComponent("Moveable Box\nYou can push this box around!", TooltipSystem.TooltipStyle.SUCCESS));
        spawnEntityAt(moveableBox, new GridPoint2(17,17), true,  true);

        // Add other types of boxes here
    }
    private void spawnGate() {
    /*
    Creates gate to test
    */
        float gateX = terrain.getMapBounds(0).x * terrain.getTileSize();
        GridPoint2 gatePos = new GridPoint2((int) 28, 4);
        Entity gate = GateFactory.createGate();
        gate.setScale(1, 2);
        spawnEntityAt(gate, gatePos, false, false);
    }

    private void playMusic() {
        Music music = ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class);
        music.setLooping(true);
        music.setVolume(0.1f);
        music.play();
    }

    private void loadAssets() {
        logger.debug("Loading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.loadTextures(gameTextures);
        resourceService.loadTextureAtlases(forestTextureAtlases);
        resourceService.loadSounds(forestSounds);
        resourceService.loadMusic(forestMusic);

        while (!resourceService.loadForMillis(10)) {
            // This could be upgraded to a loading screen
            logger.info("Loading... {}%", resourceService.getProgress());
        }
    }

    private void unloadAssets() {
        logger.debug("Unloading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.unloadAssets(gameTextures);
        resourceService.unloadAssets(forestTextureAtlases);
        resourceService.unloadAssets(forestSounds);
        resourceService.unloadAssets(forestMusic);
    }

    @Override
    public void dispose() {
        super.dispose();
        ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class).stop();
        this.unloadAssets();
    }
}
