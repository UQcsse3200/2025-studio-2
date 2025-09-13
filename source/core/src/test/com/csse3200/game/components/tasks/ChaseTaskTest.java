package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.listeners.EventListener0;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class ChaseTaskTest {
    GameTime gameTime;

    @BeforeEach
    void beforeEach() {
        RenderService renderService = new RenderService();
        renderService.setDebug(mock(DebugRenderer.class));
        ServiceLocator.registerRenderService(renderService);

        gameTime = mock(GameTime.class);
        when(gameTime.getDeltaTime()).thenReturn(20f / 1000);
        when(gameTime.getTime()).thenReturn(0L);
        ServiceLocator.registerTimeSource(gameTime);

        ServiceLocator.registerPhysicsService(new PhysicsService());
    }

    @Test
    void chase_priorityReflectsActivation() {
        Entity target = createTarget(new Vector2(0, 0));
        Entity e = makePhysicsEntity(new Vector2(10, 10));

        AITaskComponent ai = new AITaskComponent();
        ChaseTask chase = new ChaseTask(target, 10f, 3f);
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
        ChaseTask chase = new ChaseTask(target, 10f, 3f);
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
        ChaseTask chase = new ChaseTask(target, 10f, 3f);
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
        ChaseTask chase = new ChaseTask(target, 10f, 3f);
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
        ChaseTask chase = new ChaseTask(target, 100f, 3f);
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
        }

        float newDistance = e.getPosition().dst(target.getPosition());
        assertTrue(newDistance < initialDistance, "Entity should move closer when chasing");
    }

    @Test
    void chase_endsAfterGrace() {
        Entity target = createTarget(new Vector2(0, 0));
        Entity e = makePhysicsEntity(new Vector2(1000, 1000)); // Far away

        AITaskComponent ai = new AITaskComponent();
        ChaseTask chase = new ChaseTask(target, 1f, 3f);
        ai.addTask(chase);
        e.addComponent(ai);
        e.create();

        chase.activate();
        assertEquals(10, chase.getPriority(), "Within grace: end conditions ignored");

        when(gameTime.getTime()).thenReturn(2000L);
        assertEquals(10, chase.getPriority(), "Still within grace: keep chasing");

        when(gameTime.getTime()).thenReturn(3500L);
        assertEquals(-1, chase.getPriority(), "After grace: Too far, ends chase");
    }

    @Test
    void chase_endsAfterLos() {
        // Mock physics service for this test
        PhysicsEngine physics = mock(PhysicsEngine.class);
        PhysicsService physicsService = mock(PhysicsService.class);
        when(physicsService.getPhysics()).thenReturn(physics);
        ServiceLocator.registerPhysicsService(physicsService);

        Entity target = createTarget(new Vector2(0, 0));
        Entity e = new Entity();
        e.setPosition(new Vector2(5, 5));

        AITaskComponent ai = new AITaskComponent();
        ChaseTask chase = new ChaseTask(target, 50f, 3f);
        ai.addTask(chase);
        e.addComponent(ai);
        e.create();

        // Activate
        chase.activate();
        when(gameTime.getTime()).thenReturn(3100L); // Finish activation grace period

        // LOS Clear -> Keeps chasing
        when(physics.raycast(any(), any(), eq(PhysicsLayer.OBSTACLE), any())).thenReturn(false);
        assertEquals(10, chase.getPriority(), "Should chase while LOS clear");

        // LOS blocked briefly (<250ms grace window) -> Keep chasing
        when(gameTime.getTime()).thenReturn(3200L);
        when(physics.raycast(any(), any(), eq(PhysicsLayer.OBSTACLE), any())).thenReturn(true);
        assertEquals(10, chase.getPriority(), "LOS lost < 250ms -> Keep chasing");

        // Check boundary (250L) -> Still chasing
        when(gameTime.getTime()).thenReturn(3350L);
        assertEquals(10, chase.getPriority(), "Still chase at boundary (250ms since lastVisible");

        // LOS blocked long enough -> End chase
        when(gameTime.getTime()).thenReturn(3351L); // > 250ms since last visible
        assertEquals(-1, chase.getPriority(), "LOS lost > 250ms -> Stop chasing");
    }

    @Test
    void chase_endsAfterMaxDistance() {
        Entity target = createTarget(new Vector2(0, 0));
        Entity e = makePhysicsEntity(new Vector2(10, 0)); // 10 away

        AITaskComponent ai = new AITaskComponent();
        ChaseTask chase = new ChaseTask(target, 10f, 3f);
        ai.addTask(chase);
        e.addComponent(ai);
        e.create();

        chase.activate();
        when(gameTime.getTime()).thenReturn(3100L); // After grace
        assertEquals(10, chase.getPriority(), "==maxChaseDistance, should still chase");

        e.setPosition(10.001f, 0); // dst > 10
        assertEquals(-1, chase.getPriority(), "> max distance ends chase");

    }

    @Test
    void chase_continueIfNeverSeen() {
        PhysicsEngine physics = mock(PhysicsEngine.class);
        PhysicsService ps = mock(PhysicsService.class);
        when(ps.getPhysics()).thenReturn(physics);
        ServiceLocator.registerPhysicsService(ps);
        when(physics.raycast(any(), any(), eq(PhysicsLayer.OBSTACLE), any())).thenReturn(true);

        Entity target = createTarget(new Vector2(0,0));
        Entity e = new Entity(); e.setPosition(5,5);

        AITaskComponent ai = new AITaskComponent();
        ChaseTask chase = new ChaseTask(target, 50f, 3f);
        ai.addTask(chase);
        e.addComponent(ai);
        e.create();

        chase.activate();
        when(gameTime.getTime()).thenReturn(3100L); // after active grace
        assertEquals(10, chase.getPriority(), "Never seen -> lostLos should remain false");
    }

    @Test
    void chase_reactivateResetsGrace() {
        Entity target = createTarget(new Vector2(0, 0));
        Entity e = makePhysicsEntity(new Vector2(1000, 1000));

        AITaskComponent ai = new AITaskComponent();
        ChaseTask chase = new ChaseTask(target, 1f, 3f);
        ai.addTask(chase);
        e.addComponent(ai);
        e.create();

        // First run ends
        chase.activate();
        when(gameTime.getTime()).thenReturn(3100L);
        assertEquals(-1, chase.getPriority());
        chase.deactivate();

        // Reactivate: Should be active again despite distance
        when(gameTime.getTime()).thenReturn(3200L);
        chase.activate();
        assertEquals(10, chase.getPriority(), "Grace period resets after reactivation");
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
