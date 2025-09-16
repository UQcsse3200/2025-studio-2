package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ParallaxLayer {
    private Texture texture;
    private Camera camera;
    private float factor;
    private float mapWidth;
    private float mapHeight;

    public ParallaxLayer(Texture texture, Camera camera, float factor, float mapWidth, float mapHeight) {
        this.texture = texture;
        this.camera = camera;
        this.factor = factor;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    public void render(SpriteBatch batch) {
        float offsetX = camera.position.x * factor;
        float offsetY = (camera.position.y * factor) - 2.9f;

        // Get texture dimensions
        float textureWidth = texture.getWidth();
        float textureHeight = texture.getHeight();

        // Calculate scale to cover the entire map
        float scaleX = mapWidth / textureWidth / 2f;
        float scaleY = mapHeight / textureHeight / 2f;

        float scale = Math.max(scaleX, scaleY);

        // Calculate final dimensions after scaling
        float scaledWidth = textureWidth * scale;
        float scaledHeight = textureHeight * scale;

        // Center the cropped image
        float drawX = camera.position.x - scaledWidth / 2 - offsetX;
        float drawY = camera.position.y - scaledHeight / 2 - offsetY;

        batch.draw(texture, drawX, drawY, scaledWidth, scaledHeight);
    }
}