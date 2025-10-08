package com.csse3200.game.components.projectiles;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
public class BombComponentTest {
    @BeforeEach
    void setup() {
        ServiceLocator.clear();
        ServiceLocator.registerTimeSource(new GameTime());
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerPhysicsService(new PhysicsService());
    }
    private Entity makeBombEntity(float explosionDelay, float explosionRadius, short targetLayer) {
        Entity e = new Entity();
        e.addComponent(new BombComponent(explosionDelay, explosionRadius, targetLayer));
        e.create();
        return e;
    }

    @Test
    void doesNotExplodeImmediately() {
        Entity e = makeBombEntity(2.0f, 2.5f, (short) 0);
        BombComponent bomb = e.getComponent(BombComponent.class);
        assertNotNull(bomb);

        bomb.update();

        assertFalse(bomb.hasExploded(), "Bomb should not explode before delay.");
        assertNotEquals(0f, e.getScale().x, 1e-6, "Bomb should remain visible right after create");
        assertNotEquals(0f, e.getScale().y, 1e-6, "Bomb should remain visible right after create");
    }

    @Test
    void explodesAndNotVisible() {
        Entity e = makeBombEntity(0f, 2.5f, (short) 0); // zero delay
        BombComponent bomb = e.getComponent(BombComponent.class);
        bomb.update();

        assertTrue(bomb.hasExploded(), "Bomb should mark exploded immediately when delay is zero");
        assertEquals(0f, e.getScale().x, 1e-6, "Bomb should hide immediately on explode()");
        assertEquals(0f, e.getScale().y, 1e-6, "Bomb should hide immediately on explode()");
    }

    @Test
    void disposesNextUpdate() {
        Entity e = makeBombEntity(0f, 2.5f, (short) 0);
        BombComponent bomb = e.getComponent(BombComponent.class);

        List<String> eventLog = new ArrayList<>();
        e.getEvents().addListener("bomb:disposeRequested", () -> eventLog.add("bomb:disposeRequested"));

        // First update: explode immediately
        bomb.update();

        assertTrue(bomb.hasExploded(), "Bomb should explode on first update");
        assertEquals(1, eventLog.size(), "Exactly one dispose request event should fire");
        assertEquals("bomb:disposeRequested", eventLog.getFirst());

        // Component should be disabled after explode()
        String desc = bomb.toString();
        assertTrue(desc.contains("enabled=false"),
                "Component should be disabled after exploding; got: " + desc);

        // Check no more events fired
        bomb.update();
        assertEquals(1, eventLog.size(), "No more events fired after explode");
    }
}