package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.StaminaComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.utils.CollectablesSave;

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
   * Collectable Label
   */
  private Label collectableLabel;
  /**
   * count of number of collectable items collected
   */
  private int count = CollectablesSave.getCollectedCount();

    /**
     * Creates reusable ui styles and adds actors to the stage.
     */
    @Override
    public void create() {
        super.create();
        addActors();

    entity.getEvents().addListener("updateHealth", this::updatePlayerHealthUI);
    entity.getEvents().addListener("updateStamina", this::updatePlayerStaminaUI);
    entity.getEvents().addListener("updateCollectables", this::updateCollectableUI);
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
    // Create collectable table
    collectableLabel = new Label("Lost Hardware collected: " + count + " / 9", skin, "large");
    collectableLabel.setName("inputsCollected");
    stage.addActor(collectableLabel);
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
     * Update the health table with correct label and number of hearts.
     * Supports full hearts and half hearts.
     */
    private void updateHealthTable(int playerHealth) {
        healthTable.clear();

        float heartSideLength = 30f;

        // Each heart = 10 HP
        int healthPerHeart = 10;
        int fullHearts = playerHealth / healthPerHeart;
        int remainder = playerHealth % healthPerHeart;

        // Label
        healthLabel = new Label("Health: ", skin, "large");
        healthTable.add(healthLabel).pad(5);

        // Add full hearts
        for (int i = 0; i < fullHearts; i++) {
            Image fullHeart = new Image(ServiceLocator.getResourceService()
                    .getAsset("images/playerstats/health.png", Texture.class));
            healthTable.add(fullHeart).size(heartSideLength).pad(5);
        }

        // Add half heart if remainder >= 5
        if (remainder >= 5) {
            Texture heartTexture = ServiceLocator.getResourceService()
                    .getAsset("images/playerstats/health.png", Texture.class);

            TextureRegion halfHeartRegion = new TextureRegion(
                    heartTexture, 0, 0, heartTexture.getWidth() / 2, heartTexture.getHeight()
            );
            Image halfHeart = new Image(halfHeartRegion);
            healthTable.add(halfHeart).size(heartSideLength / 2, heartSideLength).pad(5);
        }
    }



    /** Updates the player's health on the UI. */
    public void updatePlayerHealthUI(int health) {
        updateHealthTable(health);
    }

    /** Updates the player's stamina on the UI. */
    public void updatePlayerStaminaUI(float stamina) {
        staminaBar.setValue(stamina);
    }

  @Override
  public void draw(SpriteBatch batch) {
    // Drawing is handled by the stage
  }

  /**
   * Updates the number of collected items on the UI.
   * @param count the number of items collected
   */
  public void updateCollectableUI(int count) {
    collectableLabel.setText("Lost hardware collected: " + count + " / 9");
  }

  @Override
  public void dispose() {
    super.dispose();
    if (healthTable != null) healthTable.remove();
    if (staminaTable != null) staminaTable.remove();
    if (collectableLabel != null) collectableLabel.remove();
  }
  public void setVisible(boolean visible) {
      if (healthTable != null) healthTable.setVisible(visible);
      if (staminaTable != null) staminaTable.setVisible(visible);
  }
}
