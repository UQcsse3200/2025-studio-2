package com.csse3200.game.rendering.parallax;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.csse3200.game.rendering.RenderComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * A render component that creates parallax scrolling backgrounds with multiple layers.
 * This component manages a collection of parallax layers that scroll at different speeds
 * relative to the camera movement, creating a depth effect.
 *
 * The component supports both tiled and stretched background layers, with configurable
 * scaling, offsets, and parallax factors for each layer.
 */
public class ParallaxBackgroundComponent extends RenderComponent {
    private final List<ParallaxLayer> layers;
    private final Camera camera;
    private final float mapWidth;
    private final float mapHeight;

    /**
     * Creates a new parallax background component.
     *
     * @param camera The camera to track for parallax calculations
     * @param mapWidth The total width of the game map
     * @param mapHeight The total height of the game map
     */
    public ParallaxBackgroundComponent(Camera camera, float mapWidth, float mapHeight) {
        this.camera = camera;
        this.layers = new ArrayList<>();
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        setLayer(0);
    }

    /**
     * Adds a basic parallax layer with default scaling and no offset.
     * The layer will be stretched to fit the viewport.
     *
     * @param texture The texture to use for this layer
     * @param factor The parallax factor (0.0 = static, 1.0 = moves with camera)
     */
    public void addLayer(Texture texture, float factor) {
        layers.add(new ParallaxLayer(texture, camera, factor, mapWidth, mapHeight));
    }

    /**
     * Adds a parallax layer with custom offset but default scaling.
     * The layer will be stretched to fit the viewport.
     *
     * @param texture The texture to use for this layer
     * @param factor The parallax factor (0.0 = static, 1.0 = moves with camera)
     * @param offsetX Horizontal offset in world units
     * @param offsetY Vertical offset in world units
     */
    public void addLayer(Texture texture, float factor, float offsetX, float offsetY) {
        layers.add(new ParallaxLayer(texture, camera, factor, mapWidth, mapHeight, offsetX, offsetY));
    }

    /**
     * Adds a parallax layer with custom offset and scaling.
     * The layer will be stretched to fit the viewport with the specified scale.
     *
     * @param texture The texture to use for this layer
     * @param factor The parallax factor (0.0 = static, 1.0 = moves with camera)
     * @param offsetX Horizontal offset in world units
     * @param offsetY Vertical offset in world units
     * @param scaleX Horizontal scale factor (1.0 = original size)
     * @param scaleY Vertical scale factor (1.0 = original size)
     */
    public void addLayer(Texture texture, float factor, float offsetX, float offsetY, float scaleX, float scaleY) {
        layers.add(new ParallaxLayer(texture, camera, factor, mapWidth, mapHeight,
            false, false, 0, 0, offsetX, offsetY, scaleX, scaleY));
    }

    /**
     * Adds a parallax layer with uniform scaling and custom offset.
     * Convenience method for when you want the same scale factor for both X and Y axes.
     *
     * @param texture The texture to use for this layer
     * @param factor The parallax factor (0.0 = static, 1.0 = moves with camera)
     * @param offsetX Horizontal offset in world units
     * @param offsetY Vertical offset in world units
     * @param scale Uniform scale factor applied to both X and Y axes
     */
    public void addScaledLayer(Texture texture, float factor, float offsetX, float offsetY, float scale) {
        addLayer(texture, factor, offsetX, offsetY, scale, scale);
    }

    /**
     * Adds a tiled parallax layer with custom offset.
     * The texture will be repeated across the specified dimensions instead of being stretched.
     *
     * @param texture The texture to use for this layer
     * @param factor The parallax factor (0.0 = static, 1.0 = moves with camera)
     * @param tileHorizontally Whether to tile the texture horizontally
     * @param tileVertically Whether to tile the texture vertically
     * @param tileWidth Width of each tile (0 = use texture width)
     * @param tileHeight Height of each tile (0 = use texture height)
     * @param offsetX Horizontal offset in world units
     * @param offsetY Vertical offset in world units
     */
    public void addTiledLayer(Texture texture, float factor, boolean tileHorizontally, boolean tileVertically,
                              float tileWidth, float tileHeight, float offsetX, float offsetY) {
        layers.add(new ParallaxLayer(texture, camera, factor, mapWidth, mapHeight,
            tileHorizontally, tileVertically, tileWidth, tileHeight, offsetX, offsetY));
    }

    /**
     * Adds a tiled parallax layer with custom offset and scaling.
     * The texture will be repeated with the specified scale applied to each tile.
     *
     * @param texture The texture to use for this layer
     * @param factor The parallax factor (0.0 = static, 1.0 = moves with camera)
     * @param tileHorizontally Whether to tile the texture horizontally
     * @param tileVertically Whether to tile the texture vertically
     * @param tileWidth Width of each tile before scaling (0 = use texture width)
     * @param tileHeight Height of each tile before scaling (0 = use texture height)
     * @param offsetX Horizontal offset in world units
     * @param offsetY Vertical offset in world units
     * @param scaleX Horizontal scale factor for tiles
     * @param scaleY Vertical scale factor for tiles
     */
    public void addTiledLayer(Texture texture, float factor, boolean tileHorizontally, boolean tileVertically,
                              float tileWidth, float tileHeight, float offsetX, float offsetY, float scaleX, float scaleY) {
        layers.add(new ParallaxLayer(texture, camera, factor, mapWidth, mapHeight,
            tileHorizontally, tileVertically, tileWidth, tileHeight, offsetX, offsetY, scaleX, scaleY));
    }

    /**
     * Renders all parallax layers in order from back to front.
     * Called automatically by the rendering system.
     *
     * @param batch The sprite batch to use for rendering
     */
    @Override
    public void draw(SpriteBatch batch) {
        for (ParallaxLayer layer : layers) {
            layer.render(batch);
        }
    }

    /**
     * Returns the render layer for this component.
     * Parallax backgrounds are rendered at the lowest priority to appear behind other elements.
     *
     * @return The render layer (Integer.MIN_VALUE for background priority)
     */
    @Override
    public int getLayer() {
        return Integer.MIN_VALUE;
    }
}