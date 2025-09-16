package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * A render component for dynamically tiling a floor texture with corners, edges, and middle tiles.
 * It handles corner tiles, edge tiles, and repeating middle tiles
 */
public class TiledFloorComponent extends RenderComponent {
    private final TextureRegion topLeftCorner;
    private final TextureRegion topRightCorner;
    private final TextureRegion bottomLeftCorner;
    private final TextureRegion bottomRightCorner;
    private final TextureRegion topEdge;
    private final TextureRegion bottomEdge;
    private final TextureRegion leftEdge;
    private final TextureRegion rightEdge;
    private final TextureRegion middleTile;
    private final int tileWidth;
    private final int tileHeight;

    /**
     * Creates a new TiledFloorComponent.
     */
    public TiledFloorComponent(TextureRegion topLeftCorner, TextureRegion topRightCorner,
                               TextureRegion bottomLeftCorner, TextureRegion bottomRightCorner,
                               TextureRegion topEdge, TextureRegion bottomEdge,
                               TextureRegion leftEdge, TextureRegion rightEdge,
                               TextureRegion middleTile) {
        this.topLeftCorner = topLeftCorner;
        this.topRightCorner = topRightCorner;
        this.bottomLeftCorner = bottomLeftCorner;
        this.bottomRightCorner = bottomRightCorner;
        this.topEdge = topEdge;
        this.bottomEdge = bottomEdge;
        this.leftEdge = leftEdge;
        this.rightEdge = rightEdge;
        this.middleTile = middleTile;

        this.tileWidth = topLeftCorner.getRegionWidth();
        this.tileHeight = topLeftCorner.getRegionHeight();

        // Validate that all tiles have the same dimensions
        validateTileDimensions();
    }

    private void validateTileDimensions() {
        TextureRegion[] allTiles = {
            topLeftCorner, topRightCorner, bottomLeftCorner, bottomRightCorner,
            topEdge, bottomEdge, leftEdge, rightEdge, middleTile
        };

        for (TextureRegion tile : allTiles) {
            assert(tile.getRegionWidth() == tileWidth && tile.getRegionHeight() == tileHeight)
                : "All tiles must have the same dimensions";
        }
    }

    @Override
    protected void draw(SpriteBatch batch) {
        final Vector2 position = entity.getPosition();
        final Vector2 scale = entity.getScale();

        // Convert tile dimensions to world units
        final float tileWorldWidth = tileWidth / 64f;
        final float tileWorldHeight = tileHeight / 64f;

        // Calculate how many tiles we need
        final int tilesX = Math.max(1, MathUtils.ceil(scale.x / tileWorldWidth));
        final int tilesY = Math.max(1, MathUtils.ceil(scale.y / tileWorldHeight));

        // Calculate actual tile size to fit perfectly
        final float actualTileWidth = scale.x / tilesX;
        final float actualTileHeight = scale.y / tilesY;

        // Handle single tile case
        if (tilesX == 1 && tilesY == 1) {
            batch.draw(middleTile, position.x, position.y, scale.x, scale.y);
            return;
        }

        // Draw the tiled floor
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
            if (isTopRow && isBottomRow) return middleTile;
            if (isTopRow) return topEdge;
            if (isBottomRow) return bottomEdge;
            return middleTile;
        }

        // Handle corners
        if (isTopRow && isLeftColumn) return topLeftCorner;
        if (isTopRow && isRightColumn) return topRightCorner;
        if (isBottomRow && isLeftColumn) return bottomLeftCorner;
        if (isBottomRow && isRightColumn) return bottomRightCorner;

        // Handle edges
        if (isTopRow) return topEdge;
        if (isBottomRow) return bottomEdge;
        if (isLeftColumn) return leftEdge;
        if (isRightColumn) return rightEdge;

        // Handle middle
        return middleTile;
    }
}