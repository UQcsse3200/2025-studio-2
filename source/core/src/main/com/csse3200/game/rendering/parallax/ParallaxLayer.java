package com.csse3200.game.rendering.parallax;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Represents a single layer in a parallax background system.
 * Each layer can scroll at a different speed relative to the camera movement,
 * creating depth perception. Layers can be either tiled (repeated) or stretched
 * to fill the viewport.
 */
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

    /**
     * Creates a basic parallax layer with default settings.
     * The layer will be stretched and use default scaling (1.0).
     *
     * @param texture The texture to display
     * @param camera The camera to track for parallax calculations
     * @param factor The parallax factor (0.0 = static, 1.0 = moves with camera)
     * @param mapWidth The total width of the game map
     * @param mapHeight The total height of the game map
     */
    public ParallaxLayer(Texture texture, Camera camera, float factor, float mapWidth, float mapHeight) {
        this(texture, camera, factor, mapWidth, mapHeight, false, false, 0, 0, 0, 0, 1.0f, 1.0f);
    }

    /**
     * Creates a parallax layer with custom offset.
     * The layer will be stretched and use default scaling (1.0).
     *
     * @param texture The texture to display
     * @param camera The camera to track for parallax calculations
     * @param factor The parallax factor (0.0 = static, 1.0 = moves with camera)
     * @param mapWidth The total width of the game map
     * @param mapHeight The total height of the game map
     * @param offsetX Horizontal offset in world units
     * @param offsetY Vertical offset in world units
     */
    public ParallaxLayer(Texture texture, Camera camera, float factor, float mapWidth, float mapHeight, float offsetX, float offsetY) {
        this(texture, camera, factor, mapWidth, mapHeight, false, false, 0, 0, offsetX, offsetY, 1.0f, 1.0f);
    }

    /**
     * Creates a tiled parallax layer with custom offset.
     * The texture will be repeated according to the tiling parameters.
     *
     * @param texture The texture to display
     * @param camera The camera to track for parallax calculations
     * @param factor The parallax factor (0.0 = static, 1.0 = moves with camera)
     * @param mapWidth The total width of the game map
     * @param mapHeight The total height of the game map
     * @param tileHorizontally Whether to tile the texture horizontally
     * @param tileVertically Whether to tile the texture vertically
     * @param tileWidth Width of each tile (0 = use texture width)
     * @param tileHeight Height of each tile (0 = use texture height)
     * @param offsetX Horizontal offset in world units
     * @param offsetY Vertical offset in world units
     */
    public ParallaxLayer(Texture texture, Camera camera, float factor, float mapWidth, float mapHeight,
                         boolean tileHorizontally, boolean tileVertically, float tileWidth, float tileHeight,
                         float offsetX, float offsetY) {
        this(texture, camera, factor, mapWidth, mapHeight, tileHorizontally, tileVertically,
            tileWidth, tileHeight, offsetX, offsetY, 1.0f, 1.0f);
    }

    /**
     * Creates a fully customizable parallax layer.
     * This is the master constructor that all other constructors delegate to.
     *
     * @param texture The texture to display
     * @param camera The camera to track for parallax calculations
     * @param factor The parallax factor (0.0 = static, 1.0 = moves with camera)
     * @param mapWidth The total width of the game map
     * @param mapHeight The total height of the game map
     * @param tileHorizontally Whether to tile the texture horizontally
     * @param tileVertically Whether to tile the texture vertically
     * @param tileWidth Width of each tile before scaling (0 = use texture width)
     * @param tileHeight Height of each tile before scaling (0 = use texture height)
     * @param offsetX Horizontal offset in world units
     * @param offsetY Vertical offset in world units
     * @param scaleX Horizontal scale factor (1.0 = original size)
     * @param scaleY Vertical scale factor (1.0 = original size)
     */
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

    /**
     * Renders this parallax layer to the screen.
     * Calculates the appropriate position based on camera movement and parallax factor,
     * then renders either as tiles or stretched based on configuration.
     *
     * @param batch The sprite batch to use for rendering
     */
    public void render(SpriteBatch batch) {
        float parallaxOffsetX = camera.position.x * factor;
        float parallaxOffsetY = (camera.position.y * factor) - 2.9f;

        if (tileHorizontally || tileVertically) {
            renderTiled(batch, parallaxOffsetX, parallaxOffsetY);
        } else {
            renderStretched(batch, parallaxOffsetX, parallaxOffsetY);
        }
    }

    /**
     * Renders the layer as repeating tiles.
     * Calculates which tiles are visible and only renders those for performance.
     *
     * @param batch The sprite batch to use for rendering
     * @param parallaxOffsetX The calculated horizontal parallax offset
     * @param parallaxOffsetY The calculated vertical parallax offset
     */
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

    /**
     * Renders the layer as a single stretched texture.
     * The texture is scaled and positioned to create the parallax effect.
     *
     * @param batch The sprite batch to use for rendering
     * @param parallaxOffsetX The calculated horizontal parallax offset
     * @param parallaxOffsetY The calculated vertical parallax offset
     */
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