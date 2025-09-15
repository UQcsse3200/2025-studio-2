package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ParallaxLayer {
    private Texture texture;
    private Camera camera;
    private float factor;

    public ParallaxLayer(Texture texture, Camera camera, float factor) {
        this.texture = texture;
        this.camera = camera;
        this.factor = factor;
    }

    public void render(SpriteBatch batch) {
        // Calculate parallax offset
        float offsetX = camera.position.x * factor;
        float offsetY = camera.position.y * factor;

        // Get screen dimensions
        float screenWidth = camera.viewportWidth;
        float screenHeight = camera.viewportHeight;

        // Calculate where to draw (center the background on camera with offset)
        float drawX = camera.position.x - screenWidth / 2 - offsetX;
        float drawY = camera.position.y - screenHeight / 2 - offsetY;

        // Draw the texture scaled to fill the entire screen
        batch.draw(texture, drawX, drawY, screenWidth, screenHeight);
    }
}