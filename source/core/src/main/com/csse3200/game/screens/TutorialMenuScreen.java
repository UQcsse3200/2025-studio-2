package com.csse3200.game.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.tutorialmenu.TutorialMenuDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The screen displaying tutorial information and controls.
 */
public class TutorialMenuScreen extends ScreenAdapter {
  private static final Logger logger = LoggerFactory.getLogger(TutorialMenuScreen.class);
  private final String[] tutorialMenuTextures = {
      "images/superintelligence_menu_background.png"
      // Add your tutorial images here when you create them:
      // "images/tutorial/movement.png",
      // "images/tutorial/key_item.png",
      // "images/tutorial/dash_mechanic.png",
  };
  
  private final String[] tutorialMenuAtlases = {
      "images/PLAYER.atlas"
  };

  private final GdxGame game;
  private final Renderer renderer;

  public TutorialMenuScreen(GdxGame game) {
    this.game = game;

    logger.debug("Initialising tutorial menu screen services");
    ServiceLocator.registerInputService(new InputService());
    ServiceLocator.registerResourceService(new ResourceService());
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(new RenderService());
    ServiceLocator.registerTimeSource(new GameTime());

    renderer = RenderFactory.createRenderer();
    renderer.getCamera().getEntity().setPosition(5f, 5f);

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
  }

  @Override
  public void dispose() {
    renderer.dispose();
    ServiceLocator.getRenderService().dispose();
    ServiceLocator.getEntityService().dispose();
    unloadAssets();
    ServiceLocator.clear();
  }

  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(tutorialMenuTextures);
    resourceService.loadTextureAtlases(tutorialMenuAtlases);
    ServiceLocator.getResourceService().loadAll();
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(tutorialMenuTextures);
    resourceService.unloadAssets(tutorialMenuAtlases);
  }

  /**
   * Creates the tutorial menu's ui including components for rendering ui elements to the screen
   * and capturing and handling ui input.
   */
  private void createUI() {
    logger.debug("Creating ui");
    Stage stage = ServiceLocator.getRenderService().getStage();
    Entity ui = new Entity();
    ui.addComponent(new TutorialMenuDisplay(game))
        .addComponent(new InputDecorator(stage, 10));

    ServiceLocator.getEntityService().register(ui);
  }
}
