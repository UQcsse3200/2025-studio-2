package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.minimap.MinimapDisplay;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.physics.ObjectContactListener;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.services.MinimapService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.utils.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.csse3200.game.components.obstacles.DoorComponent;

import java.util.ArrayList;
import java.util.List;

/** Forest area for the demo game with trees, a player, and some enemies. */
public class CaveGameArea extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(CaveGameArea.class);
  private static final int NUM_TREES = 0;
  private static final int NUM_GHOSTS = 0;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);
  private static final float WALL_WIDTH = 0.1f;
  private static final String[] caveTextures = {
    "images/cave_1.png",
    "images/cave_2.png",
    "images/platform.png",
    "images/gate.png",
    "images/box_boy_leaf.png",
    "images/box_white.png",
    "images/box_blue.png",
    "images/tree.png",
    "images/ghost_king.png",
    "images/ghost_1.png",
    "images/grass_1.png",
    "images/grass_2.png",
    "images/grass_3.png",
    "images/key.png",
    "images/door_open.png",
    "images/door_closed.png",
    "images/key.png",
    "images/hex_grass_1.png",
    "images/hex_grass_2.png",
    "images/hex_grass_3.png",
    "images/iso_grass_1.png",
    "images/iso_grass_2.png",
    "images/iso_grass_3.png",
    "images/drone.png",
    "images/bomb.png",
    "images/button.png",
    "images/button_pushed.png",
    "images/blue_button.png",
    "images/blue_button_pushed.png",
    "images/red_button.png",
    "images/red_button_pushed.png",
    "images/box_blue.png",
    "images/box_orange.png",
    "images/box_red.png",
    "images/box_white.png",
    "images/spikes_sprite.png",
    "images/blue_button.png",
    "images/blue_button_pushed.png",
    "images/red_button.png",
    "images/red_button_pushed.png",
    "images/minimap_forest_area.png",
    "images/minimap_player_marker.png",
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
    "images/iso_grass_3.png",
    "images/minimap_player_marker.png"
  };
  private static final String[] forestTextureAtlases = {
    "images/terrain_iso_grass.atlas",
          "images/ghost.atlas",
          "images/ghostKing.atlas",
          "images/drone.atlas",
          "images/PLAYER.atlas"
  };
  private static final String[] forestSounds = {"sounds/Impact4.ogg", "sounds" +
          "/chimesound.mp3"};
  private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";
  private static final String[] forestMusic = {backgroundMusic};

  private final TerrainFactory terrainFactory;


  /**
   * Initialise this ForestGameArea to use the provided TerrainFactory.
   * @param terrainFactory TerrainFactory used to create the terrain for the GameArea.
   * @requires terrainFactory != null
   */
  public CaveGameArea(TerrainFactory terrainFactory) {
    super();
    this.terrainFactory = terrainFactory;
  }

  /**
   * Load terrain, UI, music. Must be done before spawning entities.
   * Assets are loaded separately.
   * Entities spawned separately.
   */
  protected void loadPrerequisites() {
    displayUI();

    spawnTerrain();
    //spawnTrees();
    createMinimap();

    playMusic();
  }

  /**
   * Load entities. Terrain must be loaded beforehand.
   * Player must be spawned beforehand if spawning enemies.
   */
  protected void loadEntities() {
    //spawnGhosts();
    //spawnGhostKing();
    spawnPlatform(); //Testing platform
    spawnGate(); //Testing gate
    spawnDeathZone();
  }

  private void displayUI() {
    Entity ui = new Entity();
    ui.addComponent(new GameAreaDisplay("Box Cave"));
    ui.addComponent(new TooltipSystem.TooltipDisplay());
    spawnEntity(ui);
  }

  private void spawnTerrain() {
    // Background terrain
    terrain = terrainFactory.createTerrain(TerrainType.CAVE_ORTHO);
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

  private void createMinimap() {
    Texture minimapTexture =
            ServiceLocator.getResourceService().getAsset("images/minimap_forest_area.png", Texture.class);

    float tileSize = terrain.getTileSize();
    Vector2 worldSize =
            new Vector2(terrain.getMapBounds(0).x * tileSize, terrain.getMapBounds(0).y * tileSize);
    ServiceLocator.registerMinimapService(new MinimapService(minimapTexture, worldSize, new Vector2()));

    MinimapDisplay.MinimapOptions options = getMinimapOptions();

    MinimapDisplay minimapDisplay =
            new MinimapDisplay(150f, options);

    Entity minimapEntity = new Entity();
    minimapEntity.addComponent(minimapDisplay);
    spawnEntity(minimapEntity);
  }

  private static MinimapDisplay.MinimapOptions getMinimapOptions() {
    MinimapDisplay.MinimapOptions options = new MinimapDisplay.MinimapOptions();
    options.position = MinimapDisplay.MinimapPosition.BOTTOM_RIGHT;
    return options;
  }

  private void spawnTrees() {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

    for (int i = 0; i < NUM_TREES; i++) {
      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
      Entity tree = ObstacleFactory.createTree();
      spawnEntityAt(tree, randomPos, true, false);
    }
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

  private void spawnGhosts() {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

    for (int i = 0; i < NUM_GHOSTS; i++) {
      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
      Entity ghost = NPCFactory.createGhost(player);
      spawnEntityAt(ghost, randomPos, true, true);
    }
  }

  private void spawnGhostKing() {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

    GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
    Entity ghostKing = NPCFactory.createGhostKing(player);
    spawnEntityAt(ghostKing, randomPos, true, true);
  }
  //Platform spawn in testing
  private void spawnPlatform() {
    /*
    Creates floor and several steps to test jumping
    */
    GridPoint2 groundPos = new GridPoint2(0, 3);
    Entity ground = PlatformFactory.createStaticPlatform();
    ground.setScale(8,0.5f);
    spawnEntityAt(ground, groundPos, false, false);

    GridPoint2 ground2Pos = new GridPoint2(20, 3);
    Entity ground2 = PlatformFactory.createStaticPlatform();
    ground2.setScale(5,0.5f);
    spawnEntityAt(ground2, ground2Pos, false, false);

    GridPoint2 step1Pos = new GridPoint2(5,5);
    Entity step1 = PlatformFactory.createStaticPlatform();
    step1.setScale(2,1);
    spawnEntityAt(step1, step1Pos, false, false);

    GridPoint2 step2Pos = new GridPoint2(10,6);
    Entity step2 = PlatformFactory.createStaticPlatform();
    step2.setScale(2,1);
    spawnEntityAt(step2, step2Pos, false, false);

    GridPoint2 step3Pos = new GridPoint2(16,5);
    Entity step3 = PlatformFactory.createStaticPlatform();
    step3.setScale(2,1);
    //spawnEntityAt(step3, step3Pos, false, false);

    GridPoint2 step4Pos = new GridPoint2(20,7);
    Entity step4 = PlatformFactory.createStaticPlatform();
    step4.setScale(2,1);
    //spawnEntityAt(step4, step4Pos, false, false);

    GridPoint2 longPlatformPos = new GridPoint2(0,11);
    Entity longPlatform = PlatformFactory.createStaticPlatform();
    longPlatform.setScale(10,0.1f);
    spawnEntityAt(longPlatform, longPlatformPos, false, false);

    GridPoint2 wallPos = new GridPoint2(15, 1);
    Entity wall = PlatformFactory.createStaticPlatform();
    wall.setScale(0.5f,1.5f);
    spawnEntityAt(wall, wallPos, false, false);

    GridPoint2 wall2Pos = new GridPoint2(20, 1);
    Entity wall2 = PlatformFactory.createStaticPlatform();
    wall2.setScale(0.5f,1.5f);
    spawnEntityAt(wall2, wall2Pos, false, false);

  }

    private void spawnWalls() {
        float ts = terrain.getTileSize();

        // Tall wall on the left
        GridPoint2 wall1Pos = new GridPoint2(8, 22);
        Entity wall1 = WallFactory.createWall(
                0f, 0f,
                1f * ts, 5f * ts,
                "images/walls.png"
        );
        spawnEntityAt(wall1, wall1Pos, false, false);

        // Shorter wall in the middle
        GridPoint2 wall2Pos = new GridPoint2(8, 6);
        Entity wall2 = WallFactory.createWall(
                0f, 0f,
                1f * ts, 3f * ts,
                "images/tile.png"
        );
        spawnEntityAt(wall2, wall2Pos, false, false);

        // Another tall wall further right
        GridPoint2 wall3Pos = new GridPoint2(18, 4);
        Entity wall3 = WallFactory.createWall(
                0f, 0f,
                1f * ts, 6f * ts,
                "images/walls.png"
        );
        spawnEntityAt(wall3, wall3Pos, false, false);
    }
  private void spawnGate() {
    /*
    Creates gate to test
    */
    GridPoint2 gatePos = new GridPoint2((int) 28, 5);
    Entity gate = ObstacleFactory.createDoor("door", this, "forest");
    gate.setScale(1, 2);
    gate.getComponent(DoorComponent.class).openDoor();
    spawnEntityAt(gate, gatePos, true, true);
  }

  private void spawnDeathZone() {
    GridPoint2 spawnPos =  new GridPoint2(18,0);
    Entity deathZone = DeathZoneFactory.createDeathZone(spawnPos, new Vector2(5,10));
    spawnEntityAt(deathZone, spawnPos, true,  true);
  }

  private void playMusic() {
    Music music = ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class);
    music.setLooping(true);
    music.setVolume(0.1f);
    music.play();
  }

  protected void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(caveTextures);
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
    resourceService.unloadAssets(caveTextures);
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
