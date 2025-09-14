package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.PressurePlateComponent;
import com.csse3200.game.components.lighting.ConeLightPanningTaskComponent;
import com.csse3200.game.components.minimap.MinimapDisplay;
import com.csse3200.game.components.AutonomousBoxComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.obstacles.DoorComponent;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.physics.ObjectContactListener;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.MinimapService;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.utils.math.RandomUtils;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.tooltip.TooltipSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/** Forest area for the demo game with trees, a player, and some enemies. */
public class ForestGameArea extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(ForestGameArea.class);
  private static final int NUM_TREES = 7;
  private static final int NUM_GHOSTS = 2;
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
          "images/key.png",
          "images/hex_grass_1.png",
          "images/hex_grass_2.png",
          "images/hex_grass_3.png",
          "images/iso_grass_1.png",
          "images/iso_grass_2.png",
          "images/iso_grass_3.png",
          "images/drone.png",
          "images/bomb.png",
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
          "images/minimap_player_marker.png",
          "images/door_open.png",
          "images/door_closed.png",
          "images/pressure_plate_unpressed.png",
          "images/pressure_plate_pressed.png",
          "images/dash_powerup.png",
          "images/glide_powerup.png",
          "images/camera-body.png",
          "images/camera-lens.png",
          "images/PLAYER.png"
  };
  private static final String[] forestTextureAtlases = {
          "images/terrain_iso_grass.atlas",
          "images/ghost.atlas",
          "images/ghostKing.atlas",
          "images/drone.atlas",
          "images/security-camera.atlas",
          "images/PLAYER.atlas"
  };
  private static final String[] forestSounds = {"sounds/Impact4.ogg", "sounds" +
          "/chimesound.mp3"};
  private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";
  private static final String[] forestMusic = {backgroundMusic};

  private final TerrainFactory terrainFactory;

  private Entity door;
  private Timer.Task doorCloseTask;
  /**
   * Initialise this ForestGameArea to use the provided TerrainFactory.
   * @param terrainFactory TerrainFactory used to create the terrain for the GameArea.
   * @requires terrainFactory != null
   */
  public ForestGameArea(TerrainFactory terrainFactory) {
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
    // spawnTrees();
    createMinimap();
    playMusic();
  }

  /**
   * Load entities. Terrain must be loaded beforehand.
   * Player must be spawned beforehand if spawning enemies.
   */
  protected void loadEntities() {
    //spawnDrone();             // Play with idle/chasing drones (unless chasing)
    //spawnPatrollingDrone();   // Play with patrolling/chasing drones
    //spawnBomberDrone();       // Play with bomber drones
    //spawnGhosts();
    //spawnGhostKing();

    spawnPlatform(); //Testing platform
    spawnElevatorPlatform();

    spawnBoxes();  // uncomment this method when you want to play with boxes
    spawnButtons();

//    door = spawnDoor();
    spawnPressurePlates() ;

    spawnLights(); // uncomment to spawn in lights
    // spawnKey();
    spawnTraps();
    spawnGate();

    spawnUpgrade("dash", 9, 6);
    spawnUpgrade("glider", 7, 6);
    spawnUpgrade("jetpack", 5, 6);
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

  private void spawnDrone() {
    GridPoint2 spawnTile = new GridPoint2(16, 11);
    Vector2 spawnWorldPos = terrain.tileToWorldPosition(spawnTile);

    Entity drone = EnemyFactory.createDrone(player, spawnWorldPos); // pass world pos here
    spawnEntityAt(drone, spawnTile, true, true);

  }

  private void spawnPatrollingDrone() {
    GridPoint2 spawnTile = new GridPoint2(4, 11);

    Vector2[] patrolRoute = {
            terrain.tileToWorldPosition(spawnTile),
            terrain.tileToWorldPosition(new GridPoint2(6, 11)),
            terrain.tileToWorldPosition(new GridPoint2(8, 11))
    };
    Entity patrolDrone = EnemyFactory.createPatrollingDrone(player, patrolRoute);
    spawnEntityAt(patrolDrone, spawnTile, false, false); // Changed to false so patrol doesn't look weird
  }

  private void spawnBomberDrone() {
    GridPoint2 spawnTile = new GridPoint2(2, 11);
    Vector2 spawnWorldPos = terrain.tileToWorldPosition(spawnTile);

    Entity bomberDrone = EnemyFactory.createBomberDrone(player, spawnWorldPos);
    spawnEntityAt(bomberDrone, spawnTile, true, true);
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

    GridPoint2 step4Pos = new GridPoint2(20,6);
    Entity step4 = PlatformFactory.createStaticPlatform();
    step4.setScale(2,1);
    spawnEntityAt(step4, step4Pos, false, false);

    GridPoint2 longPlatformPos = new GridPoint2(0,11);
    Entity longPlatform = PlatformFactory.createStaticPlatform();
    longPlatform.setScale(10,0.1f);
    spawnEntityAt(longPlatform, longPlatformPos, false, false);

    float ts = terrain.getTileSize();
    GridPoint2 movingPos = new GridPoint2(8,6);
    Vector2 offsetWorld  = new Vector2(6f * ts, 0f);
    float speed = 2f;
    Entity movingPlatform = PlatformFactory.createMovingPlatform(offsetWorld, speed);
    movingPlatform.setScale(2,1);
    spawnEntityAt(movingPlatform, movingPos, false, false);
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
    Vector2 safeSpotPos = new Vector2(((spawnPos.x)/2)-2, ((spawnPos.y)/2)+2); // Need to be manually tweaked
    Entity spikes = TrapFactory.createSpikes(spawnPos, safeSpotPos);
    spawnEntityAt(spikes, spawnPos, true,  true);
  }

  private void spawnElevatorPlatform() {
      float ts = terrain.getTileSize();

      // Elevator: moves up 4 tiles when triggered
      Entity elevator = PlatformFactory.createButtonTriggeredPlatform(
              new Vector2(0, 4f * ts), // offset: 4 tiles up
              2f                       // speed
      );
      spawnEntityAt(elevator, new GridPoint2(10, 8), false, false);

      // Button to trigger it
      Entity button = ButtonFactory.createButton(false, "platform", "left");
      spawnEntityAt(button, new GridPoint2(10, 7), true, true);

      // Link button to platform
      button.getEvents().addListener("buttonToggled", (Boolean isPushed) -> {
        if (isPushed) {
          logger.info("Button toggled ON — activating elevator");
          elevator.getEvents().trigger("activatePlatform");
        } else {
          logger.info("Button toggled OFF — stopping elevator");
          elevator.getEvents().trigger("deactivatePlatform");
        }
      });
  }

  private void spawnButtons() {
    Entity button = ButtonFactory.createButton(false, "platform", "left");
    button.addComponent(new TooltipSystem.TooltipComponent("Platform Button\nPress E to interact", TooltipSystem.TooltipStyle.DEFAULT));
    spawnEntityAt(button, new GridPoint2(26,4), true,  true);

    Entity button2 = ButtonFactory.createButton(false, "door", "right");
    button2.addComponent(new TooltipSystem.TooltipComponent("Door Button\nPress E to interact", TooltipSystem.TooltipStyle.DEFAULT));
    spawnEntityAt(button2, new GridPoint2(20,5), true,  true);

    Entity button3 = ButtonFactory.createButton(false, "nothing", "up");
    spawnEntityAt(button3, new GridPoint2(15,12), true,  true);

    Entity button4 = ButtonFactory.createButton(false, "nothing", "down");
    spawnEntityAt(button4, new GridPoint2(15,7), true,  true);

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
    key.addComponent(new TooltipSystem.TooltipComponent("Collect the key", TooltipSystem.TooltipStyle.SUCCESS));
    spawnEntityAt(key, new GridPoint2(17,19), true, true);
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
    if (upgradeID == "jetpack") {
      Entity upgrade = CollectableFactory.createJetpackUpgrade();
      spawnEntityAt(upgrade, new GridPoint2(posx, posy), true, true);
    }
  }

  private void spawnPressurePlates() {
    Entity plate = PressurePlateFactory.createPressurePlate();
    PressurePlateComponent comp = plate.getComponent(PressurePlateComponent.class);
    comp.setTextures("images/pressure_plate_unpressed.png", "images/pressure_plate_pressed.png");

    plate.getEvents().addListener("plateToggled", (Boolean pressed) -> {
      if (door == null) return;

      if (pressed) {
        if (doorCloseTask != null) { doorCloseTask.cancel(); doorCloseTask = null; }
        door.getEvents().trigger("openDoor");
      } else {
        doorCloseTask = new Timer.Task() {
          @Override public void run() { door.getEvents().trigger("closeDoor"); doorCloseTask = null; }
        };
        Timer.schedule(doorCloseTask, 10f);
      }
    });

    GridPoint2 platePos = new GridPoint2(5, terrain.getMapBounds(0).y - 20);
    spawnEntityAt(plate, platePos, true, true);
  }

  private Entity spawnDoor() {
    Entity d = ObstacleFactory.createDoor("door", this, "sprint1");
    d.addComponent(new TooltipSystem.TooltipComponent(
            "Unlock the door with the key", TooltipSystem.TooltipStyle.DEFAULT));
    d.addComponent(new com.csse3200.game.components.DoorControlComponent()); // <-- add this
    d.setScale(1,2);
    spawnEntityAt(d, new GridPoint2(28, 5), true, true);
    return d;
  }

  private void spawnLights() {
    // see the LightFactory class for more details on spawning these
    Entity securityLight = SecurityCameraFactory.createSecurityCamera(player, 20f, "1");

    spawnEntityAt(securityLight, new GridPoint2(12, 16), true, true);
  }

  private void spawnGate() {
    /*
    Creates gate to test
    */
    GridPoint2 gatePos = new GridPoint2((int) 28, 5);
    Entity gate = ObstacleFactory.createDoor("door", this, "cave");
    gate.setScale(1, 2);
    gate.getComponent(DoorComponent.class).openDoor();
    spawnEntityAt(gate, gatePos, true, true);
  }

  private void playMusic() {
    Music music = ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class);
    music.setLooping(true);
    music.setVolume(UserSettings.getMusicVolumeNormalized());
    music.play();
  }

  protected void loadAssets() {
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