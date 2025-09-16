package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * A render component for ground floors that extend to the bottom of the screen.
 * Similar to TiledFloorComponent but without bottom edges.
 */
public class TiledGroundFloorComponent extends RenderComponent {
    private final TextureRegion topLeftCorner;
    private final TextureRegion topRightCorner;
    private final TextureRegion topEdge;
    private final TextureRegion leftEdge;
    private final TextureRegion rightEdge;
    private final TextureRegion middleTile;
    private final int tileWidth;
    private final int tileHeight;

    public TiledGroundFloorComponent(TextureRegion topLeftCorner, TextureRegion topRightCorner,
                                     TextureRegion topEdge, TextureRegion leftEdge,
                                     TextureRegion rightEdge, TextureRegion middleTile) {
        this.topLeftCorner = topLeftCorner;
        this.topRightCorner = topRightCorner;
        this.topEdge = topEdge;
        this.leftEdge = leftEdge;
        this.rightEdge = rightEdge;
        this.middleTile = middleTile;

        this.tileWidth = topLeftCorner.getRegionWidth();
        this.tileHeight = topLeftCorner.getRegionHeight();
    }

    @Override
    protected void draw(SpriteBatch batch) {
        final Vector2 position = entity.getPosition();
        final Vector2 scale = entity.getScale();

        final float tileWorldWidth = tileWidth / 64f;
        final float tileWorldHeight = tileHeight / 64f;

        final int tilesX = Math.max(1, MathUtils.ceil(scale.x / tileWorldWidth));
        final int tilesY = Math.max(1, MathUtils.ceil(scale.y / tileWorldHeight));

        final float actualTileWidth = scale.x / tilesX;
        final float actualTileHeight = scale.y / tilesY;

        // Handle single tile case
        if (tilesX == 1 && tilesY == 1) {
            batch.draw(middleTile, position.x, position.y, scale.x, scale.y);
            return;
        }

        // Draw the tiled ground floor
        for (int y = 0; y < tilesY; y++) {
            for (int x = 0; x < tilesX; x++) {
                float drawX = position.x + x * actualTileWidth;
                float drawY = position.y + y * actualTileHeight;

                TextureRegion tileToDraw = getTileForPosition(x, y, tilesX, tilesY);
                batch.draw(tileToDraw, drawX, drawY, actualTileWidth, actualTileHeight);
            }
        }
    }

    private TextureRegion getTileForPosition(int x, int y, int maxX, int maxY) {
        boolean isTopRow = (y == maxY - 1);
        boolean isBottomRow = (y == 0);
        boolean isLeftColumn = (x == 0);
        boolean isRightColumn = (x == maxX - 1);

        // Handle single row case
        if (maxY == 1) {
            if (isLeftColumn && isRightColumn) return middleTile;
            if (isLeftColumn) return leftEdge;
            if (isRightColumn) return rightEdge;
            return middleTile;
        }

        // Handle single column case
        if (maxX == 1) {
            if (isTopRow) return topEdge;
            return middleTile; // No bottom edge for ground floors
        }

        // Handle corners - only top corners
        if (isTopRow && isLeftColumn) return topLeftCorner;
        if (isTopRow && isRightColumn) return topRightCorner;

        // Handle edges
        if (isTopRow) return topEdge;
        if (isLeftColumn) return leftEdge;
        if (isRightColumn) return rightEdge;

        // Everything else is middle
        return middleTile;
    }
}