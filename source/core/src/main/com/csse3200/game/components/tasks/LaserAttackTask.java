package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;

/**
 * Task for an entity to attack a target with a laser.
 * Fires safely only when target is in range and visible.
 */
public class LaserAttackTask extends DefaultTask implements PriorityTask {
    private final Entity target;
    private final int priority;
    private final float attackDistance;
    private final PhysicsEngine physics;
    private final RaycastHit hit = new RaycastHit();
    private MovementTask movementTask;
    private long lastAttackTime;
    private final long attackDelay = 2000; // 1 second between attacks
    private final float laserSpeed;
    private final int laserDamage;
    private boolean debug = false; // Set true for debug logs
    private boolean targetDetected = false; // Tracks if player is detected

    public LaserAttackTask(Entity target, int priority, float attackDistance, float laserSpeed, int laserDamage) {
        this.target = target;
        this.priority = priority;
        this.attackDistance = attackDistance;
        this.laserSpeed = laserSpeed;
        this.laserDamage = laserDamage;
        this.physics = ServiceLocator.getPhysicsService().getPhysics();
    }

    public void setTargetDetected(boolean detected) {
        this.targetDetected = detected;
    }

    @Override
    public void start() {
        super.start();
        if (target != null) {
            movementTask = new MovementTask(target.getPosition());
            movementTask.create(owner);
            movementTask.start();
            owner.getEntity().getEvents().trigger("chaseStart");
        }
    }

    @Override
    public void update() {
        if (movementTask != null) {
            movementTask.update();
        }

        if (target == null || !targetDetected) return;

        float distance = getDistanceToTarget();

        if (distance <= attackDistance) {
            if (movementTask != null) movementTask.stop();

            if (System.currentTimeMillis() - lastAttackTime > attackDelay) {
                Vector2 direction = target.getCenterPosition().cpy().sub(owner.getEntity().getCenterPosition());

                if (direction.len2() > 0.0001f) {
                    direction.nor();
                    ProjectileFactory.createLaser(owner.getEntity(), direction, laserSpeed, laserDamage);

                    AnimationRenderComponent animationRender = owner.getEntity().getComponent(AnimationRenderComponent.class);
                    if (animationRender != null && animationRender.hasAnimation("laser_attack")) {
                        animationRender.startAnimation("laser_attack");
                    }

                    lastAttackTime = System.currentTimeMillis();
                    if (debug) {
                        System.out.println("LaserAttackTask: Fired laser at target with speed " + laserSpeed + " and damage " + laserDamage);
                    }
                } else if (debug) {
                    System.out.println("LaserAttackTask: Skipped firing due to zero-length direction vector");
                }
            }
        } else {
            if (movementTask != null) movementTask.setTarget(target.getPosition());
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (movementTask != null) movementTask.stop();
        owner.getEntity().getEvents().trigger("stopAttack");
    }

    @Override
    public int getPriority() {
        if (target == null || target.getComponent(CombatStatsComponent.class) == null) return -1;

        float distance = getDistanceToTarget();
        return (distance <= attackDistance && isTargetVisible()) ? priority : -1;
    }

    private float getDistanceToTarget() {
        return owner.getEntity().getPosition().dst(target.getPosition());
    }

    private boolean isTargetVisible() {
        if (target == null) return false;

        Vector2 from = owner.getEntity().getCenterPosition();
        Vector2 to = target.getCenterPosition();

        if (from.epsilonEquals(to, 0.01f)) return true;

        if (physics.raycast(from, to, PhysicsLayer.OBSTACLE, hit)) {
            return hit.fixture.getBody().getUserData() == target;
        }
        return true;
    }
}