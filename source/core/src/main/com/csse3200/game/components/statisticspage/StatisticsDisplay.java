package com.csse3200.game.components.statisticspage;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.GdxGame;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Statistics Page UI Class
 */
public class StatisticsDisplay extends UIComponent {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsDisplay.class);
    private final GdxGame game;
    private Sound buttonClickSound;

    private static final float Z_INDEX = 2f;
    private Table table;

    /**
     * Constructor
     */
    public StatisticsDisplay(GdxGame game) {
        super();
        this.game = game;
    }

    @Override
    public void create() {
        super.create();
        addActors();
    }

    /**
     * Add UI parts in
     */
    private void addActors() {
        Image background =
                new Image(
                        ServiceLocator.getResourceService()
                                .getAsset("images/superintelligence_menu_background.png", Texture.class));

        background.setFillParent(true);
        stage.addActor(background);

        buttonClickSound = ServiceLocator.getResourceService()
                .getAsset("sounds/buttonsound.mp3", Sound.class);

        table = new Table();
        table.setFillParent(true);
        table.center();

        Label title = new Label("Statistics", skin, "title");
        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top();
        topTable.add(title).expandX().center().padTop(20f);
        stage.addActor(topTable);

        Label playtimeLabel = new Label("Total Playtime:", skin);
        Label playtimeValue = new Label(StatsTracker.getPlaytimeMinutes() + " mins", skin);

        Label upgradesLabel = new Label("Upgrades picked up:", skin);
        Label upgradesValue = new Label(String.valueOf(StatsTracker.getUpgradesCollected()), skin);

        Label levelCompletedLabel = new Label("Levels completed:", skin);
        Label levelCompletedValue = new Label(String.valueOf(StatsTracker.getLevelsCompleted()), skin);

        Label deathCounterLabel = new Label("Deaths:", skin);
        Label deathCounterValue = new Label(String.valueOf(StatsTracker.getDeathCount()), skin);

        Label achievementCounterLabel = new Label("Achievements:", skin);
        Label achievementCounterValue = new Label(String.valueOf(StatsTracker.getAchievementsUnlocked()), skin);

        table.add(playtimeLabel).pad(10f).padLeft(50f).right();
        table.add(playtimeValue).pad(10f).left().row();

        table.add(upgradesLabel).pad(10f).padLeft(50f).right();
        table.add(upgradesValue).pad(10f).left().row();

        table.add(levelCompletedLabel).pad(10f).padLeft(50f).right();
        table.add(levelCompletedValue).pad(10f).left().row();

        table.add(deathCounterLabel).pad(10f).padLeft(50f).right();
        table.add(deathCounterValue).pad(10f).left().row();

        table.add(achievementCounterLabel).pad(10f).padLeft(50f).right();
        table.add(achievementCounterValue).pad(10f).left().row();

        TextButton exitBtn = new TextButton("Exit", skin);

        // Triggers an event when the button is pressed
        exitBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        buttonClickSound.play(UserSettings.get().masterVolume);
                        logger.debug("Exit button clicked");
                        entity.getEvents().trigger("exit");
                    }
                });

        Table bottomTable = new Table();
        bottomTable.bottom().left();
        bottomTable.setFillParent(true);
        bottomTable.add(exitBtn).pad(15f);

        TextButton resetBtn = new TextButton("Reset", skin);

        // Triggers an event when the button is pressed
        resetBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        buttonClickSound.play(UserSettings.get().masterVolume);
                        logger.debug("Reset button clicked");
                        entity.getEvents().trigger("reset");
                        entity.getEvents().trigger("exit");
                    }
                });

        Table bottomRight = new Table();
        bottomRight.bottom().right();
        bottomRight.setFillParent(true);
        bottomRight.add(resetBtn).pad(15f);

        stage.addActor(table);
        stage.addActor(bottomTable);
        stage.addActor(bottomRight);
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
