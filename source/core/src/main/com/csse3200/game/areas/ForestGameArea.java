package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.minimap.MinimapDisplay;
import com.csse3200.game.components.AutonomousBoxComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.physics.ObjectContactListener;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.utils.math.RandomUtils;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.tooltip.TooltipSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/** Forest area for the demo game with trees, a player, and some enemies. */
public class ForestGameArea extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(ForestGameArea.class);
  private static final int NUM_TREES = 7;
  private static final int NUM_GHOSTS = 0;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);
  private static final float WALL_WIDTH = 0.1f;
  private static boolean keySpawned;
  private static final String[] forestTextures = {
    "images/box_boy_leaf.png",
    "images/tree.png",
    "images/ghost_king.png",
    "images/ghost_1.png",
    "images/grass_1.png",
    "images/grass_2.png",
    "images/grass_3.png",
    "images/key_tester.png",
    "images/hex_grass_1.png",
    "images/hex_grass_2.png",
    "images/hex_grass_3.png",
    "images/iso_grass_1.png",
    "images/iso_grass_2.png",
    "images/iso_grass_3.png",
    "images/platform.png",
    "images/gate.png",
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
    "images/minimap_player_marker.png"
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
  public ForestGameArea(TerrainFactory terrainFactory) {
    super();
    this.terrainFactory = terrainFactory;
  }

  /** Create the game area, including terrain, static entities (trees), dynamic entities (player) */
  @Override
  public void create() {
    PhysicsEngine engine =  ServiceLocator.getPhysicsService().getPhysics();
    engine.getWorld().setContactListener(new ObjectContactListener());
    loadAssets();
    loadLevel();
  }

  public void reset() {
    // debug
    // for (Entity entity : areaEntities) {
    //   System.out.println(entity);
    // }
    super.dispose();
    loadLevel();
  }

  private void loadLevel() {
    displayUI();
    spawnTerrain();
    spawnTrees();
    player = spawnPlayer();
    player.getEvents().addListener("reset", this::reset); //debug
    spawnGhosts();
    //spawnGhostKing();

    MinimapDisplay minimapDisplay = createMinimap();

    player = spawnPlayer();
    //spawnGhosts();
    //spawnGhostKing();
    spawnPlatform(); //Testing platform

    spawnBoxes();  // uncomment this method when you want to play with boxes
    spawnButtons();

    spawnLights(); // uncomment to spawn in lights

    spawnTraps();
    playMusic();
  }

  private MinimapDisplay createMinimap() {
    Texture minimapTexture =
        ServiceLocator.getResourceService().getAsset("images/minimap_forest_area.png", Texture.class);

    MinimapDisplay.MinimapOptions options = new MinimapDisplay.MinimapOptions();
    options.position = MinimapDisplay.MinimapPosition.BOTTOM_RIGHT;

    float tileSize = terrain.getTileSize();
    Vector2 worldSize =
        new Vector2(terrain.getMapBounds(0).x * tileSize, terrain.getMapBounds(0).y * tileSize);
    MinimapDisplay minimapDisplay =
        new MinimapDisplay(minimapTexture, new Vector2(), worldSize, 150f, options);

    Entity minimapEntity = new Entity();
    minimapEntity.addComponent(minimapDisplay);
    spawnEntity(minimapEntity);

    return minimapDisplay;
  }

  private void displayUI() {
    Entity ui = new Entity();
    ui.addComponent(new GameAreaDisplay("Box Forest"));
    ui.addComponent(new TooltipSystem.TooltipDisplay());
    spawnEntity(ui);
  }

  private void spawnTerrain() {
    // Background terrain
    terrain = terrainFactory.createTerrain(TerrainType.FOREST_DEMO);
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
    //spawnEntityAt(ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH), GridPoint2Utils.ZERO, false, false);
    spawnEntityAt(ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH),
            new GridPoint2(0, 4), false, false);
  }

  private void spawnTrees() {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

    for (int i = 1; i < NUM_TREES; i++) {
      //GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
      //Entity tree = ObstacleFactory.createTree();
      //spawnEntityAt(tree, randomPos, true, false);
      GridPoint2 position = new GridPoint2(3*i + 10, 4 + (2*i));
      Entity tree = ObstacleFactory.createTree();
      spawnEntityAt(tree, position, true, false);
    }
  }

  private Entity spawnPlayer() {
    Entity newPlayer = PlayerFactory.createPlayer();
    spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
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
    GridPoint2 groundPos = new GridPoint2(0, 2);
    Entity ground = PlatformFactory.createStaticPlatform();
    ground.setScale(15,1);
    spawnEntityAt(ground, groundPos, false, false);

    GridPoint2 step1Pos = new GridPoint2(5,3);
    Entity step1 = PlatformFactory.createStaticPlatform();
    step1.setScale(2,1);
    spawnEntityAt(step1, step1Pos, false, false);

    GridPoint2 step2Pos = new GridPoint2(10,4);
    Entity step2 = PlatformFactory.createStaticPlatform();
    step2.setScale(2,1);
    spawnEntityAt(step2, step2Pos, false, false);

    GridPoint2 step3Pos = new GridPoint2(16,5);
    Entity step3 = PlatformFactory.createStaticPlatform();
    step3.setScale(2,1);
    spawnEntityAt(step3, step3Pos, false, false);

    GridPoint2 step4Pos = new GridPoint2(20,7);
    Entity step4 = PlatformFactory.createStaticPlatform();
    step4.setScale(2,1);
    spawnEntityAt(step4, step4Pos, false, false);

    GridPoint2 longPlatformPos = new GridPoint2(0,11);
    Entity longPlatform = PlatformFactory.createStaticPlatform();
    longPlatform.setScale(10,0.1f);
    spawnEntityAt(longPlatform, longPlatformPos, false, false);

  }
  private void spawnBoxes() {

      // Static box
      Entity staticBox = BoxFactory.createStaticBox();
      staticBox.addComponent(new TooltipSystem.TooltipComponent("Static Box\nThis box is fixed, you cannot push it!", TooltipSystem.TooltipStyle.DEFAULT));
      spawnEntityAt(staticBox, new GridPoint2(10,20), true,  true);

      // Moveable box
      Entity moveableBox = BoxFactory.createMoveableBox();
      moveableBox.addComponent(new TooltipSystem.TooltipComponent("Moveable Box\nYou can push this box around!", TooltipSystem.TooltipStyle.SUCCESS));
      spawnEntityAt(moveableBox, new GridPoint2(17,17), true,  true);

      // Autonomous box
      float startX = 3f;
      float endX = 10f;
      float y = 17f;
      float speed = 2f;

      Entity autonomousBox = BoxFactory.createAutonomousBox(startX, endX, speed);
      spawnEntityAt(autonomousBox, new GridPoint2((int)startX, (int)y), true, true);
  }

  private void spawnTraps() {
    GridPoint2 spawnPos =  new GridPoint2(7,15);
    Entity spikes = TrapFactory.createSpikes(spawnPos);
    spawnEntityAt(spikes, spawnPos, true,  true);
  }

  private void spawnButtons() {
    Entity button = ButtonFactory.createButton(false, "platform");
    button.addComponent(new TooltipSystem.TooltipComponent("Platform Button\nPress E to interact", TooltipSystem.TooltipStyle.DEFAULT));
    spawnEntityAt(button, new GridPoint2(25,15), true,  true);

    Entity button2 = ButtonFactory.createButton(false, "door");
    button2.addComponent(new TooltipSystem.TooltipComponent("Door Button\nPress E to interact", TooltipSystem.TooltipStyle.DEFAULT));
    spawnEntityAt(button2, new GridPoint2(15,15), true,  true);

    Entity button3 = ButtonFactory.createButton(false, "nothing");
    spawnEntityAt(button3, new GridPoint2(25,23), true,  true);

    //listener to spawn key when door button pushed
    button2.getEvents().addListener("buttonToggled", (Boolean isPushed) -> {
      if (isPushed && !keySpawned) {
        spawnKey();
        keySpawned = true;
      }
    });

  }

  public void spawnKey() {
      Entity key = CollectableFactory.createKey("door");
      spawnEntityAt(key, new GridPoint2(17,17), true, true);
  }

  private void spawnLights() {
    // see the LightFactory class for more details on spawning these
    Entity securityLight = LightFactory.createSecurityLight(
              player,
              PhysicsLayer.OBSTACLE,
              128,
              Color.GREEN,
              10f,
              0f,
              35f
      );
      spawnEntityAt(securityLight, new GridPoint2(5, 5), true, true);
  }

  private void playMusic() {
    Music music = ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class);
    music.setLooping(true);
    music.setVolume(UserSettings.getMusicVolumeNormalized());
    music.play();
  }

  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(forestTextures);
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
    resourceService.unloadAssets(forestTextures);
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
