package com.csse3200.game.rendering.parallax;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.csse3200.game.rendering.RenderComponent;

import java.util.ArrayList;
import java.util.List;

public class ParallaxBackgroundComponent extends RenderComponent {
    private final List<ParallaxLayer> layers;
    private final Camera camera;
    private final float mapWidth;
    private final float mapHeight;

    public ParallaxBackgroundComponent(Camera camera, float mapWidth, float mapHeight) {
        this.camera = camera;
        this.layers = new ArrayList<>();
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        setLayer(0);
    }

    // Original method for non-tiled layers
    public void addLayer(Texture texture, float factor) {
        layers.add(new ParallaxLayer(texture, camera, factor, mapWidth, mapHeight));
    }

    // Method for non-tiled layers with offset
    public void addLayer(Texture texture, float factor, float offsetX, float offsetY) {
        layers.add(new ParallaxLayer(texture, camera, factor, mapWidth, mapHeight, offsetX, offsetY));
    }

    // Method for tiled layers with offset
    public void addTiledLayer(Texture texture, float factor, boolean tileHorizontally, boolean tileVertically,
                              float tileWidth, float tileHeight, float offsetX, float offsetY) {
        layers.add(new ParallaxLayer(texture, camera, factor, mapWidth, mapHeight,
            tileHorizontally, tileVertically, tileWidth, tileHeight, offsetX, offsetY));
    }

    // Convenience method for horizontal-only tiling with offset
    public void addHorizontalTiledLayer(Texture texture, float factor, float tileWidth, float offsetX, float offsetY) {
        addTiledLayer(texture, factor, true, false, tileWidth, 0, offsetX, offsetY);
    }

    // Convenience method for horizontal-only tiling with Y offset only
    public void addHorizontalTiledLayerAtHeight(Texture texture, float factor, float tileWidth, float height) {
        addTiledLayer(texture, factor, true, false, tileWidth, 0, 0, height);
    }

    // Convenience method for fully tiled layers with offset
    public void addFullyTiledLayer(Texture texture, float factor, float tileWidth, float tileHeight, float offsetX, float offsetY) {
        addTiledLayer(texture, factor, true, true, tileWidth, tileHeight, offsetX, offsetY);
    }

    @Override
    protected void draw(SpriteBatch batch) {
        for (ParallaxLayer layer : layers) {
            layer.render(batch);
        }
    }

    @Override
    public int getLayer() {
        return Integer.MIN_VALUE;
    }
}