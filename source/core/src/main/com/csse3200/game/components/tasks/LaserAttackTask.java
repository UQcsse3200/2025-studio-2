package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.rendering.AnimationRenderComponent;
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
    private final float laserSpeed;
    private final int laserDamage;
    private boolean debug = false; // Optional debug flag

    /**
     * @param target The entity to attack.
     * @param priority Task priority.
     * @param attackDistance The distance from the entity at which to stop and attack.
     * @param laserSpeed The speed of the laser projectile.
     * @param laserDamage The damage of the laser projectile.
     */
    public LaserAttackTask(Entity target, int priority, float attackDistance, float laserSpeed, int laserDamage) {
        this.target = target;
        this.priority = priority;
        this.attackDistance = attackDistance;
        this.laserSpeed = laserSpeed;
        this.laserDamage = laserDamage;
        this.physics = ServiceLocator.getPhysicsService().getPhysics();
    }

    @Override
    public void start() {
        super.start();
        movementTask = new MovementTask(target.getPosition());
        movementTask.create(owner); // Set the owner of the MovementTask
        movementTask.start();
        this.owner.getEntity().getEvents().trigger("chaseStart");
    }

    @Override
    public void update() {
        if (movementTask != null) {
            movementTask.update();
        }

        float distance = getDistanceToTarget();
        if (distance <= attackDistance) {
// Stop moving and attack the target
            if (movementTask != null) {
                movementTask.stop();
            }
            if (System.currentTimeMillis() - lastAttackTime > attackDelay) {
// Compute laser direction safely
                Vector2 direction = target.getCenterPosition().cpy().sub(owner.getEntity().getCenterPosition()).nor();
// Fire laser
                ProjectileFactory.createLaser(owner.getEntity(), direction, laserSpeed, laserDamage);
// Trigger attack animation
                // Trigger attack animation directly via AnimationRenderComponent
                AnimationRenderComponent animationRender = owner.getEntity().getComponent(AnimationRenderComponent.class);
                if (animationRender != null) {
                    animationRender.startAnimation("laser_attack");
                }
// Optional debug log
                if (debug) {
                    System.out.println("LaserAttackTask: Fired laser at target with speed " + laserSpeed + " and damage " + laserDamage);
                }
                lastAttackTime = System.currentTimeMillis();
            }
        } else {
// Continue moving towards the target
            if (movementTask != null) {
                movementTask.setTarget(target.getPosition());
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (movementTask != null) {
            movementTask.stop();
        }
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