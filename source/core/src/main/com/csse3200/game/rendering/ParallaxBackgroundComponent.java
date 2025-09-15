package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;

public class ParallaxBackgroundComponent extends RenderComponent {
    private List<ParallaxLayer> layers;
    private Camera camera;

    public ParallaxBackgroundComponent(Camera camera) {
        this.camera = camera;
        this.layers = new ArrayList<>();
        setLayer(-100); // Render behind everything
    }

    public void addLayer(Texture texture, float factor) {
        layers.add(new ParallaxLayer(texture, camera, factor));
    }

    @Override
    protected void draw(SpriteBatch batch) {
        // Draw all layers in order (back to front)
        for (ParallaxLayer layer : layers) {
            layer.render(batch);
        }
    }
}