package com.csse3200.game.rendering.parallax;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ParallaxLayer {
    private Texture texture;
    private Camera camera;
    private float factor;
    private float mapWidth;
    private float mapHeight;
    private boolean tileHorizontally;
    private boolean tileVertically;
    private float tileWidth;
    private float tileHeight;

    // Original constructor for non-tiled layers
    public ParallaxLayer(Texture texture, Camera camera, float factor, float mapWidth, float mapHeight) {
        this(texture, camera, factor, mapWidth, mapHeight, false, false, 0, 0);
    }

    // New constructor for tiled layers
    public ParallaxLayer(Texture texture, Camera camera, float factor, float mapWidth, float mapHeight,
                         boolean tileHorizontally, boolean tileVertically, float tileWidth, float tileHeight) {
        this.texture = texture;
        this.camera = camera;
        this.factor = factor;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.tileHorizontally = tileHorizontally;
        this.tileVertically = tileVertically;
        this.tileWidth = tileWidth > 0 ? tileWidth : texture.getWidth();
        this.tileHeight = tileHeight > 0 ? tileHeight : texture.getHeight();
    }

    public void render(SpriteBatch batch) {
        float offsetX = camera.position.x * factor;
        float offsetY = (camera.position.y * factor) - 2.9f;

        if (tileHorizontally || tileVertically) {
            renderTiled(batch, offsetX, offsetY);
        } else {
            renderStretched(batch, offsetX, offsetY);
        }
    }

    private void renderTiled(SpriteBatch batch, float offsetX, float offsetY) {
        // Calculate visible area with some padding
        float padding = 2.0f;
        float viewLeft = camera.position.x - (camera.viewportWidth / 2) - padding;
        float viewRight = camera.position.x + (camera.viewportWidth / 2) + padding;
        float viewBottom = camera.position.y - (camera.viewportHeight / 2) - padding;
        float viewTop = camera.position.y + (camera.viewportHeight / 2) + padding;

        // Calculate tile range to draw
        int startTileX, endTileX, startTileY, endTileY;

        if (tileHorizontally) {
            startTileX = (int) Math.floor((viewLeft + offsetX) / tileWidth);
            endTileX = (int) Math.ceil((viewRight + offsetX) / tileWidth);
        } else {
            startTileX = 0;
            endTileX = 1;
        }

        if (tileVertically) {
            startTileY = (int) Math.floor((viewBottom + offsetY) / tileHeight);
            endTileY = (int) Math.ceil((viewTop + offsetY) / tileHeight);
        } else {
            startTileY = 0;
            endTileY = 1;
        }

        // Draw tiles
        for (int x = startTileX; x <= endTileX; x++) {
            for (int y = startTileY; y <= endTileY; y++) {
                float drawX = x * tileWidth - offsetX;
                float drawY = y * tileHeight - offsetY;

                batch.draw(texture, drawX, drawY, tileWidth, tileHeight);
            }
        }
    }

    private void renderStretched(SpriteBatch batch, float offsetX, float offsetY) {
        // Original rendering logic
        float textureWidth = texture.getWidth();
        float textureHeight = texture.getHeight();

        float scaleX = mapWidth / textureWidth / 2f;
        float scaleY = mapHeight / textureHeight / 2f;
        float scale = Math.max(scaleX, scaleY);

        float scaledWidth = textureWidth * scale;
        float scaledHeight = textureHeight * scale;

        float drawX = camera.position.x - scaledWidth / 2 - offsetX;
        float drawY = camera.position.y - scaledHeight / 2 - offsetY;

        batch.draw(texture, drawX, drawY, scaledWidth, scaledHeight);
    }
}