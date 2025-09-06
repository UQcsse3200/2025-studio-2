package com.csse3200.game.components.tasks;

import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.entities.Entity;

/** Chases a target entity */
public class ChaseTask extends DefaultTask implements PriorityTask {
    private final Entity target;
    private MovementTask movementTask;
    private boolean active = false;

    /**
     * @param target The entity to chase.
     */
    public ChaseTask(Entity target) {
        this.target = target;
    }

    /**
     * Allows us to externally activate chase task (i.e. playerDetected)
     */
    public void activate() {
        if (active) return;
        active = true;
    }

    /**
     * Allows us to externally deactivate chase task (i.e. playerLost)
     */
    public void deactivate() {
        if (!active) return;
        active = false;
    }

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

    @Override
    public void update() {
        if (!active || movementTask == null) return;

        movementTask.setTarget(target.getPosition());
        movementTask.update();
        if (movementTask.getStatus() != Status.ACTIVE) {
            movementTask.start();
        }
    }

    @Override
    public void stop() {
        if (movementTask != null) movementTask.stop();
        deactivate();
        super.stop();

        this.owner.getEntity().getEvents().trigger("chaseEnd");
    }

    @Override
    public int getPriority() {
        return active ? 10 : -1;
    }
}