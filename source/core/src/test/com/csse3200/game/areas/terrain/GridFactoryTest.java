package com.csse3200.game.areas.terrain;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.GridComponent.GridOrientation;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class GridFactoryTest {

    private GridFactory gridFactory;

    @BeforeEach
    void setUp() {
        gridFactory = new GridFactory();
    }

    @Test
    void shouldCreateDefaultOrthogonalFactory() {
        GridFactory factory = new GridFactory();
        GridComponent grid = factory.createGrid(2.0f);

        assertNotNull(grid);
        assertEquals(GridOrientation.ORTHOGONAL, grid.getOrientation());
        assertEquals(2.0f, grid.getTileSize());

        // Should use default map size (80, 70)
        GridPoint2 bounds = grid.getMapBounds();
        assertEquals(80, bounds.x);
        assertEquals(70, bounds.y);
    }

    @Test
    void shouldCreateFactoryWithSpecificOrientation() {
        GridFactory isometricFactory = new GridFactory(GridOrientation.ISOMETRIC);
        GridFactory hexagonalFactory = new GridFactory(GridOrientation.HEXAGONAL);

        GridComponent isoGrid = isometricFactory.createGrid(1.0f);
        GridComponent hexGrid = hexagonalFactory.createGrid(1.0f);

        assertEquals(GridOrientation.ISOMETRIC, isoGrid.getOrientation());
        assertEquals(GridOrientation.HEXAGONAL, hexGrid.getOrientation());
    }

    @Test
    void shouldCreateGridWithDefaultMapSize() {
        float tileSize = 1.5f;
        GridComponent grid = gridFactory.createGrid(tileSize);

        assertNotNull(grid);
        assertEquals(tileSize, grid.getTileSize());
        assertEquals(GridOrientation.ORTHOGONAL, grid.getOrientation());

        // Should use default map size (80, 70)
        GridPoint2 bounds = grid.getMapBounds();
        assertEquals(80, bounds.x);
        assertEquals(70, bounds.y);
    }

    @Test
    void shouldCreateGridWithSpecificMapSize() {
        GridPoint2 customMapSize = new GridPoint2(20, 15);
        float tileSize = 2.0f;

        GridComponent grid = gridFactory.createGrid(customMapSize, tileSize);

        assertNotNull(grid);
        assertEquals(tileSize, grid.getTileSize());
        assertEquals(GridOrientation.ORTHOGONAL, grid.getOrientation());

        GridPoint2 bounds = grid.getMapBounds();
        assertEquals(20, bounds.x);
        assertEquals(15, bounds.y);
    }

    @Test
    void shouldCreateGridWithZeroMapSize() {
        GridPoint2 zeroMapSize = new GridPoint2(0, 0);
        GridComponent grid = gridFactory.createGrid(zeroMapSize, 1.0f);

        assertNotNull(grid);
        GridPoint2 bounds = grid.getMapBounds();
        assertEquals(0, bounds.x);
        assertEquals(0, bounds.y);
    }

    @Test
    void shouldCreateGridWithZeroTileSize() {
        GridComponent grid = gridFactory.createGrid(new GridPoint2(10, 10), 0f);

        assertNotNull(grid);
        assertEquals(0f, grid.getTileSize());
    }

    @Test
    void shouldCreateGridWithNegativeTileSize() {
        GridComponent grid = gridFactory.createGrid(new GridPoint2(5, 5), -2.0f);

        assertNotNull(grid);
        assertEquals(-2.0f, grid.getTileSize());
    }

    @Test
    void shouldCreateMultipleIndependentGrids() {
        GridPoint2 mapSize1 = new GridPoint2(10, 10);
        GridPoint2 mapSize2 = new GridPoint2(15, 20);

        GridComponent grid1 = gridFactory.createGrid(mapSize1, 1.0f);
        GridComponent grid2 = gridFactory.createGrid(mapSize2, 2.0f);

        assertNotNull(grid1);
        assertNotNull(grid2);
        assertNotSame(grid1, grid2);

        assertEquals(1.0f, grid1.getTileSize());
        assertEquals(2.0f, grid2.getTileSize());

        assertEquals(10, grid1.getMapBounds().x);
        assertEquals(15, grid2.getMapBounds().x);
        assertEquals(10, grid1.getMapBounds().y);
        assertEquals(20, grid2.getMapBounds().y);
    }

    @Test
    void shouldCreateGridWithLargeMapSize() {
        GridPoint2 largeMapSize = new GridPoint2(1000, 800);
        GridComponent grid = gridFactory.createGrid(largeMapSize, 0.1f);

        assertNotNull(grid);
        GridPoint2 bounds = grid.getMapBounds();
        assertEquals(1000, bounds.x);
        assertEquals(800, bounds.y);
    }

    @Test
    void shouldCreateGridWithDecimalTileSize() {
        float tileSize = 1.234f;
        GridComponent grid = gridFactory.createGrid(new GridPoint2(5, 5), tileSize);

        assertNotNull(grid);
        assertEquals(tileSize, grid.getTileSize(), 0.001f);
    }

    @Test
    void shouldCreateDifferentOrientationFactories() {
        GridFactory orthoFactory = new GridFactory(GridOrientation.ORTHOGONAL);
        GridFactory isoFactory = new GridFactory(GridOrientation.ISOMETRIC);
        GridFactory hexFactory = new GridFactory(GridOrientation.HEXAGONAL);

        GridPoint2 mapSize = new GridPoint2(5, 5);
        float tileSize = 1.0f;

        GridComponent orthoGrid = orthoFactory.createGrid(mapSize, tileSize);
        GridComponent isoGrid = isoFactory.createGrid(mapSize, tileSize);
        GridComponent hexGrid = hexFactory.createGrid(mapSize, tileSize);

        assertEquals(GridOrientation.ORTHOGONAL, orthoGrid.getOrientation());
        assertEquals(GridOrientation.ISOMETRIC, isoGrid.getOrientation());
        assertEquals(GridOrientation.HEXAGONAL, hexGrid.getOrientation());

        // All should have same map size and tile size, but different orientations
        assertEquals(mapSize, orthoGrid.getMapBounds());
        assertEquals(mapSize, isoGrid.getMapBounds());
        assertEquals(mapSize, hexGrid.getMapBounds());

        assertEquals(tileSize, orthoGrid.getTileSize());
        assertEquals(tileSize, isoGrid.getTileSize());
        assertEquals(tileSize, hexGrid.getTileSize());
    }
}