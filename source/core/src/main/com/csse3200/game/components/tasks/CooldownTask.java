package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.components.enemy.PatrolRouteComponent;
import com.csse3200.game.components.enemy.SpawnPositionComponent;

/**
 * A cooldown task that activates after a chase has ended.
 * When activated, the entity remains idle for a set cooldown duration,
 * then teleports back to either:
 * - The start of its patrol route (if one exists)
 * - Its original spawn position (if no patrol route exists)
 * This ensures drones reset to their initial state instead of
 * beelining back manually.
 */
public class CooldownTask extends DefaultTask implements PriorityTask {
    private final float waitTime;

    private boolean active = false;
    private Vector2 resetPos;
    private WaitTask waitTask;

    /**
     * Creates a cooldown task with the given wait duration.
     *
     * @param waitTime duration (in seconds) to wait before teleporting
     *                 the entity back to its original position
     */
    public CooldownTask(float waitTime) {
        this.waitTime = waitTime;
    }

    /**
     * Activates the cooldown task. Typically triggered by a
     * "playerLost" event.
     */
    public void activate() {
        if (active) return;
        active = true;
    }

    /** Deactivate the cooldown task, preventing it from being scheduled.
     * Typically called in response to a `playerDetected` event.
     */
    public void deactivate() {
        if (!active) return;
        active = false;
    }

    /**
     * Starts the cooldown. Starts a wait subtask, disables gravity, and triggers a
     * {@code "cooldownStart"} event for animations TO BE ADDED.
     */
    @Override
    public void start() {

        super.start();
        if (!active) return;
        resetPos = computeResetPos();

        if (waitTask == null) {
            waitTask = new WaitTask(waitTime);
            waitTask.create(owner);
        }
        waitTask.start();

        // Trigger event so animations/sfx can be implemented
        owner.getEntity().getEvents().trigger("cooldownStart");

    }

    /**
     * Advance wait task. Once the cooldown wait has elapsed,
     * the entity is teleported back to its patrol start or spawn
     * position the cooldown task is stopped.
     * Calls deactivate() to stop task being runnable.
     */
    @Override
    public void update() {
        if (!active || waitTask == null) return;
        waitTask.update();

        if (waitTask.getStatus() == Status.FINISHED) {
            owner.getEntity().setPosition(resetPos);
            deactivate();
        }
    }

    /**
     * Returns the priority of this task. The cooldown is only
     * considered runnable while active is true.
     *
     * @return priority 5 when active, otherwise -1 (disabled)
     */
    @Override
    public int getPriority() {
        return active ? 5 : -1;
    }

    /**
     * When the cooldown task is stopped, gravity is restored and a
     * 'cooldownEnd' event is triggered.
     */
    @Override
    public void stop() {
        if (waitTask != null) waitTask.stop();

        super.stop();
        owner.getEntity().getEvents().trigger("cooldownEnd");
    }

    /** Choose reset position: Patrol start, otherwise spawn pos, fallback to current position */
    private Vector2 computeResetPos() {
        // Return first patrol waypoint if entity has a PatrolRouteComponent
        PatrolRouteComponent patrol = owner.getEntity().getComponent(PatrolRouteComponent.class);
        if (patrol != null && patrol.numWaypoints() > 0) {
            return patrol.patrolStart();
        }

        // Otherwise return the spawn position
        SpawnPositionComponent spawn = owner.getEntity().getComponent(SpawnPositionComponent.class);
        if (spawn != null) {
            return spawn.getSpawnPos();
        }

        // Return current position as a fallback
        return new Vector2(owner.getEntity().getPosition());
    }
}
