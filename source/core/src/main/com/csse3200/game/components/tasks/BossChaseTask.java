package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.entities.Entity;

/**
 * Boss AI chase behaviour.
 * - Chases the player until it reaches a specified x threshold (specified by stop point in constructor)
 * - Once the player escapes, the boss moves to the fixed stop point
 * - The task deactivates when the boss arrives at the stop point (i.e. it cannot be scheduled by AI task runner)
 */
public class BossChaseTask extends DefaultTask implements PriorityTask {
    private final Entity player;
    private final Vector2 stopPoint; // (stopX, stopY) in world coordinates
    private Vector2 currentTarget;
    private static final Vector2 OFFSET = new Vector2(-3f, -2); // Need offset since boss is scaled

    private MovementTask movementTask;
    private boolean chasing = true; // true: chase player, false: go to stop point
    private boolean active = true;

    /**
     * Construct the boss chase task
     * @param player the player entity to chase
     * @param stopPoint the world-space position (stopX, stopY) the boss will move to
     *                  after the player passes stopX
     */
    public BossChaseTask(Entity player, Vector2 stopPoint) {
        this.player = player;
        this.stopPoint = stopPoint.cpy();
    }

    /**
     * Activates the task, making it eligible for scheduling.
     */
    public void activate() {
        active = true;
    }

    /**
     * Deactivates the task, preventing it from being scheduled.
     */
    public void deactivate() {
        active = false;
    }

    /**
     * Starts the task. If active, decides whether to begin by chasing the player
     * or going directly to the stop point.
     * Creates and starts the movement subtask.
     * Emits a "bossChaseStart" event.
     */
    @Override
    public void start() {
        super.start();
        if (!active) return;

        // Check player's position
        chasing = player.getPosition().x < stopPoint.x;
        currentTarget = chasing ? player.getPosition().add(OFFSET) : stopPoint;

        movementTask = new MovementTask(currentTarget);
        movementTask.create(owner);
        movementTask.start();

        this.owner.getEntity().getEvents().trigger("bossChaseStart");
    }

    /**
     * Updates the task each frame. While the player is left of stopX, chases the
     * player. Once the player is at/past stopX, moves to the stopPoint.
     */
    @Override
    public void update() {
        if (!active) return;

        chasing = player.getPosition().x < stopPoint.x;
        if (!chasing && hasReachedStopPoint()) {
            movementTask.stop();
            deactivate();
            return;
        }

        Vector2 desired = chasing ? player.getPosition().add(OFFSET) : stopPoint;
        if (desired.dst2(currentTarget) > 0.5f) {
            currentTarget = desired;
            movementTask.setTarget(currentTarget);
            if (movementTask.getStatus() != Status.ACTIVE) {
                movementTask.start();
            }
        }

        if (movementTask.getStatus() != Status.FINISHED) {
            movementTask.update();
        }
    }

    /**
     * Stops the task and its movement subtask.
     * Emits a "bossChaseEnd" event.
     */
    @Override
    public void stop() {
        if (movementTask != null) movementTask.stop();
        deactivate();
        super.stop();
        this.owner.getEntity().getEvents().trigger("bossChaseEnd");
    }

    /**
     * Returns the current scheduling priority.
     * @return 10 when active (eligible), otherwise -1.
     */
    @Override
    public int getPriority() {
        return active ? 10 : -1;
    }

    /**
     * Checks whether the boss has reached the stop point.
     * @return true if boss is within range of the stop point, otherwise false.
     */
    private boolean hasReachedStopPoint() {
        Vector2 pos = owner.getEntity().getPosition();
        return pos.dst2(stopPoint) <= 0.5f;
    }

    /**
     * Get current target
     * @return a copy of the current movement target the boss is pursuing
     * (either player position or stop point)
     */
    public Vector2 getCurrentTarget() {
        return currentTarget == null ? null : currentTarget.cpy();
    }
}