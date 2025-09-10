package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.ai.tasks.Task;
import com.csse3200.game.components.enemy.PatrolRouteComponent;
import com.csse3200.game.components.enemy.SpawnPositionComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CooldownTaskTest {
    private Entity entity;
    private PatrolRouteComponent patrolRoute;
    private GameTime gameTime;

    @BeforeEach
    void setUp() {
        gameTime = mock(GameTime.class);
        ServiceLocator.registerTimeSource(gameTime);
        when(gameTime.getDeltaTime()).thenReturn(0.1f); // simulate 0.1s per update

        // --- Setup entity and components ---
        entity = new Entity();
        Vector2[] waypoints = {new Vector2(5f, 5f), new Vector2(10f, 10f)};
        patrolRoute = new PatrolRouteComponent(waypoints);
        entity.addComponent(patrolRoute);

        AITaskComponent ai = new AITaskComponent();
        entity.addComponent(ai);

        entity.create(); // ensures AI tasks get owners correctly
    }

    @Test
    void cooldown_activeStartEventTriggered() {
        AITaskComponent ai = entity.getComponent(AITaskComponent.class);
        CooldownTask cooldownTask = new CooldownTask(0.2f);
        ai.addTask(cooldownTask);

        final boolean[] triggered = {false};
        entity.getEvents().addListener("cooldownStart", () -> triggered[0] = true);

        cooldownTask.activate();
        cooldownTask.start();
        assertTrue(triggered[0], "cooldownStart should have been triggered");
        assertEquals(Task.Status.ACTIVE, cooldownTask.getStatus());
    }

    @Test
    void cooldown_inactiveStartNoEventTriggered() {
        AITaskComponent ai = entity.getComponent(AITaskComponent.class);
        CooldownTask cooldownTask = new CooldownTask(0.2f);
        ai.addTask(cooldownTask);

        final boolean[] triggered = {false};
        entity.getEvents().addListener("cooldownStart", () -> triggered[0] = true);

        cooldownTask.start();
        assertFalse(triggered[0], "cooldownStart should not have been triggered while inactive");
    }

    @Test
    void cooldown_teleportAfterWait() {
        AITaskComponent ai = entity.getComponent(AITaskComponent.class);
        CooldownTask cooldownTask = new CooldownTask(0.1f);
        ai.addTask(cooldownTask);

        entity.setPosition(new Vector2(20f, 20f));

        // Time sequence for update calls (Wait task uses GameTime.getTime)
        when(gameTime.getTime()).thenReturn(0L, 100L);
        cooldownTask.activate();
        cooldownTask.start(); // Start waiting
        cooldownTask.update(); // Finishes wait, resets position

        Vector2 expected = patrolRoute.patrolStart();
        Vector2 actual = entity.getPosition();

        assertEquals(expected.x, actual.x, 0.001f);
        assertEquals(expected.y, actual.y, 0.001f);
        assertEquals(-1, cooldownTask.getPriority(),
                "Cooldown should deactivate after teleport");
    }

    @Test
    void cooldown_endEventTriggered() {
        AITaskComponent ai = entity.getComponent(AITaskComponent.class);
        CooldownTask cooldownTask = new CooldownTask(0.1f);
        ai.addTask(cooldownTask);

        final boolean[] triggered = {false};
        entity.getEvents().addListener("cooldownEnd", () -> triggered[0] = true);

        cooldownTask.activate();
        cooldownTask.start();
        cooldownTask.stop(); // Cooldown stopped

        assertTrue(triggered[0], "cooldownEnd should have been triggered");
        assertEquals(Task.Status.INACTIVE, cooldownTask.getStatus());
    }

    @Test
    void cooldown_testIncrementalUpdate() {
        AITaskComponent ai = entity.getComponent(AITaskComponent.class);
        CooldownTask cooldownTask = new CooldownTask(0.3f);
        ai.addTask(cooldownTask);

        entity.setPosition(new Vector2(50f, 50f));

        when(gameTime.getTime()).thenReturn(0L, 100L, 200L, 300L);
        cooldownTask.activate();
        cooldownTask.start();

        // Call update multiple times
        cooldownTask.update();
        assertEquals(Task.Status.ACTIVE, cooldownTask.getStatus());

        cooldownTask.update();
        assertEquals(Task.Status.ACTIVE, cooldownTask.getStatus());

        cooldownTask.update(); // Teleport here

        assertEquals(patrolRoute.patrolStart(), entity.getPosition());
        assertEquals(-1, cooldownTask.getPriority(),
                "Cooldown should deactivate after teleport");
    }

    @Test
    void cooldown_priorityReflectsActivation() {
        AITaskComponent ai = entity.getComponent(AITaskComponent.class);
        CooldownTask cd = new CooldownTask(0.1f);
        ai.addTask(cd);

        assertEquals(-1, cd.getPriority());

        cd.activate();
        assertEquals(5, cd.getPriority());

        cd.deactivate();
        assertEquals(-1, cd.getPriority());
    }


    @Test
    void cooldown_teleportsToSpawn_whenNoPatrol() {
        Vector2 spawn = new Vector2(2, 2);
        Entity e = new Entity()
                .addComponent(new SpawnPositionComponent(spawn))
                .addComponent(new AITaskComponent());
        e.create();

        CooldownTask cd = new CooldownTask(0.1f);
        e.getComponent(AITaskComponent.class).addTask(cd);
        e.setPosition(new Vector2(5, 5));

        when(gameTime.getTime()).thenReturn(0L, 100L);
        cd.activate();
        cd.start();
        cd.update();
        assertEquals(spawn, e.getPosition());
    }


}
