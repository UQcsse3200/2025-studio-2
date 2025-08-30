package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.ai.tasks.Task;

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

    public PatrolTask(Vector2 spawnPos, Vector2[] steps, float waitTime) {
        this.spawnPos = spawnPos;
        this.waitTime = waitTime;
        this.steps = new Vector2[steps.length];
        for (int k = 0; k < steps.length; k++) this.steps[k] = new Vector2(steps[k]);
    }

    @Override
    public int getPriority() {return 1;} // Low priority

    private void buildWaypoints() {
        waypoints = new Vector2[steps.length];
        Vector2 acc = new Vector2();
        for (int k = 0; k < steps.length; k++) {
            acc.add(steps[k]);
            waypoints[k] = new Vector2(spawnPos).add(acc);
        }
    }

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

    @Override
    public void stop() {
        if (currentTask != null) {
            currentTask.stop();
            super.stop();
        }
    }

    // Helpers
    boolean isWaiting() {
        return currentTask == waitTask;
    }

    boolean isMoving() {
        return currentTask == movementTask;
    }

    int getIndex() {
        return i;
    }

    Vector2[] getWaypoints() {
        if (waypoints == null) return null;
        else return waypoints;
    }

    boolean isMovingForward() {
        return forward;
    }

    /** Toggle patrol direction */
    private void toggleDirection() {
        forward = !forward;
    }

    /** Increment/decrement index based on patrol direction */
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

    /** Get the next waypoint */
    private Vector2 getWorldPos() {
        return new Vector2(waypoints[i]);
    }

    /** Stop current task and start newTask */
    private void swapTask(Task newTask) {
        if (currentTask != null) {
            currentTask.stop();
        }
        currentTask = newTask;
        currentTask.start();
    }
}