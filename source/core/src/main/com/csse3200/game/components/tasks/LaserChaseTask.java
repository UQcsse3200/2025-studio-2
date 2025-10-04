package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.ai.tasks.TaskRunner;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 * Task that makes a laser drone chase its target and maintain an optimal height.
 */
public class LaserChaseTask extends DefaultTask implements PriorityTask {
    private final Entity target;
    private final int priority;
    private final float maxChaseDistance;
    private final float optimalHeight;
    private final float heightTolerance;
    private final PhysicsEngine physics;
    private final DebugRenderer debugRenderer;
    private final RaycastHit hit = new RaycastHit();
    private final GameTime timeSource;
    private MovementTask movementTask;

    // State tracking
    private boolean targetAcquired = false;

    private static final long CHASE_GRACE_MS = 1500L; // 1.5 seconds
    private long lastSpottedTime = 0L;
    private boolean everSpottedTarget = false;

    /**
     * Creates a chase task for a laser drone.
     *
     * @param target the entity to chase
     * @param priority task priority when chasing
     * @param maxChaseDistance maximum distance before chase stops
     * @param optimalHeight height to maintain above the target
     * @param heightTolerance tolerance for height positioning
     */
    public LaserChaseTask(Entity target, int priority, float maxChaseDistance,
                          float optimalHeight, float heightTolerance) {
        this.target = target;
        this.priority = priority;
        this.maxChaseDistance = maxChaseDistance;
        this.optimalHeight = optimalHeight;
        this.heightTolerance = heightTolerance;
        this.physics = ServiceLocator.getPhysicsService().getPhysics();
        this.debugRenderer = ServiceLocator.getRenderService().getDebug();
        this.timeSource = ServiceLocator.getTimeSource();
    }

    @Override
    public void create(TaskRunner owner) {
        super.create(owner);
    }

    public void activate() {
        this.targetAcquired = true;
    }

    public void deactivate() {
        this.targetAcquired = false;
    }

    @Override
    public void start() {
        super.start();
        if (movementTask == null) {
            movementTask = new MovementTask(getChaseTarget());
            movementTask.create(owner);
        }
        movementTask.start();
        owner.getEntity().getEvents().trigger("chaseLaser");
    }

    @Override
    public void update() {
        if (movementTask == null) return;

        if (!targetAcquired) {
            movementTask.setTarget(getSearchPosition());
        } else {
            movementTask.setTarget(getChaseTarget());
        }

        movementTask.update();
        if (movementTask.getStatus() != MovementTask.Status.ACTIVE) {
            movementTask.start();
        }
    }

    @Override
    public void stop() {
        if (movementTask != null) movementTask.stop();
        super.stop();
        owner.getEntity().getEvents().trigger("chaseEnd");
    }

    @Override
    public int getPriority() {
        if (target == null) return -1;

        if (targetAcquired) {
            lastSpottedTime = timeSource.getTime();
            everSpottedTarget = true;
            return priority;
        }

        if (!everSpottedTarget) return -1;

        long timeSinceSpotted = timeSource.getTime() - lastSpottedTime;
        if (timeSinceSpotted <= CHASE_GRACE_MS) {
            return priority;
        }

        everSpottedTarget = false;
        return -1;
    }

    /** Calculate the desired chase position above the target */
    private Vector2 getChaseTarget() {
        Vector2 targetPos = target.getPosition().cpy();
        Vector2 currentPos = owner.getEntity().getPosition();
        targetPos.y += optimalHeight;

        float horizontalDistance = Math.abs(currentPos.x - targetPos.x);
        if (horizontalDistance < 0.5f) targetPos.x = currentPos.x;

        return targetPos;
    }

    /** Get a search position when target is lost */
    private Vector2 getSearchPosition() {
        Vector2 lastKnownPos = target.getPosition().cpy();
        lastKnownPos.y += optimalHeight * 1.5f;
        return lastKnownPos;
    }

    /** Returns whether the target is currently acquired */
    public boolean hasTarget() {
        return targetAcquired;
    }
}