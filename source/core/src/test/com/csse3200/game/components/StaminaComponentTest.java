package com.csse3200.game.components;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for StaminaComponent (no LibGDX needed). */
class StaminaComponentTest {
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
        s.update(0.5f);               // +5 (10/s * 0.5s)
        assertEquals(80, s.getCurrentStamina());
    }

    @Test
    void clampsAtZeroAndExhaustsThenRecovers() {
        StaminaComponent s = new StaminaComponent(50f, 10f, 200f, 20, 0.0f);
        Entity e = new Entity();
        final boolean[] exhausted = {false};
        final boolean[] recovered = {false};
        e.getEvents().addListener("exhausted", () -> exhausted[0] = true);
        e.getEvents().addListener("recovered", () -> recovered[0] = true);
        e.addComponent(s).create();

        s.setSprinting(true);
        s.update(1f);                 // would go negative -> clamps to 0, fires exhausted
        assertEquals(0, s.getCurrentStamina());
        assertTrue(s.isExhausted());
        assertTrue(exhausted[0]);

        s.setSprinting(false);
        s.update(1f);                 // regen above 0, fires recovered
        assertTrue(s.getCurrentStamina() > 0);
        assertFalse(s.isExhausted());
        assertTrue(recovered[0]);
    }

    @Test
    void staminaUpdateEventOnlyWhenIntegerChanges() {
        StaminaComponent s = newComp();
        Entity e = new Entity();
        final int[] updates = {0};
        e.getEvents().addListener("staminaUpdate", (Integer cur, Integer max) -> updates[0]++);
        e.addComponent(s).create();

        // First tiny drain crosses the integer boundary (100 -> 99.875),
        // component uses TRUNCATION for event check, so an event SHOULD fire.
        s.setSprinting(true);
        s.update(0.005f);                   // drain 0.125
        assertTrue(updates[0] >= 1);        // <-- changed: expect at least one event
        assertEquals(100, s.getCurrentStamina()); // UI still rounds to 100

        // More drain crosses more integers -> more events
        int prev = updates[0];
        s.update(0.5f);                     // 12.5 more drain (-> 87.5)
        assertTrue(updates[0] > prev);
    }

    @Test
    void tryConsumeForAttackSucceedsAndSetsDelay() {
        StaminaComponent s = newComp();
        Entity e = new Entity();
        e.addComponent(s).create();

        assertEquals(100, s.getCurrentStamina());
        assertTrue(s.tryConsumeForAttack());         // cost=20
        assertEquals(80, s.getCurrentStamina());

        // Drain to below cost; the next attack should fail
        s.setSprinting(true);
        s.update(3.5f);                               // 25*3.5=87.5; 80-87.5 -> 0
        s.setSprinting(false);
        assertFalse(s.tryConsumeForAttack());
        assertEquals(0, s.getCurrentStamina());
    }

    @Test
    void fromConfigReflectionPicksUpValues() {
        @SuppressWarnings("unused")
        class DummyCfg {
            public float maxStamina = 120f;
            public float staminaRegenPerSecond = 12f;
            public float sprintDrainPerSecond = 30f;
            public int   attackStaminaCost = 15;
            public float regenDelaySeconds = 0.75f;
        }
        StaminaComponent s = StaminaComponent.fromConfig(new DummyCfg());
        assertEquals(120, s.getMaxStamina());
        assertEquals(120, s.getCurrentStamina());
        s.setSprinting(true);
        s.update(1f);
        assertEquals(90, s.getCurrentStamina());     // 120 - 30
    }
}
