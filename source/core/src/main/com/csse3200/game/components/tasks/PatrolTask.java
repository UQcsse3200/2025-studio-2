package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.ai.tasks.Task;
import com.csse3200.game.components.StartPositionComponent;

public class PatrolTask extends DefaultTask implements PriorityTask {
    private final Vector2[] offsets; // Relative waypoints
    private final float waitTime;

    private Vector2 origin;
    private int i = 0;
    private boolean forward = true;

    private MovementTask movementTask;
    private WaitTask waitTask;
    private Task currentTask;

    public PatrolTask(Vector2[] offsets, float waitTime) {
        this.waitTime = waitTime;
        this.offsets = new Vector2[offsets.length];
        for (int k = 0; k < offsets.length; k++) this.offsets[k] = new Vector2(offsets[k]);
    }

    @Override
    public int getPriority() {return 1;} // Low priority

    @Override
    public void start() {
        super.start();

        if (offsets.length == 0) {
            status = Status.FINISHED;
            return;
        }

        if (origin == null) {
            StartPositionComponent sp = owner.getEntity().getComponent(StartPositionComponent.class);
            if (sp == null) {
                throw new IllegalStateException("PatrolTask requires StartPositionComponent on entity");
            }
            origin = sp.getStartPos();
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
    /** Check if index of waypoint points to start */
    private boolean isStart() {
        return i == 0;
    }

    /** Check if index of waypoint points to end */
    private boolean isEnd() {
        return i == offsets.length - 1;
    }

    /** Toggle patrol direction */
    private void toggleDirection() {
        forward = !forward;
    }

    /** Increment/decrement index based on patrol direction */
    private void nextIndex() {
        if (offsets.length <= 1) return;

        if (forward) {
            if (isEnd()) {
                toggleDirection();
                i--;
            } else {
                i++;
            }
        } else {
            if (isStart()) {
                toggleDirection();
                i++;
            } else {
                i--;
            }
        }
    }

    /** Get the next waypoint (in the world) based on startPos and offsets */
    private Vector2 getWorldPos() {
        return new Vector2(origin).add(offsets[i]);
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