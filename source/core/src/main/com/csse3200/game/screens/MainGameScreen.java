package com.csse3200.game.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.CaveGameArea;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas.SprintOneGameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.maingame.MainGameActions;
<<<<<<< HEAD
import com.csse3200.game.components.player.PlayerActions;
=======
import com.csse3200.game.components.pausemenu.PauseMenuDisplay;
import com.csse3200.game.components.pausemenu.PauseMenuDisplay.Tab;
>>>>>>> main
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.input.InputService;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The game screen containing the main game.
 *
 * <p>Details on libGDX screens: https://happycoding.io/tutorials/libgdx/game-screens
 */
public class MainGameScreen extends ScreenAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MainGameScreen.class);
    private static final String[] mainGameTextures = {"images/heart.png"};
    private static final Vector2 CAMERA_POSITION = new Vector2(7.5f, 7.5f);
    private static final float DEADZONE_H_FRAC = 0.40f;
    private static final float DEADZONE_V_FRAC = 0.35f;
    private static final float CAMERA_LERP = 0.15f;

    private final GdxGame game;
    private final Renderer renderer;
    private final PhysicsEngine physicsEngine;
    private final LightingEngine lightingEngine;

<<<<<<< HEAD
  private boolean paused = false;
  private PauseMenuDisplay pauseMenuDisplay;


  public MainGameScreen(GdxGame game) {
    this.game = game;
=======
    public MainGameScreen(GdxGame game) {
        this.game = game;
>>>>>>> 7a71c0d95170f25c5bd8c5a17a60d69d9704a260

        logger.debug("Initialising main game screen services");
        ServiceLocator.registerTimeSource(new GameTime());

        PhysicsService physicsService = new PhysicsService();
        ServiceLocator.registerPhysicsService(physicsService);
        physicsEngine = physicsService.getPhysics();

        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerResourceService(new ResourceService());

        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerRenderService(new RenderService());

        renderer = RenderFactory.createRenderer();

        // 设置初始相机位置
        renderer.getCamera().getEntity().setPosition(CAMERA_POSITION);

        renderer.getDebug().renderPhysicsWorld(physicsEngine.getWorld());

        // Registering the new lighting service with the service manager
        LightingService lightingService = new LightingService(renderer.getCamera(), physicsEngine.getWorld());
        ServiceLocator.registerLightingService(lightingService);
        lightingEngine = lightingService.getEngine();

        loadAssets();
        createUI();

<<<<<<< HEAD
    logger.debug("Initialising main game screen entities");
    TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());
    ForestGameArea forestGameArea = new ForestGameArea(terrainFactory);
    CaveGameArea caveGameArea = new CaveGameArea(terrainFactory);
    SprintOneGameArea sprintOneGameArea = new SprintOneGameArea(terrainFactory);
    sprintOneGameArea.create();
    //caveGameArea.create();
    //forestGameArea.create();
  }

  @Override
  public void render(float delta) {
<<<<<<< HEAD
=======
        logger.debug("Initialising main game screen entities");
        TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());
        ForestGameArea forestGameArea = new ForestGameArea(terrainFactory);
        //CaveGameArea caveGameArea = new CaveGameArea(terrainFactory);
        //caveGameArea.create();
        forestGameArea.create();
    }

    @Override
    public void render(float delta) {
>>>>>>> 7a71c0d95170f25c5bd8c5a17a60d69d9704a260

        Vector2 currentCamPos = renderer.getCamera().getEntity().getPosition().cpy();

<<<<<<< HEAD
    physicsEngine.update();
    ServiceLocator.getEntityService().update();
=======
    if (!paused) {
        physicsEngine.update();
        ServiceLocator.getEntityService().update();
    }
>>>>>>> main
    renderer.render(lightingEngine);  // new render flow used to render lights in the game screen only.
  }
=======
        Vector2 playerPosition = null;
        Array<Entity> entities = ServiceLocator.getEntityService().get_entities();
        for (Entity entity : entities) {
            if (entity.getComponent(PlayerActions.class) != null) {
                playerPosition = entity.getPosition().cpy();
                break;
            }
        }
>>>>>>> 7a71c0d95170f25c5bd8c5a17a60d69d9704a260

        if (playerPosition != null) {
            float viewW = renderer.getCamera().getCamera().viewportWidth;
            float viewH = renderer.getCamera().getCamera().viewportHeight;

            float dzW = viewW * DEADZONE_H_FRAC;
            float dzH = viewH * DEADZONE_V_FRAC;

            float dzLeft   = currentCamPos.x - dzW * 0.5f;
            float dzRight  = currentCamPos.x + dzW * 0.5f;
            float dzBottom = currentCamPos.y - dzH * 0.5f;
            float dzTop    = currentCamPos.y + dzH * 0.5f;

            float targetX = currentCamPos.x;
            float targetY = currentCamPos.y;

            if (playerPosition.x < dzLeft) {
                targetX -= (dzLeft - playerPosition.x);
            } else if (playerPosition.x > dzRight) {
                targetX += (playerPosition.x - dzRight);
            }

            if (playerPosition.y < dzBottom) {
                targetY -= (dzBottom - playerPosition.y);
            } else if (playerPosition.y > dzTop) {
                targetY += (playerPosition.y - dzTop);
            }

            float newCamX = currentCamPos.x + (targetX - currentCamPos.x) * CAMERA_LERP;
            float newCamY = currentCamPos.y + (targetY - currentCamPos.y) * CAMERA_LERP;

            renderer.getCamera().getEntity().setPosition(new Vector2(newCamX, newCamY));
        }

        physicsEngine.update();
        ServiceLocator.getEntityService().update();
        renderer.render(lightingEngine);
    }

<<<<<<< HEAD
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
    pauseMenuDisplay = new PauseMenuDisplay(this);
    Stage stage = ServiceLocator.getRenderService().getStage();

    Entity ui = new Entity();
    ui.addComponent(new InputDecorator(stage, 10))
        .addComponent(new PerformanceDisplay())
        .addComponent(new MainGameActions(this.game))
        .addComponent(new MainGameExitDisplay())
        .addComponent(pauseMenuDisplay)
        .addComponent(new PauseInputComponent(this));
=======
    @Override
    public void resize(int width, int height) {
        renderer.resize(width, height);
        logger.trace("Resized renderer: ({} x {})", width, height);
    }

    @Override
    public void pause() {
        logger.info("Game paused");
    }
>>>>>>> 7a71c0d95170f25c5bd8c5a17a60d69d9704a260

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

    /**
     * Creates the main game's ui including components for rendering ui elements to the screen and
     * capturing and handling ui input.
     */
    private void createUI() {
        logger.debug("Creating ui");
        Stage stage = ServiceLocator.getRenderService().getStage();
        InputComponent inputComponent =
                ServiceLocator.getInputService().getInputFactory().createForTerminal();

        Entity ui = new Entity();
        ui.addComponent(new InputDecorator(stage, 10))
                .addComponent(new PerformanceDisplay())
                .addComponent(new MainGameActions(this.game))
                .addComponent(new MainGameExitDisplay())
                .addComponent(new Terminal())
                .addComponent(inputComponent)
                .addComponent(new TerminalDisplay());

        ServiceLocator.getEntityService().register(ui);
    }
}