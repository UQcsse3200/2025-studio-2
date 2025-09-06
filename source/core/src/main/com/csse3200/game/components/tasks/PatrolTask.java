package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.ai.tasks.Task;
import com.csse3200.game.components.enemy.PatrolRouteComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Task that makes an entity move through a sequence of waypoints, pausing briefly at each.
 * The entity patrols back and forth along its route, reversing direction when it reaches the end,
 * and repeats this indefinitely.
 * Requires the entity to have a PatrolRouteComponent which defines the set of waypoints to follow.
 * Note: This task only controls behaviour during the patrol itself. Resetting the entity back to
 * its starting waypoint (e.g. after switching from chase mode back to patrol) is handled separately
 * by a cooldown task. For full patrol-chase-cooldown cycles, both tasks should be added to the
 * entity's AITaskComponent.
 * */
public class PatrolTask extends DefaultTask implements PriorityTask {
    private static final Logger logger = LoggerFactory.getLogger(PatrolTask.class);
    private final float waitTime;

    private PatrolRouteComponent route;
    private int i = 0;
    private boolean forward = true;

    private MovementTask movementTask;
    private WaitTask waitTask;
    private Task currentTask;

    /**
     * Creates a patrol starting from a fixed start position.
     * @param waitTime specifies how long to wait at each waypoint before moving to the next.
     */
    public PatrolTask(float waitTime) {
        this.waitTime = waitTime;
    }

    /** Return a low priority of 1. */
    @Override
    public int getPriority() {return 1;} // Low priority

    /**
     * Set up subtasks (wait, movement) and begin moving toward first waypoint.
     */
    @Override
    public void start() {
        super.start();
        // Set "start" patrol conditions
        i = 0;
        forward = true;

        if (route == null) route = owner.getEntity().getComponent(PatrolRouteComponent.class);

        if (waitTask == null) {
            waitTask = new WaitTask(waitTime);
            waitTask.create(owner);
        }

        if (movementTask == null) {
            movementTask = new MovementTask(route.patrolStart());
            movementTask.create(owner);
        }

        // Entity waits at first waypoint before moving
        waitTask.start();
        currentTask = waitTask;

        this.owner.getEntity().getEvents().trigger("patrolStart");
    }

    /**
     * Advance the patrol state. Handles swapping between wait and movement tasks
     * based on their status.
     */
    @Override
    public void update() {
        if (getStatus() != Status.ACTIVE) return;
        if (currentTask == null) return;

        if (currentTask.getStatus() != Status.ACTIVE) {
            if (currentTask == movementTask) {
                swapTask(waitTask); // Always wait between waypoints
            } else {
                nextIndex();
                swapTask(movementTask);
                movementTask.setTarget(route.getWaypointAt(i));
            }
        }
        currentTask.update();
    }

    /** Stop the patrol task and its current subtask */
    @Override
    public void stop() {
        if (currentTask != null) {
            currentTask.stop();
        }
        currentTask = null;
        super.stop();

        this.owner.getEntity().getEvents().trigger("patrolStop");
    }

    /**
     * Check if waiting.
     * @return true if currentTask is waitTask
     */
    public boolean isWaiting() {
        return currentTask == waitTask;
    }

    /**
     * Check if moving.
     * @return true if currentTask is movementTask
     */
    public boolean isMoving() {
        return currentTask == movementTask;
    }

    /** Get the current index pointing to a waypoint. */
    public int getIndex() {
        return i;
    }

    /**
     * Check patrol direction.
     * @return true for forward, otherwise false
     */
    public boolean isMovingForward() {
        return forward;
    }

    /**
     * Get the waypoint of current index
     * @return target waypoint
     */
    public Vector2 getTargetWaypoint() {
        return route.getWaypointAt(i);
    }

    /** Toggle patrol direction. */
    private void toggleDirection() {
        forward = !forward;
    }

    /**
     * Advance to the next index (for accessing waypoints).
     * Handles ping-pong behaviour by toggling the patrol direction
     * and incrementing/decrementing the index accordingly.
     * Does nothing for single point patrols.
     */
    private void nextIndex() {
        // Index does not change for routes with single waypoints
        if (route.numWaypoints() <= 1) return;

        if (forward) {
            if (getIndex() == route.numWaypoints() - 1) {
                toggleDirection();
                i--;
            } else {
                i++;
            }
        } else {
            if (getIndex() == 0) {
                toggleDirection();
                i++;
            } else {
                i--;
            }
        }
    }

    /**
     * Stop the current subtask (if not null) and start a new one.
     * @param newTask to be started (wait or movement)
     */
    private void swapTask(Task newTask) {
        if (currentTask != null) {
            logger.debug("{} Changing to task {}", currentTask, newTask);
            currentTask.stop();
        }
        currentTask = newTask;
        currentTask.start();
    }
}