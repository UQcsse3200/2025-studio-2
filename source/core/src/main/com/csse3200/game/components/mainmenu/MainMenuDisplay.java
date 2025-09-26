package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Provider;

/**
 * A ui component for displaying the Main menu.
 */
public class MainMenuDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(MainMenuDisplay.class);
  private static final float Z_INDEX = 2f;
  private Table table;

  @Override
  public void create() {
    super.create();
    addActors();
  }

  private void addActors() {
    Image background =
        new Image(
            ServiceLocator.getResourceService()
                .getAsset("images/superintelligence_menu_background.png", Texture.class));

    background.setFillParent(true);
    stage.addActor(background);

    table = new Table();
    table.setFillParent(true);
    Image title =
        new Image(
            ServiceLocator.getResourceService()
                .getAsset("images/superintelligence_title.png", Texture.class));

    TextButton startBtn = new TextButton("Start", skin, "mainMenu");
    TextButton loadBtn = new TextButton("Load", skin, "mainMenu");
    TextButton settingsBtn = new TextButton("Settings", skin, "mainMenu");
    TextButton exitBtn = new TextButton("Exit", skin, "mainMenu");

    // Triggers an event when the button is pressed
    startBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Start button clicked");
            entity.getEvents().trigger("start");
          }
        });

    loadBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Load button clicked");
            entity.getEvents().trigger("load");
          }
        });

    settingsBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Settings button clicked");
            entity.getEvents().trigger("settings");
          }
        });

    exitBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {

            logger.debug("Exit button clicked");
            entity.getEvents().trigger("exit");
          }
        });

    // Add title
    table.add(title).padBottom(-50f);
    table.row();

    // Add row containing buttons
    Table row = new Table();
    row.add(startBtn).padLeft(15f).padRight(15f);
    row.add(loadBtn).padLeft(15f).padRight(15f);
    row.add(settingsBtn).padLeft(15f).padRight(15f);
    row.add(exitBtn).padLeft(15f).padRight(15f);
    table.add(row);

    stage.addActor(table);
  }

  @Override
  public void draw(SpriteBatch batch) {
    // draw is handled by the stage
  }

  @Override
  public float getZIndex() {
    return Z_INDEX;
  }

  @Override
  public void dispose() {
    table.clear();
    super.dispose();
  }
}
