package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.components.LeaderboardComponent;
import com.csse3200.game.ui.UIComponent;

public class LeaderboardEntryDisplay extends UIComponent {
    private Table table;
    private Label promptLabel;
    private TextField nameField;
    private TextButton confirmButton;
    private TextButton skipButton;
    private boolean completed = false;

    private long completionTime;
    private int health;
    private float stamina;
    private LeaderboardComponent leaderboard;
    private String enteredName; // null if skipped or empty

    public LeaderboardEntryDisplay(long completionTime, int health, float stamina) {
        this.completionTime = completionTime;
        this.health = health;
        this.stamina = stamina;
        this.leaderboard = new LeaderboardComponent();
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
        table = new Table();
        table.setFillParent(true);
        table.center();

        promptLabel = new Label("You finished in " + formatTime(completionTime) +
                "! Enter your name:", skin, "large");

        nameField = new TextField("", skin);
        nameField.setMessageText("Your name");

        confirmButton = new TextButton("Confirm", skin);
        confirmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (completed) return;
                completed = true;
                enteredName = nameField.getText().trim();
                if (enteredName != null && !enteredName.isEmpty()) {
                    leaderboard.updateLeaderboard(enteredName, completionTime);
                    System.out.println("Saved stats for " + enteredName +
                            " | Health: " + health + " | Stamina: " + stamina);
                } else {
                    enteredName = null; // treat empty as skip
                }
                closeAndContinue();
            }
        });

        skipButton = new TextButton("Skip", skin);
        skipButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (completed) return;
                completed = true;
                enteredName = null; // explicit skip
                closeAndContinue();
            }
        });

        table.add(promptLabel).pad(10).row();
        table.add(nameField).width(200).pad(10).row();
        table.add(confirmButton).pad(10).row();
        table.add(skipButton).pad(10).row();

        stage.addActor(table);
    }

    private void closeAndContinue() {
        // Prevent multiple actions
        confirmButton.setDisabled(true);
        skipButton.setDisabled(true);

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
        if (table != null) {
            table.remove();
            table = null;
        }
    }
}