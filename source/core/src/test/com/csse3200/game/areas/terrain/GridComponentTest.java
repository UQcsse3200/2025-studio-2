package com.csse3200.game.areas.terrain;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.GridComponent.GridOrientation;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class GridComponentTest {

    @Test
    void shouldConvertTileToWorldPositionOrthogonal() {
        GridComponent component = makeComponent(GridOrientation.ORTHOGONAL, 3f);

        assertEquals(new Vector2(0f, 0f), component.tileToWorldPosition(new GridPoint2(0, 0)));
        assertEquals(new Vector2(6f, 12f), component.tileToWorldPosition(new GridPoint2(2, 4)));
        assertEquals(new Vector2(-15f, -9f), component.tileToWorldPosition(new GridPoint2(-5, -3)));
    }

    @Test
    void shouldConvertTileToWorldPositionIsometric() {
        GridComponent component = makeComponent(GridOrientation.ISOMETRIC, 3f);

        assertEquals(new Vector2(0f, 0f), component.tileToWorldPosition(new GridPoint2(0, 0)));
        assertEquals(new Vector2(9f, 3f), component.tileToWorldPosition(new GridPoint2(2, 4)));
        assertEquals(new Vector2(-12f, 3f), component.tileToWorldPosition(new GridPoint2(-5, -3)));
    }

    @Test
    void shouldConvertTileToWorldPositionWithIntParams() {
        GridComponent component = makeComponent(GridOrientation.ORTHOGONAL, 2f);

        assertEquals(new Vector2(0f, 0f), component.tileToWorldPosition(0, 0));
        assertEquals(new Vector2(4f, 8f), component.tileToWorldPosition(2, 4));
        assertEquals(new Vector2(-10f, -6f), component.tileToWorldPosition(-5, -3));
    }

    @Test
    void shouldConvertWorldToTilePositionWithFloatParams() {
        GridComponent component = makeComponent(GridOrientation.ORTHOGONAL, 2f);

        assertEquals(new GridPoint2(0, 0), component.worldToTilePosition(0f, 0f));
        assertEquals(new GridPoint2(2, 4), component.worldToTilePosition(4f, 8f));
    }

    @Test
    void shouldValidateTileCoordinates() {
        GridComponent component = makeComponent(GridOrientation.ORTHOGONAL, 1f);

        // Valid coordinates
        assertTrue(component.isValidTile(new GridPoint2(0, 0)));
        assertTrue(component.isValidTile(new GridPoint2(9, 9))); // mapSize is 10x10
        assertTrue(component.isValidTile(5, 5));

        // Invalid coordinates
        assertFalse(component.isValidTile(new GridPoint2(-1, 0)));
        assertFalse(component.isValidTile(new GridPoint2(0, -1)));
        assertFalse(component.isValidTile(new GridPoint2(10, 0))); // >= mapSize.x
        assertFalse(component.isValidTile(new GridPoint2(0, 10))); // >= mapSize.y
        assertFalse(component.isValidTile(-1, 0));
        assertFalse(component.isValidTile(0, -1));
        assertFalse(component.isValidTile(10, 0));
        assertFalse(component.isValidTile(0, 10));
    }

    @Test
    void shouldReturnCorrectTileSize() {
        float expectedTileSize = 2.5f;
        GridComponent component = makeComponent(GridOrientation.ORTHOGONAL, expectedTileSize);

        assertEquals(expectedTileSize, component.getTileSize());
    }

    @Test
    void shouldReturnCorrectMapBounds() {
        GridComponent component = makeComponent(GridOrientation.ORTHOGONAL, 1.0f);

        GridPoint2 bounds = component.getMapBounds();
        assertNotNull(bounds);
        assertEquals(10, bounds.x);
        assertEquals(10, bounds.y);
    }

    @Test
    void shouldReturnCorrectWorldBounds() {
        GridComponent component = makeComponent(GridOrientation.ORTHOGONAL, 2.0f);

        Vector2 worldBounds = component.getWorldBounds();
        assertNotNull(worldBounds);
        // For 10x10 grid with 2.0f tile size: max tile (9,9) -> world (18,18) + tileSize -> (20,20)
        assertEquals(20f, worldBounds.x);
        assertEquals(20f, worldBounds.y);
    }

    @Test
    void shouldReturnTileCenter() {
        GridComponent component = makeComponent(GridOrientation.ORTHOGONAL, 4.0f);

        Vector2 center = component.getTileCenter(new GridPoint2(2, 3));
        // Tile (2,3) -> world (8,12) + half tileSize (2,2) -> center (10,14)
        assertEquals(new Vector2(10f, 14f), center);
    }

    @Test
    void shouldReturnCorrectOrientation() {
        GridComponent orthogonal = makeComponent(GridOrientation.ORTHOGONAL, 1f);
        GridComponent isometric = makeComponent(GridOrientation.ISOMETRIC, 1f);
        GridComponent hexagonal = makeComponent(GridOrientation.HEXAGONAL, 1f);

        assertEquals(GridOrientation.ORTHOGONAL, orthogonal.getOrientation());
        assertEquals(GridOrientation.ISOMETRIC, isometric.getOrientation());
        assertEquals(GridOrientation.HEXAGONAL, hexagonal.getOrientation());
    }

    @Test
    void shouldHandleZeroTileSize() {
        GridComponent component = makeComponent(GridOrientation.ORTHOGONAL, 0f);

        assertEquals(new Vector2(0f, 0f), component.tileToWorldPosition(new GridPoint2(5, 5)));
        assertEquals(0f, component.getTileSize());
    }

    private static GridComponent makeComponent(GridOrientation orientation, float tileSize) {
        GridPoint2 mapSize = new GridPoint2(10, 10);
        return new GridComponent(mapSize, orientation, tileSize);
    }
}