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

  /**
   * Table used for storing all UI actors related to health bar
   */
  Table healthTable;

  /**
   * Image icon used in health bar
   */
  private Image heartImage;
  /**
   * Health label
   */
  private Label healthLabel;
  /**
   * The maximum number of hearts to represent max health
   */
  private static final int MAX_HEARTS = 10;

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
    staminaTable.padTop(45f).padLeft(5f);
    staminaTable.setName("stamina");
    staminaTable.setUserObject(entity);

    // Stamina image
    float staminaSideLength = 30f;
    staminaImage = new Image(ServiceLocator.getResourceService().getAsset("images/playerstats/stamina.png", Texture.class));

    // Stamina label
    staminaLabel = new Label("Stamina: ", skin, "large");

    // Stamina bar
    StaminaComponent staminaComp = entity.getComponent(StaminaComponent.class);
    staminaBar = new ProgressBar(-5f, (float) staminaComp.getMaxStamina(), 1f, false, skin);
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
    healthTable.padTop(5f).padLeft(5f);
    healthTable.setName("health");
    healthTable.setUserObject(entity);
    updateHealthTable(entity.getComponent(CombatStatsComponent.class).getHealth());
  }

  /**
   * Update the health table with correct label and number of hearts
   * @param playerHealth The player health, as it currently stands
   */
  private void updateHealthTable(int playerHealth) {
    // Heart image
    float heartSideLength = 30f;
    heartImage = new Image(ServiceLocator.getResourceService().getAsset("images/playerstats/health.png", Texture.class));

    // Player hearts
    int numHearts = playerHealth / MAX_HEARTS;

    // Health text
    healthLabel = new Label("Health: ", skin, "large");

    healthTable.add(heartImage).size(heartSideLength).pad(5);
    healthTable.add(healthLabel);
    for (int i = 0; i < numHearts; i++) {
      heartImage = new Image(ServiceLocator.getResourceService().getAsset("images/playerstats/health.png", Texture.class));
      healthTable.add(heartImage).size(heartSideLength).pad(5);
    }
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
    healthTable.clear();
    updateHealthTable(health);
  }

  /**
   * Updates the player's stamina on the ui.
   * @param stamina the player's current stamina as an integer
   */
  public void updatePlayerStaminaUI(float stamina) {
    staminaBar.setValue(stamina);
  }

  @Override
  public void dispose() {
    staminaTable.clear();
    healthTable.clear();
    super.dispose();
  }
}
