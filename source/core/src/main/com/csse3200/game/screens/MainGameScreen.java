package com.csse3200.game.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.vfx.VfxManager;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.*;
import com.csse3200.game.areas.CaveGameArea;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.SprintOneGameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.maingame.MainGameActions;
import com.csse3200.game.components.pausemenu.PauseMenuDisplay;
import com.csse3200.game.components.pausemenu.PauseMenuDisplay.Tab;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.input.InputService;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.lighting.SecurityCamRetrievalService;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.maingame.MainGameExitDisplay;
import com.csse3200.game.components.gamearea.PerformanceDisplay;
import com.csse3200.game.input.PauseInputComponent;
import com.csse3200.game.ui.cutscene.CutsceneArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The game screen containing the main game.
 *
 * <p>Details on libGDX screens: https://happycoding.io/tutorials/libgdx/game-screens
 */
public class MainGameScreen extends ScreenAdapter {
  private static final Logger logger = LoggerFactory.getLogger(MainGameScreen.class);
  private static final String[] mainGameTextures = {
          "images/playerstats/health.png",
          "images/playerstats/stamina.png"
  };
  private static final Vector2 CAMERA_POSITION = new Vector2(7.5f, 7.5f);

  private final GdxGame game;
  private final Renderer renderer;
  private final PhysicsEngine physicsEngine;
  private final LightingEngine lightingEngine;
  private boolean paused = false;
  private PauseMenuDisplay pauseMenuDisplay;

  // Camera follow parameters
  private static final float DEADZONE_H_FRAC = 0.40f; // Horizontal deadzone fraction (40% of screen width)
  private static final float DEADZONE_V_FRAC = 0.35f; // Vertical deadzone fraction (35% of screen height)
  private static final float CAMERA_LERP = 0.15f; // Camera smoothing factor (0.15 = smooth movement)

  private GameArea gameArea;
  private final TerrainFactory terrainFactory;

  private PauseInputComponent pauseInput;

  public MainGameScreen(GdxGame game) {
    this.game = game;

    logger.debug("Initialising main game screen services");
    ServiceLocator.registerTimeSource(new GameTime());

    PhysicsService physicsService = new PhysicsService();
    ServiceLocator.registerPhysicsService(physicsService);
    physicsEngine = physicsService.getPhysics();

    ServiceLocator.registerInputService(new InputService());
    ServiceLocator.registerResourceService(new ResourceService());

    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(new RenderService());
    ServiceLocator.registerVfxService(new VfxManager(Pixmap.Format.RGBA8888));

    renderer = RenderFactory.createRenderer();
    renderer.getCamera().getEntity().setPosition(CAMERA_POSITION);
    renderer.getDebug().renderPhysicsWorld(physicsEngine.getWorld());

    // Registering the new lighting service with the service manager
    LightingService lightingService = new LightingService(renderer.getCamera(), physicsEngine.getWorld());
    ServiceLocator.registerLightingService(lightingService);
    lightingEngine = lightingService.getEngine();

    // Registering a new security camera service
    ServiceLocator.registerSecurityCamRetrievalService(new SecurityCamRetrievalService());

    loadAssets();

    logger.debug("Initialising main game screen entities");
    terrainFactory = new TerrainFactory(renderer.getCamera());

//    gameArea = new SprintOneGameArea(terrainFactory);
    gameArea = new LevelOneGameArea(terrainFactory);
    //gameArea = new LevelTwoGameArea(terrainFactory);

    gameArea.create();

    gameArea.getEvents().addListener("doorEntered", (Entity player) -> {
      logger.info("Door entered in sprint1 with key {}", player);
      switchArea("cutscene1", player);
    });

    // Have to createUI after the game area is created since createUI
    // needs the player which is created in the game area
    createUI();
  }

  private void switchArea(String levelId, Entity player) {
    Gdx.app.postRunnable(() -> {
      if (!levelId.isEmpty()) {
  //        System.out.println("Area switched to " + levelId);
        GameArea oldArea = gameArea;

  //        TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());

        GameArea newArea = null;
        String newLevel = "";

          switch (levelId) {
              case "cutscene1" -> {
                  newArea = new CutsceneArea("cutscene-scripts/cutscene1.txt");
                  newLevel = "level2";
              }
              case "level2" -> {
                  newArea = new LevelTwoGameArea(terrainFactory);
                  newLevel = "cutscene2";
              }
              case "cutscene2" -> {
                  newArea = new CutsceneArea("cutscene-scripts/cutscene2.txt");
                  newLevel = "sprint1";
              }
              case "sprint1" -> {
                  newArea = new SprintOneGameArea(terrainFactory);
                  newLevel = "level2";
              }
          }

        if (newArea != null) {
          gameArea = newArea;
          String finalNewLevel = newLevel;
          newArea.getEvents().addListener(
                  "doorEntered", (Entity play) -> switchArea(finalNewLevel, player)
          );
          newArea.getEvents().addListener(
                  "cutsceneFinished", (Entity play) -> switchArea(finalNewLevel, player)
          );
          System.out.println("Health before switch: " + player.getComponent(CombatStatsComponent.class).getHealth());
          newArea.createWithPlayer(player);
          oldArea.dispose();
        }
      }
    });
  }

  /**
   * Returns the current game area instance
   *
   * @return the current game area instance.
   */
  public GameArea getGameArea() {
    return gameArea;
  }

  @Override
  public void render(float delta) {
    if (!paused) {
      // Update camera position to follow player
      updateCameraFollow();

      physicsEngine.update();
      ServiceLocator.getEntityService().update();
    }
    renderer.render(lightingEngine);  // new render flow used to render lights in the game screen only.
  }

  /**
   * Updates the camera position to follow the player entity.
   * The camera only moves when the player is near the edge of the screen.
   */
  private void updateCameraFollow() {
    Vector2 currentCamPos = renderer.getCamera().getEntity().getPosition().cpy();

    // Find the player entity
    Vector2 playerPosition = null;
    Array<Entity> entities = ServiceLocator.getEntityService().get_entities();
    for (Entity entity : entities) {
      if (entity.getComponent(PlayerActions.class) != null) {
        playerPosition = entity.getPosition().cpy();
        break;
      }
    }

    if (playerPosition != null) {
      // Get camera viewport dimensions
      float viewW = renderer.getCamera().getCamera().viewportWidth;
      float viewH = renderer.getCamera().getCamera().viewportHeight;

      // Calculate deadzone boundaries (area where camera doesn't move)
      float dzW = viewW * DEADZONE_H_FRAC;
      float dzH = viewH * DEADZONE_V_FRAC;

      float dzLeft   = currentCamPos.x - dzW * 0.5f;
      float dzRight  = currentCamPos.x + dzW * 0.5f;
      float dzBottom = currentCamPos.y - dzH * 0.5f;
      float dzTop    = currentCamPos.y + dzH * 0.5f;

      // Calculate target camera position
      float targetX = currentCamPos.x;
      float targetY = currentCamPos.y;

      // Only move camera if player is outside the deadzone
      if (playerPosition.x < dzLeft) {
        // Player is too far left, move camera left
        targetX -= (dzLeft - playerPosition.x);
      } else if (playerPosition.x > dzRight) {
        // Player is too far right, move camera right
        targetX += (playerPosition.x - dzRight);
      }

      if (playerPosition.y < dzBottom) {
        // Player is too far down, move camera down
        targetY -= (dzBottom - playerPosition.y);
      } else if (playerPosition.y > dzTop) {
        // Player is too far up, move camera up
        targetY += (playerPosition.y - dzTop);
      }

      // Smoothly interpolate camera position for smooth movement
      float newCamX = currentCamPos.x + (targetX - currentCamPos.x) * CAMERA_LERP;
      float newCamY = currentCamPos.y + (targetY - currentCamPos.y) * CAMERA_LERP;

      // Update camera position
      renderer.getCamera().getEntity().setPosition(new Vector2(newCamX, newCamY));
    }
  }


  @Override
  public void resize(int width, int height) {
    renderer.resize(width, height);
    logger.trace("Resized renderer: ({} x {})", width, height);
  }

  @Override
  public void pause() {
    logger.info("Game paused");
  }

  @Override
  public void resume() {
    logger.info("Game resumed");
  }

  @Override
  public void dispose() {
    logger.debug("Disposing main game screen");

    renderer.dispose();
    lightingEngine.dispose();
    unloadAssets();

    ServiceLocator.getEntityService().dispose();
    ServiceLocator.getRenderService().dispose();
    ServiceLocator.getResourceService().dispose();
    ServiceLocator.getVfxService().dispose();

    ServiceLocator.clear();
  }

  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(mainGameTextures);
    ServiceLocator.getResourceService().loadAll();
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(mainGameTextures);
  }

  public boolean isPaused() {
      return paused;
  }

  public void togglePaused() {
      paused = !paused;
  }

  public void togglePauseMenu(Tab tab) {
      pauseMenuDisplay.setTab(tab);
      pauseMenuDisplay.setVisible(paused);
  }

  /**
   * Creates the main game's ui including components for rendering ui elements to the screen and
   * capturing and handling ui input.
   */
  private void createUI() {
    logger.debug("Creating ui");
    if (gameArea.getPlayer() == null) {
      throw new IllegalStateException("GameArea has a null player");
    }
    pauseMenuDisplay = new PauseMenuDisplay(this, gameArea.getPlayer(), this.game);
    pauseInput = new PauseInputComponent(this);
    Stage stage = ServiceLocator.getRenderService().getStage();

    Entity ui = new Entity();
    ui.addComponent(new InputDecorator(stage, 10))
        .addComponent(new PerformanceDisplay())
        .addComponent(new MainGameActions(this.game))
        .addComponent(new MainGameExitDisplay())
        .addComponent(pauseMenuDisplay)
        .addComponent(pauseInput);

    ServiceLocator.getEntityService().register(ui);
  }

  // Set last keycode for inventory when tab is clicked
  public void reflectPauseTabClick(PauseMenuDisplay.Tab tab) {
    if (pauseInput != null) {
      pauseInput.setLastKeycodeForTab(tab);
    }
  }
}
