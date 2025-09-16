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
        setLayer(0); // Render behind everything
    }

    // Non-tiled layers
    public void addLayer(Texture texture, float factor) {
        layers.add(new ParallaxLayer(texture, camera, factor, mapWidth, mapHeight));
    }

    // Tiled layers
    public void addTiledLayer(Texture texture, float factor, boolean tileHorizontally, boolean tileVertically, float tileWidth, float tileHeight) {
        layers.add(new ParallaxLayer(texture, camera, factor, mapWidth, mapHeight, tileHorizontally, tileVertically, tileWidth, tileHeight));
    }

    // Horizontal-only tiling
    public void addHorizontalTiledLayer(Texture texture, float factor, float tileWidth) {
        addTiledLayer(texture, factor, true, false, tileWidth, 0);
    }

    // Both directions
    public void addFullyTiledLayer(Texture texture, float factor, float tileWidth, float tileHeight) {
        addTiledLayer(texture, factor, true, true, tileWidth, tileHeight);
    }

    @Override
    protected void draw(SpriteBatch batch) {
        // Draw all layers in order (back to front)
        for (ParallaxLayer layer : layers) {
            layer.render(batch);
        }
    }

    @Override
    public int getLayer() {
        return Integer.MIN_VALUE;
    }
}