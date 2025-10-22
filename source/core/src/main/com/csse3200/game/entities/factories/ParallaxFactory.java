package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ParallaxConfig;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.rendering.parallax.ParallaxBackgroundComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

import java.util.Comparator;
import java.util.List;

public final class ParallaxFactory {
    private ParallaxFactory() {}

    public static Entity createParallax(String configPath, Camera camera, GridPoint2 mapSize) {
        ParallaxConfig cfg = FileLoader.readClass(ParallaxConfig.class, configPath);
        ResourceService rs = ServiceLocator.getResourceService();

        ParallaxBackgroundComponent pbg = new ParallaxBackgroundComponent(camera, mapSize.x, mapSize.y);

        addList(pbg, camera, rs, cfg.layers,  false);
        addList(pbg, camera, rs, cfg.overlays, true);

        return new Entity().addComponent(pbg);
    }

    private static void addList(ParallaxBackgroundComponent pbg,
                                Camera camera,
                                ResourceService rs,
                                List<ParallaxConfig.Layer> list,
                                boolean isOverlay) {
        if (list == null || list.isEmpty()) return;

        // Draw far -> near regardless of JSON order
        list.sort(Comparator.comparing(l -> l == null ? 0f : l.factor));

        for (ParallaxConfig.Layer l : list) {
            if (l == null || l.texture == null) continue;

            Texture tex = rs.getAsset(l.texture, Texture.class);
            if (tex == null) {
                throw new IllegalStateException("Parallax texture not loaded: " + l.texture);
            }

            // Smoother scaling (switch to Nearest if you want crisp pixel look)
            tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);

            float coverage = (l.coverage > 0f) ? l.coverage : (isOverlay ? 1.10f : 1.00f);
            float scale    = (l.scale    > 0f) ? l.scale    : autoScaleWidth(tex, camera, coverage);

            if (l.tiled) {
                // We explicitly draw repeated quads; wrap mode not required.
                pbg.addTiledLayer(
                        tex, l.factor,
                        true,  false,      // horizontal tiling
                        0f, 0f,            // 0 => use texture W/H
                        l.offsetX, l.offsetY,
                        scale, scale);
            } else {
                pbg.addScaledLayer(tex, l.factor, l.offsetX, l.offsetY, scale);
            }
        }
    }

    /** Auto-scale to fit WIDTH ≈ viewportWidth * coverage when scale==0. */
    private static float autoScaleWidth(Texture tex, Camera cam, float coverage) {
        float camW = (cam instanceof OrthographicCamera)
                ? ((OrthographicCamera) cam).viewportWidth
                : 1f;
        return (tex.getWidth() == 0) ? 1f : (camW / tex.getWidth()) * coverage;
    }
}
