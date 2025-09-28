package com.csse3200.game.components.statisticspage;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.GdxGame;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsDisplay extends UIComponent {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsDisplay.class);
    private final GdxGame game;

    private static final float Z_INDEX = 2f;
    private Table table;
    private Table topTable;


    public StatisticsDisplay(GdxGame game) {
        super();
        this.game = game;
    }

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
        table.center();

        Label title = new Label("Statistics", skin, "title");
        topTable = new Table();
        topTable.setFillParent(true);
        topTable.top();
        topTable.add(title).expandX().center().padTop(20f);
        stage.addActor(topTable);

        Label playtimeLabel =
                new Label("Total Playtime: " + StatsTracker.getPlaytimeMinutes() + " mins",
                skin);
        Label upgradesLabel =
                new Label("Upgrades picked up: " + StatsTracker.getUpgradesCollected(), skin);
        Label levelCompletedLabel =
                new Label("Levels completed: " + StatsTracker.getLevelsCompleted(), skin);
        Label deathCounterLabel =
                new Label("Deaths: " + StatsTracker.getDeathCount(),
                skin);
        Label achievementCounterLabel =
                new Label("Achievements: " + StatsTracker.getAchievementsUnlocked(),
                skin);

        table.add(playtimeLabel).pad(10f).padLeft(50f).row();
        table.add(upgradesLabel).pad(10f).padLeft(50f).row();
        table.add(levelCompletedLabel).pad(10f).padLeft(50f).row();
        table.add(deathCounterLabel).pad(10f).padLeft(50f).row();
        table.add(achievementCounterLabel).pad(10f).padLeft(50f).row();


        TextButton exitBtn = new TextButton("Exit", skin);

        // Triggers an event when the button is pressed
        exitBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {

                        logger.debug("Exit button clicked");
                        entity.getEvents().trigger("exit");
                    }
                });

        Table bottomTable = new Table();
        bottomTable.bottom().left();
        bottomTable.setFillParent(true);
        bottomTable.add(exitBtn).pad(15f);

        stage.addActor(table);
        stage.addActor(bottomTable);
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
