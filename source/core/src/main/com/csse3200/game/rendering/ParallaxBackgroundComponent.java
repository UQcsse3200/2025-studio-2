package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.csse3200.game.services.ServiceLocator;
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

    public void addLayer(Texture texture, float factor) {
        layers.add(new ParallaxLayer(texture, camera, factor, mapWidth, mapHeight));
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