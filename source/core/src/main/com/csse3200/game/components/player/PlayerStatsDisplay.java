package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.StaminaComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * A ui component for displaying player stats, e.g. health.
 */
public class PlayerStatsDisplay extends UIComponent {
  Table healthTable;
  private Image heartImage;
  private Label healthLabel;

  /**
   * Table used for storing all UI actors related to stamina bar
   */
  private Table staminaTable;
  /**
   * Stamina label
   */
  private Label staminaLabel;
  /**
   * Progress bar used to visually show stamina
   */
  private ProgressBar staminaBar;
  /**
   * Image icon used in stamina bar
   */
  private Image staminaImage;

  /**
   * Creates reusable ui styles and adds actors to the stage.
   */
  @Override
  public void create() {
    super.create();
    addActors();

    entity.getEvents().addListener("updateHealth", this::updatePlayerHealthUI);
    entity.getEvents().addListener("updateStamina", this::updatePlayerStaminaUI);
  }

  /**
   * Creates actors and positions them on the stage using a table.
   * @see Table for positioning options
   */
  private void addActors() {
    // Create health table
    createHealthTable();
    stage.addActor(healthTable);
    // Create stamina table
    createStaminaTable();
    stage.addActor(staminaTable);
  }

  /**
   * Helper method that creates and sets up the initial stamina table
   */
  private void createStaminaTable() {
    // Create stamina table
    staminaTable = new Table();
    staminaTable.top().left();
    staminaTable.setFillParent(true);
    staminaTable.padTop(90f).padLeft(5f);
    staminaTable.setName("stamina");
    staminaTable.setUserObject(entity);

    // Stamina image
    float staminaSideLength = 30f;
    staminaImage = new Image(ServiceLocator.getResourceService().getAsset("images/playerstats/stamina.png", Texture.class));

    // Stamina label
    staminaLabel = new Label("Stamina: ", skin, "large");

    // Stamina bar
    StaminaComponent staminaComp = entity.getComponent(StaminaComponent.class);
    staminaBar = new ProgressBar(0f, (float) staminaComp.getMaxStamina(), 1f, false, skin);
    staminaBar.setValue((float) staminaComp.getCurrentStamina());

    // Add actors to stamina table
    staminaTable.add(staminaImage).size(staminaSideLength).pad(5);
    staminaTable.add(staminaLabel);
    staminaTable.add(staminaBar);
  }

  /**
   * Helper method that creates and sets up the initial health table
   */
  private void createHealthTable() {
    // Create health table
    healthTable = new Table();
    healthTable.top().left();
    healthTable.setFillParent(true);
    healthTable.padTop(45f).padLeft(5f);
    healthTable.setName("health");
    healthTable.setUserObject(entity);

    // Heart image
    float heartSideLength = 30f;
    heartImage = new Image(ServiceLocator.getResourceService().getAsset("images/playerstats/health.png", Texture.class));

    // Health text
    int health = entity.getComponent(CombatStatsComponent.class).getHealth();
    CharSequence healthText = String.format("Health: %d", health);
    healthLabel = new Label(healthText, skin, "large");

    healthTable.add(heartImage).size(heartSideLength).pad(5);
    healthTable.add(healthLabel);
  }

  @Override
  public void draw(SpriteBatch batch)  {
    // draw is handled by the stage
  }

  /**
   * Updates the player's health on the ui.
   * @param health player health
   */
  public void updatePlayerHealthUI(int health) {
    CharSequence text = String.format("Health: %d", health);
    healthLabel.setText(text);
  }

  /**
   * Updates the player's stamina on the ui.
   * @param stamina the player's current stamina as an integer
   */
  public void updatePlayerStaminaUI(int stamina) {

  }

  @Override
  public void dispose() {
    super.dispose();
    heartImage.remove();
    healthLabel.remove();
    staminaLabel.remove();
    staminaBar.remove();
    staminaImage.remove();
  }
}
