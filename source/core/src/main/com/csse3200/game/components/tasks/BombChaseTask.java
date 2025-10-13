package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.ai.tasks.TaskRunner;
import com.csse3200.game.components.lighting.ConeDetectorComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 * Task that makes a bomber chase and position itself optimally above a target
 * when detected by its cone light.
 */
public class BombChaseTask extends DefaultTask implements PriorityTask {
    private final Entity target;
    private final int priority;
    private final float optimalHeight; // Optimal height to maintain above target for bombing
    private final float heightTolerance; // Tolerance for height positioning

    private final GameTime timeSource;
    private MovementTask movementTask;
    private ConeDetectorComponent detectorComponent;

    // State tracking
    private boolean targetAcquired = false;

    private static final long CHASE_GRACE_MS = 1500L;
    private long lastSpottedTime = 0L;
    private boolean everSpottedTarget = false;

    /**
     * Creates a chase task for a bomber that uses cone light detection.
     *
     * @param target            the entity to chase
     * @param priority          task priority when chasing
     * @param maxChaseDistance  maximum distance before chasing stops
     * @param optimalHeight     optimal height to maintain above target for bombing
     * @param heightTolerance   tolerance for height positioning
     */
    public BombChaseTask(Entity target, int priority, float maxChaseDistance,
                         float optimalHeight, float heightTolerance) {
        this.target = target;
        this.priority = priority;
        this.optimalHeight = optimalHeight;
        this.heightTolerance = heightTolerance;
        timeSource = ServiceLocator.getTimeSource();
    }

    @Override
    public void create(TaskRunner owner) {
        super.create(owner);
        // Get the bomber's detector component
        detectorComponent = owner.getEntity().getComponent(ConeDetectorComponent.class);
    }

    /**
     * Activates the chase task. The priority will be raised, allowing the task to run.
     */
    public void activate() {
        targetAcquired = true;
    }

    /**
     * Deactivates the chase task. The priority will be set to -1, stopping the task.
     */
    public void deactivate() {
        targetAcquired = false;
    }

    /**
     * Initialise and starts movement toward the chase target.
     */
    @Override
    public void start() {
        super.start();

        if (null == movementTask) {
            movementTask = new MovementTask(getChaseTarget());
            movementTask.create(owner);
        }
        movementTask.start();

        owner.getEntity().getEvents().trigger("chaseBomber");
    }

    /**
     * Update the chase, positioning bomber at optimal height above target.
     */
    @Override
    public void update() {
        if (null == movementTask) return;

        // If lost cone detection during chase, move to search position
        if (!targetAcquired && null != detectorComponent) {
            // Move higher to try to reacquire target with cone light
            movementTask.setTarget(getSearchPosition());
        } else {
            // Normal chase behavior - position above target at optimal height
            movementTask.setTarget(getChaseTarget());
        }

        movementTask.update();
        if (Status.ACTIVE != movementTask.getStatus()) {
            movementTask.start();
        }
    }

    /**
     * Calculate the desired chase position at optimal bombing height.
     * Positions the bomber above the target for effective bombing.
     */
    private Vector2 getChaseTarget() {
        Vector2 targetPos = target.getPosition().cpy();
        Vector2 currentPos = owner.getEntity().getPosition();

        // Position bomber at optimal height above target
        targetPos.y += optimalHeight;

        // Smooth horizontal positioning - avoid jittery movement when close
        float horizontalDistance = Math.abs(currentPos.x - target.getPosition().x);
        if (0.5f > horizontalDistance) {
            // If already close horizontally, maintain position
            targetPos.x = currentPos.x;
        }

        return targetPos;
    }

    /**
     * Get a search position when target is lost.
     * Moves higher to get better cone coverage for reacquiring target.
     */
    private Vector2 getSearchPosition() {
        Vector2 lastKnownPos = target.getPosition().cpy();
        lastKnownPos.y += optimalHeight * 1.5f; // Higher for better search coverage
        return lastKnownPos;
    }

    /** Stop the task */
    @Override
    public void stop() {
        if (null != movementTask) {
            movementTask.stop();
        }
        super.stop();
        owner.getEntity().getEvents().trigger("chaseEnd");
    }

    /**
     * Get current priority of the task.
     * Priority system:
     * - Returns priority when cone light has detected target
     * - Returns lower priority (5) when searching for lost target
     * - Returns -1 if target too far or at correct position for bombing
     */
    @Override
    public int getPriority() {
        // No detector component, can't operate
        if (null == detectorComponent) {
            return -1;
        }

        // If at optimal bombing position and target detected, yield to BombDropTask
        if (detectorComponent.isDetected()) {
            lastSpottedTime = timeSource.getTime();
            everSpottedTarget = true;
            // If already in the best position, let BombDropTask take over (return -1)
            if (isAtOptimalPosition()) {
                return -1;
            }
            // Otherwise, keep high priority chase
            return priority;
        }

        // --- If the light cone cannot detect the target ---

        // If you have never found a target, you won't chase it
        if (!everSpottedTarget) {
            return -1;
        }

        // If you have ever discovered a goal, check if the grace period is over
        long timeSinceSpotted = timeSource.getTime() - lastSpottedTime;
        if (CHASE_GRACE_MS >= timeSinceSpotted) {
            // Still in grace period, continue to chase
            if (isAtOptimalPosition()) {
                return -1; // Even during grace period, you should try to drop bullets when you arrive at your location
            }
            return priority;
        }

        // The grace period ends, stop the pursuit, and reset the status for the next trigger
        everSpottedTarget = false;
        return -1;
    }

    /**
     * Check if bomber is at optimal position for bombing.
     * Returns true when horizontally aligned and at correct height.
     */
    private boolean isAtOptimalPosition() {
        Vector2 bomberPos = owner.getEntity().getCenterPosition();
        Vector2 targetPos = target.getCenterPosition();

        // Check horizontal alignment
        float horizontalDistance = Math.abs(bomberPos.x - targetPos.x);
        if (1.0f < horizontalDistance) {
            return false;
        }

        // Check vertical positioning
        float heightDiff = bomberPos.y - targetPos.y;
        return Math.abs(heightDiff - optimalHeight) <= heightTolerance;
    }

    /**
     * Check if the chase task has acquired a target via cone light detection.
     * @return true if target acquired, false otherwise
     */
    public boolean hasTarget() {
        return targetAcquired;
    }
}