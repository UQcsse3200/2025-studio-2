package com.csse3200.game.components.tasks;

import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.entities.Entity;

/**
 *  Makes an entity continuously move toward a designated target entity.
 *  This task is only runnable by AITaskComponent when it has been explicitly activated, which lets
 *  external systems control when chasing should occur. For example, in EnemyFactory,
 *  drones listen to 'playerDetected' and 'playerLost' to activate or deactivate the chasing behaviour.
 *
 *  getPriority() returns a high priority when the task is active so that the AI scheduler will select it
 *  over low priority tasks like patrols or idle behaviours. When deactivated, its priority is set to -1 so the
 *  task is never scheduled.
 **/
public class ChaseTask extends DefaultTask implements PriorityTask {
    private final Entity target;
    private MovementTask movementTask;
    private boolean active = false;

    /**
     * Creates a new chase task that will pursue the given target entity
     * @param target The target entity to be chased
     */
    public ChaseTask(Entity target) {
        this.target = target;
    }

    /**
     * Activate the chase task, making it eligible for scheduling by the AI system.
     * Typically called in response to a 'playerDetected' event.
     */
    public void activate() {
        if (active) return;
        active = true;
    }

    /**
     * Deactivate the chase task, preventing it from being scheduled.
     * Typically called in response to a 'playerLost' event.
     */
    public void deactivate() {
        if (!active) return;
        active = false;
    }

    /**
     * Start the chase behaviour. Early return if the task is not active.
     */
    @Override
    public void start() {
        super.start();
        if (!active) return;

        if (movementTask == null) {
            movementTask = new MovementTask(target.getPosition());
            movementTask.create(owner);
        } else {
            movementTask.setTarget(target.getPosition());
        }
        movementTask.start();

        this.owner.getEntity().getEvents().trigger("chaseStart");
    }

    /**
     * Update the chase behaviour each frame.
     */
    @Override
    public void update() {
        if (!active || movementTask == null) return;

        movementTask.setTarget(target.getPosition());
        movementTask.update();
        if (movementTask.getStatus() != Status.ACTIVE) {
            movementTask.start();
        }
    }

    /**
     * Stop the chase behaviour and movement subtask.
     */
    @Override
    public void stop() {
        if (movementTask != null) movementTask.stop();
        deactivate();
        super.stop();

        this.owner.getEntity().getEvents().trigger("chaseEnd");
    }

    /**
     * Get the current priority of the task
     * @return 10 if active, otherwise -1.
     */
    @Override
    public int getPriority() {
        return active ? 10 : -1;
    }
}