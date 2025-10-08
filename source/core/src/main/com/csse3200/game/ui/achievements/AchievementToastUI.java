package com.csse3200.game.ui.achievements;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.achievements.AchievementId;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.achievements.AchievementService;

public class AchievementToastUI extends UIComponent implements AchievementService.Listener {
    private Table container;
    private Label label;

    @Override
    public void create() {
        super.create();
        Stage stage = ServiceLocator.getRenderService().getStage();

        container = new Table();
        container.setFillParent(true);
        container.top().padTop(40f); // top centre
        label = new Label("", skin, "title");
        label.getColor().a = 0f;
        container.add(label).center();
        stage.addActor(container);
        AchievementService.get().addListener(new AchievementToastUI());

        AchievementService.get().addListener(this);
    }

    @Override
    public void dispose() {
        AchievementService.get().removeListener(this);
        if (container != null) container.remove();
        super.dispose();
    }

    @Override
    public void draw(SpriteBatch batch) { }

    @Override
    public void onUnlocked(AchievementId id, String title, String description) {
        label.setText("Achievement Unlocked: " + title);
        label.clearActions();
        label.getColor().a = 0f;
        label.addAction(Actions.sequence(
                Actions.fadeIn(0.25f),
                Actions.delay(1.75f),
                Actions.fadeOut(0.5f)
        ));
    }
}
