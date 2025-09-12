package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.listeners.EventListener0;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class ChaseTaskTest {
    private AtomicLong nowMs;

    @BeforeEach
    void beforeEach() {
        // Mock rendering, physics, and game time
        RenderService renderService = new RenderService();
        renderService.setDebug(mock(DebugRenderer.class));
        ServiceLocator.registerRenderService(renderService);

        GameTime gameTime = mock(GameTime.class);
        nowMs = new AtomicLong(0L);
        when(gameTime.getDeltaTime()).thenReturn(20f / 1000);
        when(gameTime.getTime()).thenAnswer(inv -> nowMs.get());
        ServiceLocator.registerTimeSource(gameTime);

        ServiceLocator.registerPhysicsService(new PhysicsService());
    }

    @Test
    void chase_priorityReflectsActivation() {
        Entity target = createTarget(new Vector2(0, 0));
        Entity e = makePhysicsEntity(new Vector2(10, 10));
        AITaskComponent ai = new AITaskComponent();
        ChaseTask chase = new ChaseTask(target, 10f);
        ai.addTask(chase);
        e.addComponent(ai);
        e.create();

        assertEquals(-1, chase.getPriority(),
                "Chase has default -1 priority (inactive)");

        chase.activate();
        assertEquals(10, chase.getPriority(),
                "Activating chase makes priority 10");

        chase.deactivate();
        assertEquals(-1, chase.getPriority(),
                "Deactivating chase makes priority -1");
    }

    @Test
    void chase_start_activeFiresEvent() {
        Entity target = createTarget(new Vector2(0, 0));
        Entity e = makePhysicsEntity(new Vector2(10, 10));
        AITaskComponent ai = new AITaskComponent();
        ChaseTask chase = new ChaseTask(target, 10f);
        ai.addTask(chase);
        e.addComponent(ai);
        e.create();

        EventListener0 callback = mock(EventListener0.class);
        e.getEvents().addListener("chaseStart", callback);

        chase.activate();
        chase.start();
        verify(callback, times(1)).handle();
    }

    @Test
    void chase_start_inactiveDoesNotFireEvent() {
        Entity target = createTarget(new Vector2(0, 0));
        Entity e = makePhysicsEntity(new Vector2(10, 10));
        AITaskComponent ai = new AITaskComponent();
        ChaseTask chase = new ChaseTask(target, 10f);
        ai.addTask(chase);
        e.addComponent(ai);
        e.create();

        EventListener0 callback = mock(EventListener0.class);
        e.getEvents().addListener("chaseStart", callback);

        chase.start();
        verify(callback, times(0)).handle();
    }

    @Test
    void chase_stopFiresEvent() {
        Entity target = createTarget(new Vector2(0, 0));
        Entity e = makePhysicsEntity(new Vector2(10, 10));
        AITaskComponent ai = new AITaskComponent();
        ChaseTask chase = new ChaseTask(target, 10f);
        ai.addTask(chase);
        e.addComponent(ai);
        e.create();

        EventListener0 callback = mock(EventListener0.class);
        e.getEvents().addListener("chaseEnd", callback);

        chase.activate();
        chase.start();
        chase.stop();
        verify(callback, times(1)).handle();
    }

    @Test
    void chase_shouldMoveTowardsTarget() {
        Entity target = createTarget(new Vector2(0, 0));
        Entity e = makePhysicsEntity(new Vector2(10, 10));
        AITaskComponent ai = new AITaskComponent();
        ChaseTask chase = new ChaseTask(target, 100f);
        ai.addTask(chase);
        e.addComponent(ai);
        e.create();

        chase.activate();
        chase.start();

        float initialDistance = e.getPosition().dst(target.getPosition());

        for (int i = 0; i < 3; i++) {
            e.earlyUpdate();
            e.update();
            ServiceLocator.getPhysicsService().getPhysics().update();
            nowMs.addAndGet(20L);
        }

        float newDistance = e.getPosition().dst(target.getPosition());
        assertTrue(newDistance < initialDistance, "Entity should move closer when chasing");
    }

    @Test
    void chase_endsAfterGracePeriod() {
        Entity target = createTarget(new Vector2(0, 0));
        Entity e = makePhysicsEntity(new Vector2(1000, 1000)); // Far away

        AITaskComponent ai = new AITaskComponent();
        ChaseTask chase = new ChaseTask(target, 1f);
        ai.addTask(chase);
        e.addComponent(ai);
        e.create();

        chase.activate();
        assertEquals(10, chase.getPriority(), "Within grace: end conditions ignored");

        nowMs.addAndGet(2000L);
        assertEquals(10, chase.getPriority(), "Still within grace: keep chasing");

        nowMs.addAndGet(1500L);
        assertEquals(-1, chase.getPriority(), "After grace: Too far, ends chase");
    }

    private Entity createTarget(Vector2 pos) {
        Entity e = new Entity();
        e.setPosition(pos);
        e.create();
        return e;
    }

    private Entity makePhysicsEntity(Vector2 pos) {
        Entity e = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new PhysicsMovementComponent());
        e.setPosition(pos);
        return e;
    }
}
