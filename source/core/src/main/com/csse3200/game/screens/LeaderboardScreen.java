package com.csse3200.game.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.leaderboardpage.LeaderboardActions;
import com.csse3200.game.components.leaderboardpage.LeaderboardDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.input.InputService;
import com.csse3200.game.input.LeaderboardInputComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The game screen containing the stats page.
 */
public class LeaderboardScreen extends ScreenAdapter {
    private static final Logger logger = LoggerFactory.getLogger(LeaderboardScreen.class);
    private static final String[] STATISTICS_SCREEN_TEXTURES = {
        "images/superintelligence_menu_background.png"
    };

    private final String[] leaderboardMenuSounds = {
            "sounds/buttonsound.mp3"
    };

    private final GdxGame game;
    private final Renderer renderer;

    public LeaderboardScreen(GdxGame game) {
        this.game = game;

        logger.debug("Initialising leaderboard screen");
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerRenderService(new RenderService());
        renderer = RenderFactory.createRenderer();

        loadAssets();
        createUI();
    }

    @Override
    public void render(float delta) {
        ServiceLocator.getEntityService().update();
        renderer.render();
    }

    @Override
    public void resize(int width, int height) {
        renderer.resize(width, height);
        logger.trace("Resized renderer: ({} x {})", width, height);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing statistics screen");

        renderer.dispose();
        unloadAssets();
        ServiceLocator.getRenderService().dispose();
        ServiceLocator.getEntityService().dispose();

        ServiceLocator.clear();
    }

    private void loadAssets() {
        logger.debug("Loading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.loadTextures(STATISTICS_SCREEN_TEXTURES);
        resourceService.loadSounds(leaderboardMenuSounds);
        ServiceLocator.getResourceService().loadAll();
    }

    private void unloadAssets() {
        logger.debug("Unloading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.unloadAssets(STATISTICS_SCREEN_TEXTURES);
        resourceService.unloadAssets(leaderboardMenuSounds);
    }

    /**
     * Creates the statistic page's ui including components for rendering ui
     * elements to the screen and
     * capturing and handling ui input.
     */
    private void createUI() {
        logger.debug("Creating ui");
        Stage stage = ServiceLocator.getRenderService().getStage();
        Entity ui = new Entity();
        ui.addComponent(new LeaderboardDisplay(game))
                .addComponent(new InputDecorator(stage, 10))
                .addComponent(new LeaderboardInputComponent())
                .addComponent(new LeaderboardActions(game));
        ServiceLocator.getEntityService().register(ui);
    }
}

