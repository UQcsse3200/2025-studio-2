package com.csse3200.game.components.leaderboardpage;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.LeaderboardComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Statistics Page UI Class
 */
public class LeaderboardDisplay extends UIComponent {

    private static final Logger logger = LoggerFactory.getLogger(LeaderboardDisplay.class);
    private final GdxGame game;

    private static final float Z_INDEX = 2f;
    private Table table;

    /**
     * Constructor
     */
    public LeaderboardDisplay(GdxGame game) {
        this.game = game;
    }

    /**
     * Creates the UI
     */
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

        table = new Table();
        table.setFillParent(true);
        table.center();

        Label title = new Label("Leaderboard", skin, "title");
        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top();
        topTable.add(title).expandX().center().padTop(20.0f);
        stage.addActor(topTable);


        Map<String, Long> leaderboardData = LeaderboardComponent.getInstance().getData();

        for (Map.Entry<String, Long> entry : leaderboardData.entrySet()) {
            Label name = new Label(entry.getKey() + ":", skin);
            Label time = new Label((double) entry.getValue() / 1000 + " (s)", skin);

            table.add(name).pad(10.0f).padLeft(50.0f).right();
            table.add(time).pad(10.0f).left().row();
        }

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
