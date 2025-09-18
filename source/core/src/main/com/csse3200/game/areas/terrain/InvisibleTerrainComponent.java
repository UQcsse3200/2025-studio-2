package com.csse3200.game.areas.terrain;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;

/**
 * Invisible terrain component that provides grid functionality without rendering.
 * Used for entity positioning and collision boundaries while allowing custom backgrounds.
 */
public class InvisibleTerrainComponent extends TerrainComponent {

    public InvisibleTerrainComponent(OrthographicCamera camera, TiledMap map,
                                     TiledMapRenderer renderer, TerrainOrientation orientation,
                                     float tileSize) {
        super(camera, map, renderer, orientation, tileSize);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Do nothing
    }
}