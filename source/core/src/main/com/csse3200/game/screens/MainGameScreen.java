package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.crashinvaders.vfx.VfxManager;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.LevelOneGameArea;
import com.csse3200.game.areas.LevelTwoGameArea;
import com.csse3200.game.areas.SprintOneGameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.StaminaComponent;
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
import com.csse3200.game.services.CodexService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.cutscene.CutsceneArea;
import com.csse3200.game.components.LeaderboardComponent;
import com.csse3200.game.ui.terminal.TerminalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The game screen containing the main game.
 *
 * <p>Details on libGDX screens: https://happycoding.io/tutorials/libgdx/game-screens
 */
public class MainGameScreen extends ScreenAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MainGameScreen.class);
    private static final String[] mainGameTextures = {"images/playerstats/health.png", "images/playerstats/stamina.png"};
    private static final Vector2 CAMERA_POSITION = new Vector2(7.5f, 7.5f);
    // Camera follow parameters
    private static final float DEADZONE_H_FRAC = 0.40f; // Horizontal deadzone fraction (40% of screen width)
    private static final float DEADZONE_V_FRAC = 0.35f; // Vertical deadzone fraction (35% of screen height)
    private static final float CAMERA_LERP_X = 0.0795f; // Camera smoothing factor, lower = smoother
    private static final float CAMERA_LERP_Y = 0.0573f; // Camera smoothing factor, lower = smoother
    private static final float MIN_CAMERA_FOLLOW_Y = 1f;
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
        LEVEL_ONE, LEVEL_TWO, SPRINT_ONE, TEMPLATE, FOREST, CAVE, CUTSCENE_ONE, CUTSCENE_TWO
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
            Areas next = getNextArea(gameAreaEnum);

            // Gather stats for leaderboard entry
            int health = player.getComponent(CombatStatsComponent.class).getHealth();
            float stamina = player.getComponent(com.csse3200.game.components.StaminaComponent.class).getCurrentStamina();
            long completionTime = gameTime.getTimeSince(lvlStartTime);

            // Hide HUD
            if (minimapDisplay != null) minimapDisplay.setVisible(false);
            if (playerStatsDisplay != null) playerStatsDisplay.setVisible(false);
            if (levelTagDisplay != null) levelTagDisplay.setVisible(false);

            // Pause gameplay input (Esc still works via PauseInputComponent)
            paused = true;

            // Show leaderboard entry screen
            LeaderboardEntryDisplay entryDisplay = new LeaderboardEntryDisplay(completionTime, health, stamina);
            Entity uiEntity = new Entity().addComponent(entryDisplay);
            ServiceLocator.getEntityService().register(uiEntity);

            // Handle completion of entry screen
            uiEntity.getEvents().addListener("leaderboardEntryComplete", () -> {
                String name = entryDisplay.getEnteredName();
                if (name != null && !name.isEmpty()) {
                    leaderboardComponent.updateLeaderboard(name, completionTime);
                }

                // Restore HUD
                if (minimapDisplay != null) minimapDisplay.setVisible(true);
                if (playerStatsDisplay != null) playerStatsDisplay.setVisible(true);
                if (levelTagDisplay != null) levelTagDisplay.setVisible(true);

                // Restore gameplay input
                paused = false;

                // Continue to cutscene/next level
                switchArea(next, player);
            });
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
     * Get the GameArea mapped to the Areas area.
     * @param area - Areas area.
     * @return GameArea mapped.
     */
    public GameArea getGameArea(Areas area) {
        GameArea newArea = null;

        switch (area) {
            case LEVEL_ONE -> {
                newArea = new LevelOneGameArea(terrainFactory);
            }
            case CUTSCENE_ONE -> {
                newArea = new CutsceneArea("cutscene-scripts/cutscene1.txt");
            }
            case LEVEL_TWO -> {
                lvlStartTime =  gameTime.getTime();
                newArea = new LevelTwoGameArea(terrainFactory);
            }
            case CUTSCENE_TWO -> {
                newArea = new CutsceneArea("cutscene-scripts/cutscene2.txt");
            }
            case SPRINT_ONE -> {
                newArea = new SprintOneGameArea(terrainFactory);
            }
        }

        return newArea;
    }

    /**
     * Get the Areas area that follows the current Areas game area.
     * @param area - Current Areas game area.
     * @return next Areas game area.
     */
    private Areas getNextArea(Areas area) {
        Areas nextArea = null;

        switch (area) {
            case LEVEL_ONE -> {
                nextArea = Areas.CUTSCENE_ONE;
            }
            case CUTSCENE_ONE -> {
                nextArea = Areas.LEVEL_TWO;
            }
            case LEVEL_TWO -> {
                nextArea = Areas.CUTSCENE_TWO;
            }
            case CUTSCENE_TWO -> {
                nextArea = Areas.SPRINT_ONE;
            }
            case SPRINT_ONE -> {
                nextArea = Areas.LEVEL_TWO;
            }
        }

        return nextArea;
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
        // Re-register input service so player controls work in the new area
        createUI();

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

        System.out.println("Starting Level Time!");
        lvlStartTime = gameTime.getTime();

        Entity ui = new Entity();
        ui.addComponent(new InputDecorator(stage, 10))
                .addComponent(new PerformanceDisplay())
                .addComponent(new MainGameActions(this.game))
                .addComponent(pauseMenuDisplay)
                .addComponent(deathScreenDisplay)
                .addComponent(pauseInput);
        // Add HUD components
        minimapDisplay = new MinimapDisplay(200f, new MinimapDisplay.MinimapOptions());
        playerStatsDisplay = new PlayerStatsDisplay();
        levelTagDisplay = new GameAreaDisplay(gameAreaEnum.toString());

        ServiceLocator.getEntityService().register(ui);
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