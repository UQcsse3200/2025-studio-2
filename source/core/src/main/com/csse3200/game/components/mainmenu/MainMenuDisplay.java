package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.HoverEffectHelper;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * A ui component for displaying the Main menu.
 */
public class MainMenuDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(MainMenuDisplay.class);
  private static final float Z_INDEX = 2f;
  private Table table;
  private Sound buttonClickSound;

  @Override
  public void create() {
    super.create();
    buttonClickSound = ServiceLocator.getResourceService()
            .getAsset("sounds/buttonsound.mp3", Sound.class);
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
    TextButton tutorialBtn = new TextButton("Tutorial", skin, "mainMenu");
    TextButton leaderboardBtn = new TextButton("Leaderboard", skin, "mainMenu");
    TextButton settingsBtn = new TextButton("Settings", skin, "mainMenu");
    TextButton statsBtn = new TextButton("Stats", skin, "mainMenu");
    TextButton exitBtn = new TextButton("Exit", skin, "mainMenu");

    startBtn.setTransform(true);
    startBtn.setOrigin(Align.center);

    loadBtn.setTransform(true);
    loadBtn.setOrigin(Align.center);

    tutorialBtn.setTransform(true);
    tutorialBtn.setOrigin(Align.center);

    leaderboardBtn.setTransform(true);
    leaderboardBtn.setOrigin(Align.center);

    settingsBtn.setTransform(true);
    settingsBtn.setOrigin(Align.center);

    statsBtn.setTransform(true);
    statsBtn.setOrigin(Align.center);

    exitBtn.setTransform(true);
    exitBtn.setOrigin(Align.center);


    HoverEffectHelper.applyHoverEffects(Arrays.asList(startBtn, loadBtn, tutorialBtn, leaderboardBtn, settingsBtn, statsBtn, exitBtn));
    // Triggers an event when the button is pressed
    startBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Start button clicked");
            buttonClickSound.play(UserSettings.get().masterVolume);
            entity.getEvents().trigger("start");
          }
        });

    loadBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Load button clicked");
            buttonClickSound.play(UserSettings.get().masterVolume);
            entity.getEvents().trigger("load");
          }
        });

    tutorialBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Tutorial button clicked");
            buttonClickSound.play(UserSettings.get().masterVolume);
            entity.getEvents().trigger("tutorial");
          }
        });

    leaderboardBtn.addListener(
        new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                logger.debug("Leaderboard button clicked");
                buttonClickSound.play(UserSettings.get().masterVolume);
                entity.getEvents().trigger("leaderboard");
            }
        });

    settingsBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Settings button clicked");
            buttonClickSound.play(UserSettings.get().masterVolume);
            entity.getEvents().trigger("settings");
          }
        });

    statsBtn.addListener(
      new ChangeListener() {
        @Override
        public void changed(ChangeEvent changeEvent, Actor actor) {
          logger.debug("Stats button clicked");
          buttonClickSound.play(UserSettings.get().masterVolume);
          entity.getEvents().trigger("stats");
          }
      });

    exitBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Exit button clicked");
            buttonClickSound.play(UserSettings.get().masterVolume);
            entity.getEvents().trigger("exit");
          }
        });

    // Add title
    table.add(title).padBottom(-50f);
    table.row();

    // Add row containing buttons
    Table row = new Table();
    row.setTransform(true);
    row.add(startBtn).padLeft(15f).padRight(15f);
    row.add(loadBtn).padLeft(15f).padRight(15f);
    row.add(tutorialBtn).padLeft(15f).padRight(15f);
    row.add(leaderboardBtn).padLeft(15f).padRight(15f);
    row.add(settingsBtn).padLeft(15f).padRight(15f);
    row.add(statsBtn).padLeft(15f).padRight(15f);
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
