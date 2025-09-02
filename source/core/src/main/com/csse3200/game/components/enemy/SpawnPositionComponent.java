package com.csse3200.game.components.enemy;


import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;

public class SpawnPositionComponent extends Component {
    private final Vector2 spawnPos;

    public SpawnPositionComponent(Vector2 spawnPos) {
        this.spawnPos = spawnPos.cpy(); // store a copy
    }

    public Vector2 getSpawnPos() {
        return spawnPos;
    }
}
