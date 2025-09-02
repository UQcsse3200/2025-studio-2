package com.csse3200.game.components;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for StaminaComponent (no LibGDX needed). */
class StaminaComponentTest {
    private static final float EPS = 1e-4f;

    private StaminaComponent newComp() {
        // max=100, regen=10/s, drain=25/s, attackCost=20, delay=1.0s
        return new StaminaComponent(100f, 10f, 25f, 20, 1.0f);
    }

    @Test
    void startsFull() {
        StaminaComponent s = newComp();
        assertEquals(100, s.getCurrentStamina());
        assertEquals(100, s.getMaxStamina());
        assertFalse(s.isExhausted());
    }

    @Test
    void drainsWhileSprinting() {
        StaminaComponent s = newComp();
        s.setSprinting(true);
        for (int i = 0; i < 10; i++) s.update(0.1f); // 1.0s total
        assertEquals(75, s.getCurrentStamina());     // 100 - 25*1
    }

    @Test
    void noRegenDuringDelay() {
        StaminaComponent s = newComp();
        s.setSprinting(true);
        s.update(1f);                 // -> 75
        s.setSprinting(false);
        s.update(0.9f);               // still within 1.0s delay
        assertEquals(75, s.getCurrentStamina());
    }

    @Test
    void regensAfterDelay() {
        StaminaComponent s = newComp();
        s.setSprinting(true);
        s.update(1f);                 // -> 75
        s.setSprinting(false);
        s.update(1.0f);               // finish delay
        s.update(0.5f);               // 0.5s regen @10/s => +5
        assertEquals(80, s.getCurrentStamina());
    }

    @Test
    void clampsAtZeroAndExhaustsThenRecovers() {
        StaminaComponent s = new StaminaComponent(50f, 10f, 200f, 20, 0.0f);
        // attach to an entity to receive events (so changeStamina can trigger)
        Entity e = new Entity();
        final boolean[] exhausted = {false};
        final boolean[] recovered = {false};
        e.getEvents().addListener("exhausted", () -> exhausted[0] = true);
        e.getEvents().addListener("recovered", () -> recovered[0] = true);
        e.addComponent(s).create();

        s.setSprinting(true);
        s.update(1f);                 // would go negative -> clamps to 0 and fires "exhausted"
        assertEquals(0, s.getCurrentStamina());
        assertTrue(s.isExhausted());
        assertTrue(exhausted[0]);

        s.setSprinting(false);
        s.update(1f);                 // regen -> >0 and fires "recovered"
        assertTrue(s.getCurrentStamina() > 0);
        assertFalse(s.isExhausted());
        assertTrue(recovered[0]);
    }

    @Test
    void staminaUpdateEventOnlyWhenIntegerChanges() {
        StaminaComponent s = newComp();
        Entity e = new Entity();
        final int[] updates = {0};
        e.getEvents().addListener("staminaUpdate", (int cur, int max) -> updates[0]++);
        e.addComponent(s).create();

        // small tick that doesn't cross an integer should *not* fire
        s.setSprinting(true);
        s.update(0.005f);  // drain = 0.125 -> 99.875 (round to 100)
        assertEquals(100, s.getCurrentStamina());
        assertEquals(0, updates[0]);

        // tick enough to cross multiple integers -> should fire
        s.update(0.5f);    // drain 12.5 -> 87.5 -> ints crossed
        assertTrue(updates[0] > 0);
    }

    @Test
    void tryConsumeForAttackSucceedsAndSetsDelay() {
        StaminaComponent s = newComp();
        // attach to capture updates, optional
        Entity e = new Entity();
        e.addComponent(s).create();

        assertEquals(100, s.getCurrentStamina());
        assertTrue(s.tryConsumeForAttack());         // cost=20
        assertEquals(80, s.getCurrentStamina());

        // Drain to below cost and verify it fails
        s.setSprinting(true);
        s.update(3.5f);                               // 25*3.5 = 87.5 => 80-87.5 -> 0
        s.setSprinting(false);
        assertFalse(s.tryConsumeForAttack());
        assertEquals(0, s.getCurrentStamina());
    }

    @Test
    void fromConfigReflectionPicksUpValues() {
        class DummyCfg {
            // Using names from StaminaComponent.fromConfig’s search list
            public float maxStamina = 120f;
            public float staminaRegenPerSecond = 12f;
            public float sprintDrainPerSecond = 30f;
            public int   attackStaminaCost = 15;
            public float regenDelaySeconds = 0.75f;
        }
        StaminaComponent s = StaminaComponent.fromConfig(new DummyCfg());
        assertEquals(120, s.getMaxStamina());
        assertEquals(120, s.getCurrentStamina());
        // quick behaviour check
        s.setSprinting(true);
        s.update(1f);
        assertEquals(90, s.getCurrentStamina());     // 120 - 30
    }
}
