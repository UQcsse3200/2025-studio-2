package com.csse3200.game.components.tasks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.ai.tasks.Task;
import com.csse3200.game.components.enemy.PatrolRouteComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CooldownTaskTest {
    private Entity entity;
    private PatrolRouteComponent patrolRoute;
    private Graphics graphics;

    @BeforeEach
    void setUp() {
        graphics = mock(Graphics.class);
        Gdx.graphics = graphics;
        when(graphics.getDeltaTime()).thenReturn(0.1f); // simulate 0.1s per update

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
    void testCooldownStartEventTriggered() {
        AITaskComponent ai = entity.getComponent(AITaskComponent.class);
        CooldownTask cooldownTask = new CooldownTask(0.2f);
        ai.addTask(cooldownTask);

        final boolean[] triggered = {false};
        entity.getEvents().addListener("cooldownStart", () -> triggered[0] = true);

        cooldownTask.start();
        assertTrue(triggered[0], "cooldownStart should have been triggered");
        assertEquals(Task.Status.ACTIVE, cooldownTask.getStatus());
    }

    @Test
    void testTeleportAfterWait() {
        AITaskComponent ai = entity.getComponent(AITaskComponent.class);
        CooldownTask cooldownTask = new CooldownTask(0.1f);
        ai.addTask(cooldownTask);

        entity.setPosition(new Vector2(20f, 20f));
        cooldownTask.start();

        // Single update uses mocked delta time
        cooldownTask.update();

        Vector2 expected = patrolRoute.patrolStart();
        Vector2 actual = entity.getPosition();

        assertEquals(expected.x, actual.x, 0.001f);
        assertEquals(expected.y, actual.y, 0.001f);
        assertEquals(Task.Status.FINISHED, cooldownTask.getStatus());
    }

    @Test
    void testCooldownEndEventTriggered() {
        AITaskComponent ai = entity.getComponent(AITaskComponent.class);
        CooldownTask cooldownTask = new CooldownTask(0.1f);
        ai.addTask(cooldownTask);

        final boolean[] triggered = {false};
        entity.getEvents().addListener("cooldownEnd", () -> triggered[0] = true);

        cooldownTask.start();
        cooldownTask.update();

        assertTrue(triggered[0], "cooldownEnd should have been triggered");
        assertEquals(Task.Status.FINISHED, cooldownTask.getStatus());
    }

    @Test
    void testIncrementalUpdate() {
        AITaskComponent ai = entity.getComponent(AITaskComponent.class);
        CooldownTask cooldownTask = new CooldownTask(0.3f);
        ai.addTask(cooldownTask);

        entity.setPosition(new Vector2(50f, 50f));
        cooldownTask.start();

        // Call update multiple times (each = 0.1f delta)
        cooldownTask.update(); // +0.1
        assertEquals(Task.Status.ACTIVE, cooldownTask.getStatus());

        cooldownTask.update(); // +0.1 (total 0.2)
        assertEquals(Task.Status.ACTIVE, cooldownTask.getStatus());

        cooldownTask.update(); // +0.1 (total 0.3 = waitTime)
        assertEquals(Task.Status.FINISHED, cooldownTask.getStatus());

        assertEquals(patrolRoute.patrolStart(), entity.getPosition());
    }
}
