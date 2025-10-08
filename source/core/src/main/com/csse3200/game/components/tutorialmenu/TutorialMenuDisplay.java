package com.csse3200.game.components.tutorialmenu;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.GdxGame;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A UI component for displaying the tutorial menu with game controls and information.
 */
public class TutorialMenuDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(TutorialMenuDisplay.class);
  private final GdxGame game;

  public TutorialMenuDisplay(GdxGame game) {
    this.game = game;
  }

  @Override
  public void create() {
    super.create();
    addActors();
  }

  private void addActors() {
    // Background image
    Image background = new Image(
        ServiceLocator.getResourceService()
            .getAsset("images/superintelligence_menu_background.png", Texture.class));
    background.setFillParent(true);
    stage.addActor(background);

    // Main table for layout
    Table table = new Table();
    table.setFillParent(true);
    
    // Back button to return to main menu
    TextButton backBtn = new TextButton("Back", skin);
    backBtn.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent changeEvent, Actor actor) {
        logger.debug("Back button clicked");
        game.setScreen(GdxGame.ScreenType.MAIN_MENU);
      }
    });

    table.add(backBtn).center();
    
    stage.addActor(table);
  }

  @Override
  protected void draw(SpriteBatch batch) {
  }

  @Override
  public void dispose() {
    super.dispose();
  }
}
