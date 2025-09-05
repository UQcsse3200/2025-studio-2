package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;

/**
 * A task for an entity to attack a target with a laser.
 * This is a priority task, meaning it can be interrupted by other higher-priority tasks.
 */
public class LaserAttackTask extends DefaultTask implements PriorityTask {
    private final Entity target;
    private final int priority;
    private final float attackDistance;
    private final PhysicsEngine physics;
    private final RaycastHit hit = new RaycastHit();
    private MovementTask movementTask;
    private long lastAttackTime;
    private final long attackDelay = 1000; // 1 second delay between attacks

    /**
     * @param target The entity to attack.
     * @param priority Task priority.
     * @param attackDistance The distance from the entity at which to stop and attack.
     */
    public LaserAttackTask(Entity target, int priority, float attackDistance) {
        this.target = target;
        this.priority = priority;
        this.attackDistance = attackDistance;
        this.physics = ServiceLocator.getPhysicsService().getPhysics();
    }

    @Override
    public void start() {
        super.start();
        movementTask = new MovementTask(target.getPosition());
        movementTask.create(owner); // Set the owner of the MovementTask
        movementTask.start();
        // FIXED: Change to a valid animation to prevent crashes
        this.owner.getEntity().getEvents().trigger("chaseStart");
    }

    @Override
    public void update() {
        movementTask.update();

        float distance = getDistanceToTarget();
        if (distance <= attackDistance) {
            // Stop moving and attack the target
            movementTask.stop();
            if (System.currentTimeMillis() - lastAttackTime > attackDelay) {
                // Trigger the laser attack event
                Vector2 direction = target.getCenterPosition().sub(owner.getEntity().getCenterPosition()).nor();
                ProjectileFactory.createLaser(owner.getEntity(), direction, 10f, 10); // Example values for speed and damage
                lastAttackTime = System.currentTimeMillis();
            }
        } else {
            // Continue moving towards the target
            movementTask.setTarget(target.getPosition());
        }
    }

    @Override
    public void stop() {
        super.stop();
        movementTask.stop();
        this.owner.getEntity().getEvents().trigger("stopAttack");
    }

    @Override
    public int getPriority() {
        // Check if target exists and is a valid target
        if (target == null || target.getComponent(CombatStatsComponent.class) == null) {
            return -1;
        }

        float distance = getDistanceToTarget();
        if (distance <= attackDistance && isTargetVisible()) {
            return priority;
        }
        return -1; // No target, so no priority
    }

    private float getDistanceToTarget() {
        return owner.getEntity().getPosition().dst(target.getPosition());
    }

    private boolean isTargetVisible() {
        Vector2 from = owner.getEntity().getCenterPosition();
        Vector2 to = target.getCenterPosition();

        // If there's an obstacle, the target isn't visible.
        if (physics.raycast(from, to, PhysicsLayer.OBSTACLE, hit)) {
            return hit.fixture.getBody().getUserData() == target;
        }
        return true;
    }
}
