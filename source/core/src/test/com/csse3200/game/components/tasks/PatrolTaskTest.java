package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;

import com.csse3200.game.ai.tasks.Task;
import com.csse3200.game.components.enemy.PatrolRouteComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.listeners.EventListener0;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class PatrolTaskTest {
    @BeforeEach
    void setUp() {
        GameTime gameTime = mock(GameTime.class);
        when(gameTime.getDeltaTime()).thenReturn(20f/1000);
        ServiceLocator.registerTimeSource(gameTime);
        ServiceLocator.registerPhysicsService(new PhysicsService());
    }

    @Test
    void startsMovingToFirstWaypoint() {
        Vector2[] route = {new Vector2(0, 0), new Vector2(1, 0)};

        Entity e = makePatrollingEntity(route);
        PatrolTask patrol = addPatrol(e, 0.5f);
        e.create();
        Vector2[] waypoints = getWaypoints(e);

        patrol.start();

        // After start: Index at 0, entity is moving towards waypoints[0]
        assertEquals(0, patrol.getIndex(),
                "Waypoint index should be 0 after PatrolTask starts");
        assertEquals(waypoints[0], patrol.getTargetWaypoint(),
                "Target should be first waypoint");
        assertTrue(patrol.isMoving(),
                "Entity should be moving after PatrolTask starts");
        assertFalse(patrol.isWaiting(),
                "Entity should not be waiting after PatrolTask starts");
    }

    @Test
    void swapsToWaitAfterMovement() {
        Vector2[] route = {new Vector2(0, 0), new Vector2(0.5f, 0), new Vector2(1, 0)};

        Entity e = makePatrollingEntity(route);
        PatrolTask patrol = addPatrol(e, 0);
        e.create();
        Vector2[] waypoints = getWaypoints(e);

        patrol.start();

        assertTrue(patrol.isMoving());
        assertEquals(0, patrol.getIndex());
        assertEquals(waypoints[0], patrol.getTargetWaypoint());

        // Finish movement to wp[0] and start waiting
        e.setPosition(patrol.getTargetWaypoint());
        patrol.update(); // Movement finishes
        patrol.update(); // Starts waiting
        assertTrue(patrol.isWaiting());
        assertEquals(0, patrol.getIndex());
        assertEquals(waypoints[0], patrol.getTargetWaypoint());

        // Finish waiting and advance to next waypoint
        patrol.update();
        assertTrue(patrol.isMoving());
        assertEquals(1, patrol.getIndex());
        assertEquals(waypoints[1], patrol.getTargetWaypoint());
    }

    @Test
    void pingPongAtEnds() {
        Vector2[] route = {new Vector2(0, 0), new Vector2(0.5f, 0), new Vector2(1, 0)};

        Entity e = makePatrollingEntity(route);
        PatrolTask patrol = addPatrol(e, 0);
        e.create();

        patrol.start();

        int[] walked = new int[6];
        boolean[] directions = new boolean[6];
        int i = 0;
        walked[i] = patrol.getIndex();
        directions[i] = patrol.isMovingForward();
        i++;
        while (i < walked.length) {
            e.setPosition(patrol.getTargetWaypoint());
            patrol.update(); // Finish movement
            patrol.update(); // Swap to wait (wait time = 0)
            patrol.update(); // Swap to movement
            walked[i] = patrol.getIndex();
            directions[i] = patrol.isMovingForward();
            i++;
        }

        assertArrayEquals(new int[]{0, 1, 2, 1, 0, 1}, walked,
                "Should ping-pong indices across ends");
        assertArrayEquals(new boolean[]{true, true, true, false, false, true}, directions,
                "Direction should flip at first/last waypoints");
    }

    @Test
    void handlesSingleStepPatrol() {
        Vector2[] route = {new Vector2(0, 0)};

        Entity e = makePatrollingEntity(route);
        PatrolTask patrol = addPatrol(e, 0);
        e.create();

        patrol.start();

        e.setPosition(patrol.getTargetWaypoint());
        patrol.update();
        assertTrue(patrol.isMoving(),
                "Movement task is FINISHED but still the currentTask");

        for (int k = 0; k < 5; k++) {
            patrol.update();
            assertTrue(patrol.isWaiting(),
                    "CurrentTask should be WaitTask");

            patrol.update();
            assertEquals(0, patrol.getIndex(),
                    "Single waypoint: Index stays at zero");
            assertTrue(patrol.isMoving(),
                    "CurrentTask should be MovementTask");
        }
    }

    @Test
    void shouldTriggerEvent() {
        Vector2[] route = {new Vector2(0, 0)};

        Entity e = makePatrollingEntity(route);
        PatrolTask patrol = addPatrol(e, 0);
        e.create();

        EventListener0 callback = mock(EventListener0.class);
        e.getEvents().addListener("patrolStart", callback);

        patrol.start();

        verify(callback).handle();
    }

    @Test
    void stop_makesTaskInactive() {
        Vector2[] route = {new Vector2(0, 0)};

        Entity e = makePatrollingEntity(route);
        PatrolTask patrol = addPatrol(e, 0);
        e.create();

        patrol.start();
        assertEquals(Task.Status.ACTIVE, patrol.getStatus(),
                "PatrolTask should have ACTIVE status after start()");
        patrol.stop();
        assertEquals(Task.Status.INACTIVE, patrol.getStatus(),
                "PatrolTask should have INACTIVE status after stop()");

    }

    @Test
    void hasLowPriority() {
        PatrolTask patrol = new PatrolTask(0);
        assertEquals(1, patrol.getPriority(),
                "PatrolTask should have priority 1");
    }

    private Entity makePatrollingEntity(Vector2[] route) {
        return new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new PhysicsMovementComponent())
                .addComponent(new PatrolRouteComponent(route));
    }

    private PatrolTask addPatrol(Entity e, float wait) {
        AITaskComponent ai = new AITaskComponent();
        PatrolTask patrol = new PatrolTask(wait);
        ai.addTask(patrol);
        e.addComponent(ai);
        return patrol;
    }

    private Vector2[] getWaypoints(Entity e) {
        return e.getComponent(PatrolRouteComponent.class).getWaypoints();
    }
}
