package com.csse3200.game.components.enemy;


import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;

/**
 * Simple component that stores an entity's spawn position in world coordinates.
 * Intended as a fixed reference point, not the entity's live position. SpawnPos cannot be
 * mutated by external callers with defensive copying.
 * */
public class SpawnPositionComponent extends Component {
    private final Vector2 spawnPos;

    /**
     * Create a spawn position component
     * @param spawnPos the spawn position in world coordinates
     */
    public SpawnPositionComponent(Vector2 spawnPos) {
        if (spawnPos == null) {
            throw new IllegalArgumentException("spawnPos must not be null");
        }
        this.spawnPos = spawnPos.cpy(); // store a copy
    }

    /**
     * Returns the spawn position
     * @return a copy of the spawn position in world coordinates
     */
    public Vector2 getSpawnPos() {
        return spawnPos.cpy();
    }
}
