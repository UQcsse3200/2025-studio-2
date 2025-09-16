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
    private float offsetX;
    private float offsetY;
    private float scaleX; // Add scale parameters
    private float scaleY;

    // Original constructor (no scale - defaults to 1.0f)
    public ParallaxLayer(Texture texture, Camera camera, float factor, float mapWidth, float mapHeight) {
        this(texture, camera, factor, mapWidth, mapHeight, false, false, 0, 0, 0, 0, 1.0f, 1.0f);
    }

    // Constructor with offset (no scale - defaults to 1.0f)
    public ParallaxLayer(Texture texture, Camera camera, float factor, float mapWidth, float mapHeight, float offsetX, float offsetY) {
        this(texture, camera, factor, mapWidth, mapHeight, false, false, 0, 0, offsetX, offsetY, 1.0f, 1.0f);
    }

    // Constructor for tiled layers with offset (no scale - defaults to 1.0f)
    public ParallaxLayer(Texture texture, Camera camera, float factor, float mapWidth, float mapHeight,
                         boolean tileHorizontally, boolean tileVertically, float tileWidth, float tileHeight,
                         float offsetX, float offsetY) {
        this(texture, camera, factor, mapWidth, mapHeight, tileHorizontally, tileVertically,
            tileWidth, tileHeight, offsetX, offsetY, 1.0f, 1.0f);
    }

    // Full constructor with scale
    public ParallaxLayer(Texture texture, Camera camera, float factor, float mapWidth, float mapHeight,
                         boolean tileHorizontally, boolean tileVertically, float tileWidth, float tileHeight,
                         float offsetX, float offsetY, float scaleX, float scaleY) {
        this.texture = texture;
        this.camera = camera;
        this.factor = factor;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.tileHorizontally = tileHorizontally;
        this.tileVertically = tileVertically;
        this.tileWidth = tileWidth > 0 ? tileWidth : texture.getWidth();
        this.tileHeight = tileHeight > 0 ? tileHeight : texture.getHeight();
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.scaleX = scaleX > 0 ? scaleX : 1.0f;
        this.scaleY = scaleY > 0 ? scaleY : 1.0f;
    }

    public void render(SpriteBatch batch) {
        float parallaxOffsetX = camera.position.x * factor;
        float parallaxOffsetY = (camera.position.y * factor) - 2.9f;

        if (tileHorizontally || tileVertically) {
            renderTiled(batch, parallaxOffsetX, parallaxOffsetY);
        } else {
            renderStretched(batch, parallaxOffsetX, parallaxOffsetY);
        }
    }

    private void renderTiled(SpriteBatch batch, float parallaxOffsetX, float parallaxOffsetY) {
        // Apply scale to tile dimensions
        float scaledTileWidth = tileWidth * scaleX;
        float scaledTileHeight = tileHeight * scaleY;

        // Calculate visible area with some padding
        float padding = 2.0f;
        float viewLeft = camera.position.x - (camera.viewportWidth / 2) - padding;
        float viewRight = camera.position.x + (camera.viewportWidth / 2) + padding;
        float viewBottom = camera.position.y - (camera.viewportHeight / 2) - padding;
        float viewTop = camera.position.y + (camera.viewportHeight / 2) + padding;

        // Apply layer offset to the visible area calculations
        viewLeft += parallaxOffsetX + offsetX;
        viewRight += parallaxOffsetX + offsetX;
        viewBottom += parallaxOffsetY + offsetY;
        viewTop += parallaxOffsetY + offsetY;

        // Calculate tile range to draw using scaled tile dimensions
        int startTileX, endTileX, startTileY, endTileY;

        if (tileHorizontally) {
            startTileX = (int) Math.floor(viewLeft / scaledTileWidth);
            endTileX = (int) Math.ceil(viewRight / scaledTileWidth);
        } else {
            startTileX = 0;
            endTileX = 1;
        }

        if (tileVertically) {
            startTileY = (int) Math.floor(viewBottom / scaledTileHeight);
            endTileY = (int) Math.ceil(viewTop / scaledTileHeight);
        } else {
            startTileY = 0;
            endTileY = 1;
        }

        // Draw tiles with scale applied
        for (int x = startTileX; x <= endTileX; x++) {
            for (int y = startTileY; y <= endTileY; y++) {
                float drawX = x * scaledTileWidth - parallaxOffsetX - offsetX;
                float drawY = y * scaledTileHeight - parallaxOffsetY - offsetY;

                batch.draw(texture, drawX, drawY, scaledTileWidth, scaledTileHeight);
            }
        }
    }

    private void renderStretched(SpriteBatch batch, float parallaxOffsetX, float parallaxOffsetY) {
        // Use custom scale instead of auto-calculated scale
        float textureWidth = texture.getWidth();
        float textureHeight = texture.getHeight();

        float scaledWidth = textureWidth * scaleX;
        float scaledHeight = textureHeight * scaleY;

        float drawX = camera.position.x - scaledWidth / 2 - parallaxOffsetX - offsetX;
        float drawY = camera.position.y - scaledHeight / 2 - parallaxOffsetY - offsetY;

        batch.draw(texture, drawX, drawY, scaledWidth, scaledHeight);
    }
}