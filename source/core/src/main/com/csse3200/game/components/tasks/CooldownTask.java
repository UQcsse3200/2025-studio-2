package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.components.enemy.PatrolRouteComponent;
import com.csse3200.game.components.enemy.SpawnPositionComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;

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
    private float timer;
    private PatrolRouteComponent route;
    private boolean active = false;

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
     * "chaseEnd" event when the drone loses the player.
     */
    public void activate() {
        active = true;
        status = Status.INACTIVE;
    }

    /**
     * Starts the cooldown. Resets the timer, stops entity
     * movement, disables gravity, and triggers a
     * {@code "cooldownStart"} event for animations TO BE ADDED.
     */
    @Override
    public void start() {
        super.start();
        timer = 0f;
        Entity entity = this.owner.getEntity();



        PhysicsComponent physics = owner.getEntity().getComponent(PhysicsComponent.class);
        if (physics != null) {
            physics.getBody().setGravityScale(0f);
            physics.getBody().setLinearVelocity(0f, 0f); // stop falling instantly
        }

        // Trigger event so animations/sfx can be implemented
        owner.getEntity().getEvents().trigger("cooldownStart");
    }

    /**
     * Updates the cooldown timer. Once the cooldown has elapsed,
     * the entity is teleported back to its patrol start or spawn
     * position, gravity is restored, and a {@code "cooldownEnd"}
     * event is fired.
     */
    @Override
    public void update() {
        float delta = com.badlogic.gdx.Gdx.graphics.getDeltaTime();
        timer += delta;
        if (timer >= waitTime) {
            Entity entity = owner.getEntity();
            Vector2 resetPos = null;

            PatrolRouteComponent patrol = entity.getComponent(PatrolRouteComponent.class);
            if (patrol != null) {
                resetPos = patrol.patrolStart();
            } else {
                SpawnPositionComponent spawn = entity.getComponent(SpawnPositionComponent.class);
                if (spawn != null) {
                    resetPos = spawn.getSpawnPos();
                }
            }

            if (resetPos != null) {
                entity.setPosition(resetPos);
            }
            // Re-enable gravity after teleport
            PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
            if (physics != null) {
                physics.getBody().setGravityScale(1f);
            }



            owner.getEntity().getEvents().trigger("cooldownEnd");

            status = Status.FINISHED;
            active = false;

        }
    }

    /**
     * Returns the priority of this task. The cooldown is only
     * considered runnable while {@link #active} is true.
     *
     * @return priority 2 when active, otherwise -1 (disabled)
     */
    @Override
    public int getPriority() {
        return active ? 2 : -1;

    }

    /**
     * Stops the cooldown task. Resets the timer so the task
     * can be cleanly restarted later.
     */
    @Override
    public void stop() {
        super.stop();
        timer = 0f;
    }
}
