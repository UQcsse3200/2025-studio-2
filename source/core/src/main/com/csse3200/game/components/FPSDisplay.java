package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.csse3200.game.ui.UIComponent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class FPSDisplay extends UIComponent {
    /**
     * Label used to show the game's current FPS
     */
    private Label fpsLabel;

    /**
     * Creates all widgets to be drawn.
     */
    @Override
    public void create() {
        super.create();
        addActors();
    }

    /**
     * Initialises the FPS label and adds it to the stage.
     */
    private void addActors() {
        // Create label with current FPS
        fpsLabel = new Label("FPS: " + Gdx.graphics.getFramesPerSecond(), skin);
        stage.addActor(fpsLabel);
    }

    /**
     * Draws the FPS label in the top left of the screen.
     * @param batch Batch to render to.
     */
    @Override
    public void draw(SpriteBatch batch) {
        // Draw FPS at top-left corner of screen
        int screenHeight = Gdx.graphics.getHeight();
        float offsetX = 10f;
        float offsetY = 30f;
        fpsLabel.setPosition(offsetX, screenHeight - offsetY);
    }

    /**
     * Removes the FPS label.
     */
    public void dispose() {
        fpsLabel.remove();
        super.dispose();
    }

  /**
   * Resets the text inside the FPS label to be current FPS.
   */
  public void update() {
        // Continuously update FPS to contain current FPS
        fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
    }
}
