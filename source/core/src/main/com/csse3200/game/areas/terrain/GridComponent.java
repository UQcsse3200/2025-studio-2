package com.csse3200.game.areas.terrain;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;

public class GridComponent extends Component {
    private final GridPoint2 mapSize;
    private final GridOrientation orientation;
    private final float tileSize;

    public GridComponent(GridPoint2 mapSize, GridOrientation orientation, float tileSize) {
        this.mapSize = mapSize;
        this.orientation = orientation;
        this.tileSize = tileSize;
    }

    /**
     * Convert tile coordinates to world position
     */
    public Vector2 tileToWorldPosition(GridPoint2 tilePos) {
        return tileToWorldPosition(tilePos.x, tilePos.y);
    }

    /**
     * Convert tile coordinates to world position
     */
    public Vector2 tileToWorldPosition(int x, int y) {
        switch (orientation) {
            case HEXAGONAL:
                float hexLength = tileSize / 2;
                float yOffset = (x % 2 == 0) ? 0.5f * tileSize : 0f;
                return new Vector2(x * (tileSize + hexLength) / 2, y + yOffset);
            case ISOMETRIC:
                return new Vector2((x + y) * tileSize / 2, (y - x) * tileSize / 2);
            case ORTHOGONAL:
                return new Vector2(x * tileSize, y * tileSize);
            default:
                return new Vector2(0, 0);
        }
    }

    /**
     * Convert world position to tile coordinates
     */
    public GridPoint2 worldToTilePosition(Vector2 worldPos) {
        return worldToTilePosition(worldPos.x, worldPos.y);
    }

    /**
     * Convert world position to tile coordinates
     */
    public GridPoint2 worldToTilePosition(float x, float y) {
        switch (orientation) {
            case ORTHOGONAL:
                return new GridPoint2((int)(x / tileSize), (int)(y / tileSize));
            case ISOMETRIC:
                float tileX = (x / tileSize + y / tileSize);
                float tileY = (y / tileSize - x / tileSize);
                return new GridPoint2((int)tileX, (int)tileY);
            case HEXAGONAL:
                float hexLength = tileSize / 2;
                float col = x / ((tileSize + hexLength) / 2);
                float yOffset = ((int)col % 2 == 0) ? 0.5f * tileSize : 0f;
                float row = (y - yOffset) / tileSize;
                return new GridPoint2((int)col, (int)row);
            default:
                return new GridPoint2(0, 0);
        }
    }

    /**
     * Check if tile coordinates are within grid bounds
     */
    public boolean isValidTile(GridPoint2 tilePos) {
        return isValidTile(tilePos.x, tilePos.y);
    }

    /**
     * Check if tile coordinates are within grid bounds
     */
    public boolean isValidTile(int x, int y) {
        return x >= 0 && x < mapSize.x && y >= 0 && y < mapSize.y;
    }

    /**
     * Get the size of each tile in world units
     */
    public float getTileSize() {
        return tileSize;
    }

    /**
     * Get the dimensions of the grid in tiles
     */
    public GridPoint2 getMapBounds() {
        return new GridPoint2(mapSize.x, mapSize.y);
    }

    /**
     * Get the total world size of the grid
     */
    public Vector2 getWorldBounds() {
        Vector2 maxTileWorld = tileToWorldPosition(mapSize.x - 1, mapSize.y - 1);
        return new Vector2(maxTileWorld.x + tileSize, maxTileWorld.y + tileSize);
    }

    /**
     * Get the center world position of a tile
     */
    public Vector2 getTileCenter(GridPoint2 tilePos) {
        Vector2 tileWorld = tileToWorldPosition(tilePos);
        return new Vector2(tileWorld.x + tileSize / 2, tileWorld.y + tileSize / 2);
    }

    /**
     * Get the grid orientation
     */
    public GridOrientation getOrientation() {
        return orientation;
    }

    public enum GridOrientation {
        ORTHOGONAL,
        ISOMETRIC,
        HEXAGONAL
    }
}
