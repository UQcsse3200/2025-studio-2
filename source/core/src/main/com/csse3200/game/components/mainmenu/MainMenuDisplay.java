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
 * The ui component for displaying the Main menu.
 */
public class MainMenuDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(MainMenuDisplay.class);
  private static final float Z_INDEX = 2.0f;
  private Table table;
  private Sound buttonClickSound;

  @Override
  public void create() {
    super.create();
    buttonClickSound = ServiceLocator.getResourceService().getAsset("sounds/buttonsound.mp3", Sound.class);
    addActors();
  }

  private TextButton createButton(String name, Table row) {
    TextButton button = new TextButton(name, skin, "mainMenu");
    button.setTransform(true);
    button.setOrigin(Align.center);
    button.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent changeEvent, Actor actor) {
        logger.debug("{} button clicked", name);
        buttonClickSound.play(UserSettings.get().masterVolume);
        entity.getEvents().trigger(name.toLowerCase());
      }
    });

    row.add(button).padLeft(16).padRight(16);
    return button;
  }

  private void addActors() {
    Image background = new Image(ServiceLocator.getResourceService()
        .getAsset("images/superintelligence_menu_background.png", Texture.class));

    background.setFillParent(true);
    stage.addActor(background);

    table = new Table();
    table.setFillParent(true);
    Image title = new Image(ServiceLocator.getResourceService()
        .getAsset("images/superintelligence_title.png", Texture.class));

    Table row = new Table();
    row.setTransform(true);

    // Add buttons here
    HoverEffectHelper.applyHoverEffects(Arrays.asList(
        createButton("Start", row),
        createButton("Load", row),
        createButton("Tutorial", row),
        createButton("Leaderboard", row),
        createButton("Settings", row),
        createButton("Stats", row),
        createButton("Exit", row)
    ));

    table.add(title).padBottom(-50.0f);
    table.row();
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
