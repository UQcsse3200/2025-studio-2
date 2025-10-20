package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * A render component for dynamically tiling a wall texture vertically.
 */
public class TiledWallComponent extends RenderComponent {
    private final TextureRegion topTile;
    private final TextureRegion middleTile;
    private final float tileAspectRatio;
    private final float topTileAspectRatio;

    /**
     * Creates a new TiledWallComponent.
     *
     * @param topTile    The texture for the top of the wall.
     * @param middleTile The texture for repeating middle sections of the wall.
     */
    public TiledWallComponent(TextureRegion topTile, TextureRegion middleTile) {
        this.topTile = topTile;
        this.middleTile = middleTile;
        topTile.setRegionWidth(middleTile.getRegionWidth());

        tileAspectRatio = (float) middleTile.getRegionWidth() / middleTile.getRegionHeight();
        topTileAspectRatio = (float) topTile.getRegionWidth() / topTile.getRegionHeight();
    }

    @Override
    protected void draw(SpriteBatch batch) {
        Vector2 position = entity.getPosition();
        Vector2 scale = entity.getScale();
        float tileWorldWidth = scale.x;
        float topTileWorldHeight = tileWorldWidth / topTileAspectRatio;
        float tileWorldHeight = tileWorldWidth / tileAspectRatio;
        int totalTilesToDraw = MathUtils.ceil((scale.y - topTileWorldHeight) / tileWorldHeight);

        if (0 == totalTilesToDraw) return;

        float stretchedTileHeight = (scale.y - topTileWorldHeight) / totalTilesToDraw;
        for (int i = 0; i < totalTilesToDraw; i++) {
            float currentY = position.y + i * stretchedTileHeight;
            batch.draw(middleTile, position.x, currentY, tileWorldWidth, stretchedTileHeight);
        }


        float topY = position.y + totalTilesToDraw * stretchedTileHeight;
        batch.draw(topTile, position.x, topY, tileWorldWidth, tileWorldWidth / topTileAspectRatio);
    }
}
