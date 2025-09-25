package com.csse3200.game.components.statisticspage;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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

        // Add row containing buttons
        Table row = new Table();
        table.add(exitBtn).pad(15f).expand().bottom().left();
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
