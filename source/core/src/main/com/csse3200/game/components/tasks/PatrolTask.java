package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.ai.tasks.Task;
import com.csse3200.game.components.enemy.PatrolRouteComponent;


/**
 * Makes an entity move through a sequence of waypoints and wait briefly at each one. Changes direction when it
 * reaches the end, repeating indefinitely. Waypoints are computed once from a fixed spawn position and a list
 * of relative step vectors. Requires an entity with a PhysicsMovementComponent.
 * In the current implementation, if the entity does not start at the first waypoint, it beelines there (no
 * pathfinding/obstacle avoidance).
 * TODO: Implement a cool down task to ensure entity starts a patrol at the first waypoint.
 * TODO: Will modify task priority and start() logic with additional checks once this is done.
 * */
public class PatrolTask extends DefaultTask implements PriorityTask {
    private final float waitTime;

    private PatrolRouteComponent route;
    private int i = 0;
    private boolean forward = true;

    private MovementTask movementTask;
    private WaitTask waitTask;
    private Task currentTask;

    /**
     * Creates a patrol starting from a fixed start position.
     * @param waitTime specifies how long to wait at each waypoint
     */
    public PatrolTask(float waitTime) {
        this.waitTime = waitTime;
    }

    /** Return a low priority of 1. */
    @Override
    public int getPriority() {return 1;} // Low priority

    /**
     * Set up subtasks (wait, movement) and begin moving toward first waypoint. If there are
     * no steps the status of the task is FINISHED.
     */
    @Override
    public void start() {
        super.start();

        if (route == null) {
            route = this.owner.getEntity().getComponent(PatrolRouteComponent.class);

            if (route.numWaypoints() == 0) {
                throw new IllegalStateException("Patrol route is empty");
            }

        }

        if (waitTask == null) {
            waitTask = new WaitTask(waitTime);
            waitTask.create(owner);
        }

        if (movementTask == null) {
            movementTask = new MovementTask(route.getWaypointAt(i));
            movementTask.create(owner);
        } else {
            movementTask.setTarget(route.getWaypointAt(i));
        }

        movementTask.start();
        currentTask = movementTask;

        this.owner.getEntity().getEvents().trigger("patrolStart");
    }

    /**
     * Advance the patrol state. Handles swapping between wait and movement tasks
     * based on their status.
     */
    @Override
    public void update() {
        if (currentTask.getStatus() != Status.ACTIVE) {
            if (currentTask == movementTask) {
                swapTask(waitTask); // Always wait between waypoints
            } else {
                nextIndex();
                movementTask.setTarget(route.getWaypointAt(i));
                swapTask(movementTask);
            }
        }
        currentTask.update();
    }

    /** Stop the current subtasks and deactivate patrol task. */
    @Override
    public void stop() {
        if (currentTask != null) {
            currentTask.stop();
            super.stop();
        }
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
            currentTask.stop();
        }
        currentTask = newTask;
        currentTask.start();
    }
}