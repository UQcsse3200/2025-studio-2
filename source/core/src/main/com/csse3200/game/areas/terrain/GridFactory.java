package com.csse3200.game.areas.terrain;

import com.badlogic.gdx.math.GridPoint2;

public class GridFactory {
    private static final GridPoint2 DEFAULT_MAP_SIZE = new GridPoint2(80, 70);

    private final GridComponent.GridOrientation orientation;

    /**
     * Create a grid factory with Orthogonal orientation
     */
    public GridFactory() {
        this(GridComponent.GridOrientation.ORTHOGONAL);
    }

    /**
     * Create a grid factory
     * @param orientation orientation for the grid system
     */
    public GridFactory(GridComponent.GridOrientation orientation) {
        this.orientation = orientation;
    }

    /**
     * Create a grid with default map size
     * @param tileSize Size of each tile in world units
     * @return GridComponent for spatial calculations
     */
    public GridComponent createGrid(float tileSize) {
        return createGrid(DEFAULT_MAP_SIZE, tileSize);
    }

    /**
     * Create a grid with specified dimensions
     * @param mapSize Dimensions of the grid in tiles
     * @param tileSize Size of each tile in world units
     * @return GridComponent for spatial calculations
     */
    public GridComponent createGrid(GridPoint2 mapSize, float tileSize) {
        return new GridComponent(mapSize, orientation, tileSize);
    }
}
