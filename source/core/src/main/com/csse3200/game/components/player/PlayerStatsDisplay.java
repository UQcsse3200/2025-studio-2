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
 * A ui component for displaying player stats, e.g. health and stamina.
 */
public class PlayerStatsDisplay extends UIComponent {
    private Table healthTable;
    private Table staminaTable;

    private Image heartImage;
    private Label healthLabel;
    private Label staminaLabel;
    private ProgressBar staminaBar;
    private Image staminaImage;

    private static final int MAX_HEARTS = 10;

    private boolean visible = false; // start hidden

    @Override
    public void create() {
        super.create();
        addActors();
        setVisible(false); // ensure hidden at start

        entity.getEvents().addListener("updateHealth", this::updatePlayerHealthUI);
        entity.getEvents().addListener("updateStamina", this::updatePlayerStaminaUI);
    }

    private void addActors() {
        createHealthTable();
        stage.addActor(healthTable);

        createStaminaTable();
        stage.addActor(staminaTable);
    }

    private void createStaminaTable() {
        staminaTable = new Table();
        staminaTable.top().left();
        staminaTable.setFillParent(true);
        staminaTable.padTop(45f).padLeft(5f);
        staminaTable.setName("stamina");
        staminaTable.setUserObject(entity);

        float staminaSideLength = 30f;
        staminaImage = new Image(ServiceLocator.getResourceService().getAsset("images/playerstats/stamina.png", Texture.class));

        staminaLabel = new Label("Stamina: ", skin, "large");

        StaminaComponent staminaComp = entity.getComponent(StaminaComponent.class);
        staminaBar = new ProgressBar(0f, (float) staminaComp.getMaxStamina(), 1f, false, skin);
        staminaBar.setValue((float) staminaComp.getCurrentStamina());

        staminaTable.add(staminaImage).size(staminaSideLength).pad(5);
        staminaTable.add(staminaLabel);
        staminaTable.add(staminaBar);
    }

    private void createHealthTable() {
        healthTable = new Table();
        healthTable.top().left();
        healthTable.setFillParent(true);
        healthTable.padTop(5f).padLeft(5f);
        healthTable.setName("health");
        healthTable.setUserObject(entity);
        updateHealthTable(entity.getComponent(CombatStatsComponent.class).getHealth());
    }

    private void updateHealthTable(int playerHealth) {
        float heartSideLength = 30f;
        heartImage = new Image(ServiceLocator.getResourceService().getAsset("images/playerstats/health.png", Texture.class));

        int numHearts = playerHealth / MAX_HEARTS;

        healthLabel = new Label("Health: ", skin, "large");

        healthTable.add(heartImage).size(heartSideLength).pad(5);
        healthTable.add(healthLabel);
        for (int i = 0; i < numHearts; i++) {
            heartImage = new Image(ServiceLocator.getResourceService().getAsset("images/playerstats/health.png", Texture.class));
            healthTable.add(heartImage).size(heartSideLength).pad(5);
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        // draw is handled by the stage
    }

    public void updatePlayerHealthUI(int health) {
        healthTable.clear();
        updateHealthTable(health);
    }

    public void updatePlayerStaminaUI(float stamina) {
        staminaBar.setValue(stamina);
    }

    /** Toggle visibility of the stats HUD */
    public void setVisible(boolean visible) {
        this.visible = visible;
        if (healthTable != null) healthTable.setVisible(visible);
        if (staminaTable != null) staminaTable.setVisible(visible);
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (healthTable != null) healthTable.remove();
        if (staminaTable != null) staminaTable.remove();
    }
}
