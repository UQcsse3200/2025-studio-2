package com.csse3200.game.components.enemy;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
public class SpawnPositionComponentTest {

    @Test
    void constructorRejectsEmptySpawnPos() {
        assertThrows(IllegalArgumentException.class,
                () -> new SpawnPositionComponent(null),
                "Empty spawn pos should be rejected");
    }

    @Test
    void constructorCopiesSpawnPos() {
        Vector2 point = new Vector2(0, 0);
        SpawnPositionComponent spc = new SpawnPositionComponent(point);

        point.set(1, 1);

        assertEquals(new Vector2(0, 0), spc.getSpawnPos(),
                "Mutating the original spawnPos must not affect internal state");
    }

    @Test
    void getSpawnPosReturnsCopy() {
        Vector2 point = new Vector2(0, 0);
        SpawnPositionComponent spc = new SpawnPositionComponent(point);

        Vector2 spawnPos = spc.getSpawnPos();
        spawnPos.set(1, 1);

        assertEquals(new Vector2(0, 0), spc.getSpawnPos(),
                "Mutating the returned spawn pos must not affect internal state");
    }
}
