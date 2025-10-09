package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.crashinvaders.vfx.VfxManager;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.*;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.StaminaComponent;
import com.csse3200.game.components.computerterminal.SimpleCaptchaBank;
import com.csse3200.game.components.computerterminal.SpritesheetSpec;
import com.csse3200.game.components.computerterminal.TerminalUiComponent;
import com.csse3200.game.components.deathscreen.DeathScreenDisplay;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.gamearea.PerformanceDisplay;
import com.csse3200.game.components.maingame.MainGameActions;
import com.csse3200.game.components.minimap.MinimapDisplay;
import com.csse3200.game.components.pausemenu.PauseMenuDisplay;
import com.csse3200.game.components.pausemenu.PauseMenuDisplay.Tab;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.LeaderboardEntryDisplay;
import com.csse3200.game.components.player.PlayerStatsDisplay;
import com.csse3200.game.components.statisticspage.StatsTracker;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.input.InputService;
import com.csse3200.game.input.PauseInputComponent;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.lighting.SecurityCamRetrievalService;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.*;
import com.csse3200.game.ui.cutscene.CutsceneArea;
import com.csse3200.game.components.LeaderboardComponent;
import com.csse3200.game.ui.terminal.TerminalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * The game screen containing the main game.
 *
 * <p>Details on libGDX screens: https://happycoding.io/tutorials/libgdx/game-screens
 */
public class MainGameScreen extends ScreenAdapter {
  private static final Logger logger = LoggerFactory.getLogger(MainGameScreen.class);
  private static final String[] mainGameTextures = {"images/playerstats/health.png", "images/playerstats/stamina.png"};
  private static final String[] TERMINAL_TEXTURES = {
          "images/terminal_bg.png",
          "images/terminal_bg_blue.png",
          // add all your spritesheet puzzles here:
          "images/puzzles/waldo_4x4.png",
          "images/puzzles/whichTutor_1x2.png"
  };
  private static final Vector2 CAMERA_POSITION = new Vector2(7.5f, 7.5f);
  // Camera follow parameters
  private static final float DEADZONE_H_FRAC = 0.40f; // Horizontal deadzone fraction (40% of screen width)
  private static final float DEADZONE_V_FRAC = 0.35f; // Vertical deadzone fraction (35% of screen height)
  private static final float CAMERA_LERP_X = 0.0795f; // Camera smoothing factor, lower = smoother
  private static final float CAMERA_LERP_Y = 0.0573f; // Camera smoothing factor, lower = smoother
  private static final float MIN_CAMERA_FOLLOW_Y = 1f;
  private float laserTimer = 0f;
  private float jumpCount=0;
  private static long lvlStartTime;

  private final GdxGame game;
  private final Renderer renderer;
  private final PhysicsEngine physicsEngine;
  private final LightingEngine lightingEngine;
  private final TerrainFactory terrainFactory;
  private boolean paused = false;
  private PauseMenuDisplay pauseMenuDisplay;
  private DeathScreenDisplay deathScreenDisplay;

  private Areas gameAreaEnum;
  private GameArea gameArea;

  private PauseInputComponent pauseInput;
  private LeaderboardComponent leaderboardComponent;
  private GameTime gameTime;
    private MinimapDisplay minimapDisplay;
    private PlayerStatsDisplay playerStatsDisplay;
    private GameAreaDisplay levelTagDisplay;

  public enum Areas {
    LEVEL_ONE,
    LEVEL_TWO,
    LEVEL_THREE,
    SPRINT_ONE,
    TEMPLATE,
    FOREST,
    CAVE,
    CUTSCENE_ONE,
    CUTSCENE_TWO,
    TUTORIAL,
    BOSS_LEVEL
  }

  public MainGameScreen(GdxGame game) {
    this(game, null);
  }

  public MainGameScreen(GdxGame game, Areas area) {
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
    TerminalService.register();
    ServiceLocator.registerVfxService(new VfxManager(Pixmap.Format.RGBA8888));

    // Register service for managing codex entries
    ServiceLocator.registerCodexService(new CodexService());

    ServiceLocator.registerComputerTerminalService(new ComputerTerminalService());

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

    gameTime = new GameTime();

    logger.debug("Initialising main game screen entities");
    terrainFactory = new TerrainFactory(renderer.getCamera());

    gameAreaEnum = area;
    gameArea = getGameArea(area);
    gameArea.create();

    // As some levels progress to the next level via doors and some via cutscenes ending, add both
    gameArea.getEvents().addListener("doorEntered", (Entity player) -> {
      if (gameArea instanceof TutorialGameArea) {
        // Go back to tutorial menu instead of next level
        logger.info("Tutorial completed, returning to tutorial menu");
        game.setScreen(new TutorialMenuScreen(game));
      } else {
        // Normal level progression
        logger.info("Door entered, proceeding to next level");
        switchArea(getNextArea(area), player);
      }
    });
    gameArea.getEvents().addListener("cutsceneFinished", (Entity play) -> {
      switchArea(getNextArea(area), play);
    });

    gameArea.getEvents().addListener("reset", this::onGameAreaReset);
    gameArea.getPlayer().getEvents().addListener("playerDied", this::showDeathScreen);

    // Have to createUI after the game area .create() since createUI requires the player to exist,
    // which is only done upon game area creation
    createUI();
  }

  public Areas getAreaEnum() {
    return gameAreaEnum;
  }
  /**
   * Centralized door-entered flow: gather stats, show leaderboard entry, hide HUD,
   * then resume HUD and switch to next area.
   */
  private void handleDoorEntered() {
      Areas next = getNextArea(gameAreaEnum);
      Entity playerEntity = gameArea.getPlayer();

      // Defensive guards: never trust event parameter, only gameArea.getPlayer()
      CombatStatsComponent combat = playerEntity.getComponent(CombatStatsComponent.class);
      int health = (combat != null) ? combat.getHealth() : 0;

      StaminaComponent staminaComp = playerEntity.getComponent(StaminaComponent.class);
      float stamina = (staminaComp != null) ? staminaComp.getCurrentStamina() : 0f;

      long completionTime = gameTime.getTimeSince(lvlStartTime);

      hideHUD();
      paused = true;

      LeaderboardEntryDisplay entryDisplay = new LeaderboardEntryDisplay(completionTime, health, stamina);
      Entity uiEntity = new Entity().addComponent(entryDisplay);
      ServiceLocator.getEntityService().register(uiEntity);

      uiEntity.getEvents().addListener("leaderboardEntryComplete", () -> {
          String name = entryDisplay.getEnteredName();
          if (name != null && !name.isEmpty() && leaderboardComponent != null) {
              leaderboardComponent.updateLeaderboard(name, completionTime);
          }
          showHUD();
          paused = false;
          switchArea(next, playerEntity);
      });
  }
  /**
   * Get the GameArea mapped to the Areas area.
   * @param area - Areas area.
   * @return GameArea mapped.
   */
  public GameArea getGameArea(Areas area) {
    lvlStartTime = gameTime.getTime();
    return switch (area) {
      case TUTORIAL ->  new TutorialGameArea(terrainFactory);
      case LEVEL_ONE -> new LevelOneGameArea(terrainFactory);
      case CUTSCENE_ONE -> new CutsceneArea("cutscene-scripts/cutscene1.txt");
      case LEVEL_TWO -> new LevelTwoGameArea(terrainFactory);
      case CUTSCENE_TWO -> new CutsceneArea("cutscene-scripts/cutscene2.txt");
      case SPRINT_ONE -> new SprintOneGameArea(terrainFactory);
      case BOSS_LEVEL ->  new BossLevelGameArea(terrainFactory);
      case LEVEL_THREE -> new LevelThreeGameArea(terrainFactory);
      default -> throw new IllegalStateException("Unexpected value: " + area);
    };
  }

  /**
   * Get the Areas area that follows the current Areas game area.
   * @param area - Current Areas game area.
   * @return next Areas game area.
   */
  private Areas getNextArea(Areas area) {
    return switch (area) {
      case LEVEL_ONE -> Areas.CUTSCENE_ONE;
      case CUTSCENE_ONE, SPRINT_ONE -> Areas.LEVEL_TWO;
      case LEVEL_TWO -> Areas.CUTSCENE_TWO;
      case CUTSCENE_TWO -> Areas.LEVEL_THREE;
      case LEVEL_THREE -> Areas.BOSS_LEVEL;
      case BOSS_LEVEL -> Areas.SPRINT_ONE;
      default -> throw new IllegalStateException("Unexpected value: " + area);
    };
  }

  private void switchArea(Areas area, Entity player) {
    final Runnable runnable = () -> this.switchAreaRunnable(area, player);
    if (gameArea instanceof CutsceneArea) {
      Gdx.app.postRunnable(runnable);
    } else {
      player.getEvents().trigger("startTransition", 1.5f, runnable);
    }
  }

  private void switchAreaRunnable(Areas area, Entity player) {
    if (area == null) return;

    GameArea oldArea = gameArea;
    oldArea.dispose();
    oldArea = null; // Garbage collector?

    System.out.println("Area switched to " + area);
    //TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());

    GameArea newArea = getGameArea(area);
    Areas newLevel = getNextArea(area);

    if (newArea != null) {
      System.out.println("TIME" + lvlStartTime);
      //leaderboardComponent.updateLeaderboard(gameAreaEnum.toString(), gameTime.getTimeSince(lvlStartTime));
      if (newArea instanceof CutsceneArea) {
        StatsTracker.completeLevel();
      }

      gameArea = newArea;
      gameAreaEnum = area;

      gameArea.getEvents().addListener("doorEntered", (Entity play) -> {
        switchArea(newLevel, play);
      });
      gameArea.getEvents().addListener("cutsceneFinished", (Entity play) -> switchArea(newLevel, play));

      InventoryComponent inv = player.getComponent(InventoryComponent.class);
      if (inv != null) {
          inv.resetBag(InventoryComponent.Bag.OBJECTIVES);
      }

//        System.out.println("Health before switch: " + player.getComponent(CombatStatsComponent.class).getHealth());
      gameArea.createWithPlayer(player);

      gameArea.getEvents().addListener("reset", this::onGameAreaReset);
      gameArea.getPlayer().getEvents().addListener("playerDied", this::showDeathScreen);
    }
  }

  /**
   * Builds the small set of CAPTCHA specs used by the terminal
   *
   * Indexing is 0-based, row-major:
   * top-left = 0, then 1, 2, ... across the row, next row continues
   */
  private SimpleCaptchaBank buildCaptchaBank() {
    SimpleCaptchaBank bank = new SimpleCaptchaBank();

    // 4x4 Waldo puzzle.
    bank.add(new SpritesheetSpec(
            "images/puzzles/waldo_4x4.png",
            4, 4,
            Set.of(3),
            "Wheres Waldo? Select all tiles that contain him."
    ));

    // 1x2 “which tutor” puzzle.
    // 0 = left tile, 1 = right tile
    bank.add(new SpritesheetSpec(
            "images/puzzles/whichTutor_1x2.png",
            1, 2,
            Set.of(0),
            "Which tutor is way better in every regard (not ragebait)"
    ));
    return bank;
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
          if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
              jumpCount++;
              if (gameArea instanceof LevelOneGameArea levelOneArea && jumpCount == 30) {
                  levelOneArea.laserShowerChecker(delta);
                  jumpCount = 0;
              }else if (gameArea instanceof LevelTwoGameArea levelTwoArea&& jumpCount == 20) {
                  levelTwoArea.laserShowerChecker(delta);
                  jumpCount = 0;
              }
          }
          laserTimer += delta;

          // Check if 50 seconds have passed
          if (laserTimer >= 50f) {
              if (gameArea instanceof BossLevelGameArea bossLevel) {
                  bossLevel.spawnLaserShower(); // spawn lasers
              }
              laserTimer = 0f; // reset timer
          }

      }
      renderer.render(lightingEngine);  // new render flow used to render lights in the game screen only.
  }

  /**
   * Updates the camera position to follow the player entity.
   * The camera only moves when the player is near the edge of the screen.
   */
  private void updateCameraFollow() {
    Entity player = gameArea.getPlayer();
    if (player == null) return;

    final Camera camera = renderer.getCamera().getCamera();
    final Vector2 playerPosition = player.getPosition();

    // Get camera viewport dimensions
    float viewW = camera.viewportWidth;
    float viewH = camera.viewportHeight;

    // Calculate deadzone boundaries (area where camera doesn't move)
    float dzW = viewW * DEADZONE_H_FRAC;
    float dzH = viewH * DEADZONE_V_FRAC;

    float dzLeft   = camera.position.x - dzW * 0.1f;
    float dzRight  = camera.position.x + dzW * 0.1f;
    float dzBottom = camera.position.y - dzH * 0.20f;
    float dzTop    = camera.position.y + dzH * 0.30f;

    // Calculate target camera position
    float targetX = camera.position.x;
    float targetY = camera.position.y;

    // Only move camera if player is outside the deadzone
    if (playerPosition.x < dzLeft) {
      // Player is too far left, move camera left
      targetX -= (dzLeft - playerPosition.x);
    } else if (playerPosition.x > dzRight) {
      // Player is too far right, move camera right
      targetX += (playerPosition.x - dzRight);
    }

    // Don't move camera down if player is below the minium height camera following height
    if (playerPosition.y >= MIN_CAMERA_FOLLOW_Y) {
      if (playerPosition.y < dzBottom) {
        // Player is too far down, move camera down
        targetY -= (dzBottom - playerPosition.y);
      } else if (playerPosition.y > dzTop) {
        // Player is too far up, move camera up
        targetY += (playerPosition.y - dzTop);
      }
    }

    // Smoothly interpolate camera position for smooth movement
    camera.position.x += (targetX - camera.position.x) * CAMERA_LERP_X;
    camera.position.y += (targetY - camera.position.y) * CAMERA_LERP_Y;
    camera.update();
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

    ServiceLocator.getEntityService().dispose();
    lightingEngine.dispose();
    renderer.dispose();
    unloadAssets();
    ServiceLocator.getRenderService().dispose();
    ServiceLocator.getResourceService().dispose();
    ServiceLocator.getVfxService().dispose();

    ServiceLocator.clear();
  }

  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(mainGameTextures);
    resourceService.loadTextures(TERMINAL_TEXTURES);
    ServiceLocator.getResourceService().loadAll();
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(mainGameTextures);
    resourceService.unloadAssets(TERMINAL_TEXTURES);
  }

  public boolean isPaused() {
    return paused;
  }

  public void togglePaused() {
    paused = !paused;
  }

  public void togglePauseMenu(Tab tab) {
    pauseMenuDisplay.setVisible(paused);
    if (paused) pauseMenuDisplay.setTab(tab);
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

    pauseMenuDisplay = new PauseMenuDisplay(this, this.game);
    deathScreenDisplay = new DeathScreenDisplay(this, this.game);
    pauseInput = new PauseInputComponent(this);

    Stage stage = ServiceLocator.getRenderService().getStage();
    leaderboardComponent = new LeaderboardComponent();

    lvlStartTime = gameTime.getTime();

    // Build your puzzle bank (spritesheet-driven)
    SimpleCaptchaBank bank = buildCaptchaBank();

    Entity ui = new Entity();
    ui.addComponent(new InputDecorator(stage, 10))
            .addComponent(new PerformanceDisplay())
            .addComponent(new MainGameActions(this.game))
            .addComponent(pauseMenuDisplay)
            .addComponent(deathScreenDisplay)
            .addComponent(pauseInput)
            .addComponent(new TerminalUiComponent(this).setCaptchaBank(bank));

    ServiceLocator.getEntityService().register(ui);
    ServiceLocator.getComputerTerminalService().registerUiEntity(ui);
  }
  private void hideHUD() {
        if (minimapDisplay != null) minimapDisplay.setVisible(false);
        if (playerStatsDisplay != null) playerStatsDisplay.setVisible(false);
        if (levelTagDisplay != null) levelTagDisplay.setVisible(false);
    }

    private void showHUD() {
        if (minimapDisplay != null) minimapDisplay.setVisible(true);
        if (playerStatsDisplay != null) playerStatsDisplay.setVisible(true);
        if (levelTagDisplay != null) levelTagDisplay.setVisible(true);
    }

  /**
   * Shows the death screen overlay
   */
  private void showDeathScreen() {
    if (gameArea != null && gameArea.getPlayer() != null) {
      gameArea.recordDeathLocation(gameArea.getPlayer().getPosition());
    }

    deathScreenDisplay.setVisible(true);
  }

  /**
   * Reset game area and re-add player's death listener
   */
  public void reset() {
    gameArea.reset();
  }

  public void onGameAreaReset(Entity player) {
    player.getEvents().addListener("playerDied", this::showDeathScreen);
  }

  // Set last keycode for inventory when tab is clicked
  public void reflectPauseTabClick(PauseMenuDisplay.Tab tab) {
    if (pauseInput != null) {
      pauseInput.setLastKeycodeForTab(tab);
    }
  }
}
