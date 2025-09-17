package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/** Centre-screen arrow that points toward the last damage source, then fades out. */
public class DamageIndicatorUI extends UIComponent {
    private Image arrow;
    private Vector2 lastDirection = new Vector2(1, 0);
    private float timer = 0f;

    private static final float SHOW_TIME = 1.35f;   // seconds
    private static final float MIN_ALPHA = 0.25f;   // end alpha
    private static final float MAX_ALPHA = 0.95f;   // start alpha

    @Override
    public void create() {
        super.create();

        arrow = new Image(new Texture("images/damage_arrow.png")); // add this asset in step 3
        arrow.setVisible(false);
        arrow.setColor(new Color(1f, 0.25f, 0.25f, 0f));
        arrow.setOrigin(arrow.getWidth() * 0.5f, arrow.getHeight() * 0.5f);

        Stage stage = ServiceLocator.getRenderService().getStage();
        Table table = new Table();
        table.setFillParent(true);
        table.add(arrow).expand().center();
        stage.addActor(table);

        // Listen for direction events from TouchAttackComponent
        entity.getEvents().addListener("damageDirection", this::onDamageDirection);
    }

    private void onDamageDirection(Vector2 dir) {
        if (dir == null || dir.isZero()) return;
        lastDirection.set(dir).nor();
        timer = SHOW_TIME;
        arrow.setVisible(true);
        arrow.setRotation(lastDirection.angleDeg());
        arrow.setColor(1f, 0.25f, 0.25f, MAX_ALPHA);
    }

    @Override
    public void update() {
        if (!arrow.isVisible()) return;

        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        timer -= dt;
        if (timer <= 0f) {
            arrow.setVisible(false);
            arrow.setColor(1f, 0.25f, 0.25f, 0f);
            return;
        }

        // Keep angle fresh in case of rapid successive hits
        arrow.setRotation(lastDirection.angleDeg());

        // Linear fade from MAX_ALPHA -> MIN_ALPHA
        float t = 1f - (timer / SHOW_TIME);
        float alpha = MAX_ALPHA - t * (MAX_ALPHA - MIN_ALPHA);
        arrow.setColor(1f, 0.25f, 0.25f, alpha);
    }

    @Override public void draw(SpriteBatch batch) {} // Stage renders UI
    @Override public void dispose() { super.dispose(); if (arrow != null) arrow.remove(); }
}
