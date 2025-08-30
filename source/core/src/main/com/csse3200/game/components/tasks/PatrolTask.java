package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.ai.tasks.Task;

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
    private final Vector2 spawnPos;
    private final Vector2[] steps; // Relative waypoints
    private final float waitTime;

    private Vector2[] waypoints;
    private int i = 0;
    private boolean forward = true;

    private MovementTask movementTask;
    private WaitTask waitTask;
    private Task currentTask;

    /**
     * Creates a patrol starting from a fixed start position.
     * @param spawnPos specifies the starting position
     * @param steps array of relative steps used to build the waypoints
     * @param waitTime specifies how long to wait at each waypoint
     */
    public PatrolTask(Vector2 spawnPos, Vector2[] steps, float waitTime) {
        this.spawnPos = spawnPos;
        this.waitTime = waitTime;
        this.steps = new Vector2[steps.length];
        for (int k = 0; k < steps.length; k++) this.steps[k] = new Vector2(steps[k]);
    }

    /** Return a low priority of 1. */
    @Override
    public int getPriority() {return 1;} // Low priority

    /** Build an array of (cumulative) waypoints based on relative steps. */
    private void buildWaypoints() {
        waypoints = new Vector2[steps.length];
        Vector2 acc = new Vector2();
        for (int k = 0; k < steps.length; k++) {
            acc.add(steps[k]);
            waypoints[k] = new Vector2(spawnPos).add(acc);
        }
    }

    /**
     * Set up subtasks (wait, movement) and begin moving toward first waypoint. If there are
     * no steps the status of the task is FINISHED.
     */
    @Override
    public void start() {
        super.start();

        if (steps.length == 0) {
            status = Status.FINISHED;
            return;
        }

        if (waypoints == null) {
            buildWaypoints();
        }

        if (waitTask == null) {
            waitTask = new WaitTask(waitTime);
            waitTask.create(owner);
        }

        if (movementTask == null) {
            movementTask = new MovementTask(getWorldPos());
            movementTask.create(owner);
        } else {
            movementTask.setTarget(getWorldPos());
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
                movementTask.setTarget(getWorldPos());
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
    boolean isWaiting() {
        return currentTask == waitTask;
    }

    /**
     * Check if moving.
     * @return true if currentTask is movementTask
     */
    boolean isMoving() {
        return currentTask == movementTask;
    }

    /** Get the current index pointing to a waypoint. */
    int getIndex() {
        return i;
    }

    /**
     * Get an array of the cumulative waypoints, built from the relative steps.
     * @return an array of waypoints or null if unset
     */
    Vector2[] getWaypoints() {
        if (waypoints == null) return null;
        else return waypoints;
    }

    /**
     * Check patrol direction.
     * @return true for forward, otherwise false
     */
    boolean isMovingForward() {
        return forward;
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
        if (waypoints.length <= 1) return;

        if (forward) {
            if (getIndex() == waypoints.length - 1) {
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

    /** Get the waypoint that the current index points to. */
    private Vector2 getWorldPos() {
        return new Vector2(waypoints[i]);
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