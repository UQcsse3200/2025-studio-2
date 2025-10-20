package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.ui.HoverEffectHelper;
import com.csse3200.game.ui.UIComponent;
import java.util.Arrays;

public class LeaderboardEntryDisplay extends UIComponent {
    private Table rootTable;
  TextField nameField;
    TextButton confirmButton;
    TextButton skipButton;
    private boolean completed;

    private final long completionTime;
    private String enteredName; // null if skipped or empty
    Label errorLabel;
    // Overlay manager for global UI coordination
    public static final class UIOverlayManager {
        private static boolean overlayActive = false;
        public static boolean isOverlayActive() { return overlayActive; }
        public static void setOverlayActive(boolean active) { overlayActive = active; }
    }

    public LeaderboardEntryDisplay(long completionTime) {
        this.completionTime = completionTime;
    }

    public String getEnteredName() {
        return enteredName;
    }

    @Override
    public void create() {
        super.create();
        addActors();
    }

    private void addActors() {
        // Root table with dim background
        rootTable = new Table();
        rootTable.setFillParent(true);

        // Semi-transparent black background
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.7f);
        pixmap.fill();
        Texture blackTexture = new Texture(pixmap);
        pixmap.dispose();
        Image background = new Image(blackTexture);
        rootTable.setBackground(background.getDrawable());

        // Content table centered
        Table contentTable = new Table();
        contentTable.center();

        // Prompt
        Label promptLabel = new Label("You finished in " + formatTime(completionTime) + "! Enter your name:", skin, "large");

        //Error Label if user leaves the field empty and press 'Confirm'
        errorLabel = new Label("", skin, "default");
        errorLabel.setColor(1, 0, 0, 1); // red text
        errorLabel.setVisible(false);    // hidden by default

        // Name field
        nameField = new TextField("", skin);
        nameField.setMessageText("Your name");
        stage.setKeyboardFocus(nameField); // auto-focus

        // Buttons
        confirmButton = new TextButton("Confirm", skin, "mainMenu");
        skipButton = new TextButton("Skip", skin, "mainMenu");

        for (TextButton btn : Arrays.asList(confirmButton, skipButton)) {
            btn.setTransform(true);
            btn.setOrigin(Align.center);
        }
        HoverEffectHelper.applyHoverEffects(Arrays.asList(confirmButton, skipButton));

        // Button listeners
        confirmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (completed) return;
                enteredName = nameField.getText().trim();
                if (enteredName.isEmpty()) {
                    errorLabel.setText("Name cannot be empty!");
                    errorLabel.setVisible(true);
                    return; // don’t close yet
                }
                closeAndContinue();
            }
        });


        skipButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (completed) return;
                completed = true;
                enteredName = null; // explicit skip
                closeAndContinue();
            }
        });

        // Layout
        contentTable.add(promptLabel).pad(10).row();
        contentTable.add(nameField).width(200).pad(10).row();

        // Buttons side by side
        Table buttonRow = new Table();
        buttonRow.add(confirmButton).pad(10);
        buttonRow.add(skipButton).pad(10);
        contentTable.add(buttonRow).padTop(10).row();
        contentTable.add(errorLabel).padTop(5).row();
        rootTable.add(contentTable).expand().center();
        stage.addActor(rootTable);

        // Hide other UI + block input while leaderboard is active
        hideOtherUIElements();
        TooltipSystem.TooltipManager.setSuppressed(true);
        UIOverlayManager.setOverlayActive(true);
        blockAllInput();
    }

    private void closeAndContinue() {
        confirmButton.setDisabled(true);
        skipButton.setDisabled(true);

        // Restore UI + input
        showOtherUIElements();
        TooltipSystem.TooltipManager.setSuppressed(false);
        unblockAllInput();
        UIOverlayManager.setOverlayActive(false);

        dispose();
        entity.getEvents().trigger("leaderboardEntryComplete");
    }

    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long remSeconds = seconds % 60;
        long millis = (ms % 1000) / 10;
        return String.format("%02d:%02d.%02d", minutes, remSeconds, millis);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // stage handles drawing
    }

    @Override
    public void dispose() {
        super.dispose();
        if (rootTable != null) {
            rootTable.remove();
            rootTable = null;
        }
    }

    // --- Helpers to hide/show other UI ---
    private void hideOtherUIElements() {
        Actor minimapActor = stage.getRoot().findActor("minimap");
        if (minimapActor != null) minimapActor.setVisible(false);

        Actor healthActor = stage.getRoot().findActor("health");
        if (healthActor != null) healthActor.setVisible(false);

        Actor staminaActor = stage.getRoot().findActor("stamina");
        if (staminaActor != null) staminaActor.setVisible(false);

        Actor exitActor = stage.getRoot().findActor("exit");
        if (exitActor != null) exitActor.setVisible(false);

        Actor titleActor = stage.getRoot().findActor("title");
        if (titleActor != null) titleActor.setVisible(false);

        Actor inputsCollectedActor = stage.getRoot().findActor("inputsCollected");
        if (inputsCollectedActor != null) inputsCollectedActor.setVisible(false);
    }

    private void showOtherUIElements() {
        Actor minimapActor = stage.getRoot().findActor("minimap");
        if (minimapActor != null) minimapActor.setVisible(true);

        Actor healthActor = stage.getRoot().findActor("health");
        if (healthActor != null) healthActor.setVisible(true);

        Actor staminaActor = stage.getRoot().findActor("stamina");
        if (staminaActor != null) staminaActor.setVisible(true);

        Actor exitActor = stage.getRoot().findActor("exit");
        if (exitActor != null) exitActor.setVisible(true);

        Actor titleActor = stage.getRoot().findActor("title");
        if (titleActor != null) titleActor.setVisible(true);

        Actor tooltipActor = stage.getRoot().findActor("useKeyTooltip");
        if (tooltipActor != null) tooltipActor.setVisible(true);

        Actor inputsCounterActor = stage.getRoot().findActor("inputsCounter");
        if (inputsCounterActor != null) inputsCounterActor.setVisible(true);

        Actor inputsCollectedActor = stage.getRoot().findActor("inputsCollected");
        if (inputsCollectedActor != null) inputsCollectedActor.setVisible(true);
    }

    // --- Input blocking ---
    private void blockAllInput() {
        if (entity != null && entity.getComponent(KeyboardPlayerInputComponent.class) != null) {
            entity.getComponent(KeyboardPlayerInputComponent.class).setEnabled(false);
        }
    }

    private void unblockAllInput() {
        if (entity != null && entity.getComponent(KeyboardPlayerInputComponent.class) != null) {
            entity.getComponent(KeyboardPlayerInputComponent.class).setEnabled(true);
        }
    }
}
