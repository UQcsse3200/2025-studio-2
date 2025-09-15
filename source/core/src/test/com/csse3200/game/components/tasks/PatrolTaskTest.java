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
    private GameTime gameTime;

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();
        ServiceLocator.registerPhysicsService(new PhysicsService());
        gameTime = mock(GameTime.class);
        when(gameTime.getDeltaTime()).thenReturn(0.02f);
        ServiceLocator.registerTimeSource(gameTime);
    }

    @Test
    void patrol_startsWithWait() {
        // Set up patrol
        Vector2[] route = {new Vector2(0, 0), new Vector2(1, 0)};
        Entity e = makePatrollingEntity(route);
        PatrolTask patrol = addPatrol(e, 0f);
        e.create();
        Vector2[] waypoints = getWaypoints(e);

        patrol.start();

        // Assert that entity is waiting at first waypoint when a patrol starts
        assertTrue(patrol.isWaiting(),
                "Patrol should start with a wait");
        assertEquals(0, patrol.getIndex(),
                "Index should start at 0");
        assertEquals(waypoints[0], patrol.getTargetWaypoint(),
                "Target waypoint should be be the waypoint at index 0");
    }

    @Test
    void patrol_swapsToMoveAfterFirstWait() {
        // Set up patrol
        Vector2[] route = {new Vector2(0, 0), new Vector2(1, 0)};
        Entity e = makePatrollingEntity(route);
        PatrolTask patrol = addPatrol(e, 0f);
        e.create();
        Vector2[] waypoints = getWaypoints(e);

        // Start the patrol and finish waiting
        patrol.start();
        patrol.update();

        assertTrue(patrol.isWaiting(),
                "Current subtask should be waiting but finished");

        // Transition to movement in next update
        patrol.update();

        // Assert that entity is now moving toward waypoint[1]
        assertTrue(patrol.isMoving(),
                "Should be moving after initial wait ends");
        assertEquals(1, patrol.getIndex(),
                "Index should be at 1");
        assertEquals(waypoints[1], patrol.getTargetWaypoint(),
                "Target waypoint should be the waypoint at index 1");
    }

    @Test
    void patrol_pingPongsAtEnds() {
        // Set up patrol
        Vector2[] route = {new Vector2(0, 0), new Vector2(1, 0), new Vector2(2, 0)};
        Entity e = makePatrollingEntity(route);
        PatrolTask patrol = addPatrol(e, 0f);
        e.create();

        // Start patrol and update to first movement task
        patrol.start();
        patrol.update();
        patrol.update();
        assertTrue(patrol.isMoving(),
                "Should be moving after first two updates");
        assertEquals(1, patrol.getIndex(),
                "Index should be 1");

        // Store the walked waypoints as indexes and direction of movement
        int[] walked = new int[6];
        boolean[] directions = new boolean[6];

        int i = 0;
        walked[i] = patrol.getIndex();
        directions[i] = patrol.isMovingForward();
        i++;

        while (i < walked.length) {
            e.setPosition(patrol.getTargetWaypoint());
            patrol.update(); // Finish movement
            patrol.update(); // Swap to wait, finishes immediately (wait=0)
            patrol.update(); // Swap to movement
            walked[i] = patrol.getIndex();
            directions[i] = patrol.isMovingForward();
            i++;
        }

        assertArrayEquals(new int[]{1, 2, 1, 0, 1, 2}, walked,
                "Should ping-pong indices across ends");
        assertArrayEquals(new boolean[]{true, true, false, false, true, true}, directions,
                "Direction should flip at first/last waypoints");
    }

    @Test
    void patrol_twoWaypointPingPong() {
        // Set up patrol
        Vector2[] route = {new Vector2(0, 0), new Vector2(1, 0)};
        Entity e = makePatrollingEntity(route);
        PatrolTask patrol = addPatrol(e, 0f);
        e.create();

        // Start patrol and update to first movement task
        patrol.start();
        patrol.update();
        patrol.update();
        assertTrue(patrol.isMoving(),
                "Should be moving after first two updates");
        assertEquals(1, patrol.getIndex(),
                "Index should be 1");

        // Store the walked waypoints as indexes and direction of movement
        int[] walked = new int[6];
        boolean[] directions = new boolean[6];

        int i = 0;
        walked[i] = patrol.getIndex();
        directions[i] = patrol.isMovingForward();
        i++;

        while (i < walked.length) {
            e.setPosition(patrol.getTargetWaypoint());
            patrol.update(); // Finish movement
            patrol.update(); // Swap to wait, finishes immediately (wait=0)
            patrol.update(); // Swap to movement
            walked[i] = patrol.getIndex();
            directions[i] = patrol.isMovingForward();
            i++;
        }

        assertArrayEquals(new int[]{1, 0, 1, 0, 1, 0}, walked,
                "Should ping-pong indices across ends");
        assertArrayEquals(new boolean[]{true, false, true, false, true, false}, directions,
                "Direction should flip at first/last waypoints");
    }

    @Test
    void patrol_handlesSecondStart() {
        // Set up patrol
        Vector2[] route = {new Vector2(0, 0), new Vector2(1, 0)};
        Entity e = makePatrollingEntity(route);
        PatrolTask patrol = addPatrol(e, 0f);
        e.create();

        // Start patrol
        patrol.start();  // Waiting
        patrol.update(); // Wait finishes
        patrol.update(); // Swap to movement
        assertTrue(patrol.isMoving(),
                "Should be moving after two updates");
        assertEquals(1, patrol.getIndex(),
                "Index should be 1 before restart");

        // Start patrol again, checking state has been reset
        patrol.start();
        assertTrue(patrol.isWaiting(),
                "Second start should reset patrol to waiting");
        assertEquals(0, patrol.getIndex(),
                "Target index should be 0 after second start");
    }

    @Test
    void patrol_handlesSingleWaypoint() {
        // Set up patrol with single waypoint
        Vector2[] route = {new Vector2(0, 0)};
        Entity e = makePatrollingEntity(route);
        PatrolTask patrol = addPatrol(e, 0);
        e.create();

        patrol.start();
        patrol.update(); // Finish wait

        for (int k = 0; k < 5; k++) {
            patrol.update(); // Swap to movement, finishes (single waypoint)
            assertTrue(patrol.isMoving(), "Current task is moving");
            assertEquals(0, patrol.getIndex(), "Index 0 for single waypoint");
            assertEquals(new Vector2(0, 0), patrol.getTargetWaypoint(), "Always targets first waypoint");

            patrol.update(); // Swap to wait, finishes (wait = 0)
            assertTrue(patrol.isWaiting(), "Current task is waiting");
        }
    }

    @Test
    void patrol_startFiresEvent() {
        // Set up patrol
        Vector2[] route = {new Vector2(0, 0), new Vector2(1, 0)};
        Entity e = makePatrollingEntity(route);
        PatrolTask patrol = addPatrol(e, 0f);
        e.create();

        EventListener0 callback = mock(EventListener0.class);
        e.getEvents().addListener("patrolStart", callback);

        patrol.start();
        verify(callback, times(1)).handle();
    }

    @Test
    void patrol_stopMakesTaskInactive() {
        // Set up patrol
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
    void patrol_stopFiresEvent() {
        // Set up patrol
        Vector2[] route = {new Vector2(0, 0), new Vector2(1, 0)};
        Entity e = makePatrollingEntity(route);
        PatrolTask patrol = addPatrol(e, 0f);
        e.create();

        EventListener0 callback = mock(EventListener0.class);
        e.getEvents().addListener("patrolEnd", callback);

        // Verify patrolEnd is fired once
        patrol.start();
        patrol.update();
        patrol.stop();
        verify(callback, times(1)).handle();
    }

    @Test
    void patrol_hasLowPriority() {
        PatrolTask patrol = new PatrolTask(0);
        assertEquals(1, patrol.getPriority(),
                "PatrolTask should have priority 1");
    }

    @Test
    void patrol_noUpdateIfStopped() {
        // Set up patrol
        Vector2[] route = {new Vector2(0, 0), new Vector2(1, 0)};
        Entity e = makePatrollingEntity(route);
        PatrolTask patrol = addPatrol(e, 0);
        e.create();
        patrol.start();

        int before = patrol.getIndex();
        patrol.stop();
        patrol.update();
        assertEquals(before, patrol.getIndex(), "Patrol should not update after stop");
    }

    @Test
    void patrol_handlesNonZeroWait() {
        // Set up patrol with nonzero wait
        Vector2[] route = new Vector2[]{new Vector2(0, 0), new Vector2(5, 0)};
        Entity e = makePatrollingEntity(route);
        PatrolTask patrol = addPatrol(e, 0.4f);
        e.create();

        // Time sequence for update calls
        when(gameTime.getTime()).thenReturn(0L, 200L, 400L, 600L);

        patrol.start();
        patrol.update(); // Still waiting
        assertTrue(patrol.isWaiting(), "Waiting before full wait time has passed");
        assertEquals(0, patrol.getIndex());

        patrol.update(); // Finish wait
        assertTrue(patrol.isWaiting(), "Current task is waiting (status finished)");

        patrol.update(); // Swap to movement
        assertTrue(patrol.isMoving(), "Moving after wait task finishes");
        assertEquals(1, patrol.getIndex());
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