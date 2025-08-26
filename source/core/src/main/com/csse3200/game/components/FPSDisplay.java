package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.csse3200.game.ui.UIComponent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class FPSDisplay extends UIComponent {
    private Label fpsLabel;

    @Override
    public void create() {
        super.create();
        addActors();
    }

    private void addActors() {
        // Create label with current FPS
        fpsLabel = new Label("FPS: " + Gdx.graphics.getFramesPerSecond(), skin);
        stage.addActor(fpsLabel);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Draw FPS at top-left corner of screen
        int screenHeight = Gdx.graphics.getHeight();
        float offsetX = 10f;
        float offsetY = 30f;
        fpsLabel.setPosition(offsetX, screenHeight - offsetY);
    }

    public void dispose() {
        fpsLabel.remove();
        super.dispose();
    }

    public void update() {
        // Continuously update FPS to contain current FPS
        fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
    }
}
