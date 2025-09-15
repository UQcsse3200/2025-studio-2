package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.services.ServiceLocator;

/** Task that makes an entity chase a target until either the target is too far away or the line of sight
 * is lost. Designed specifically for flying bomber enemies that should maintain a hover height above the
 * target and stop chasing when the target enters a bomb-drop zone. */
public class BombChaseTask extends DefaultTask implements PriorityTask {
    private final Entity target;
    private final int priority;
    private final float viewDistance;
    private final float maxChaseDistance;
    private final float hoverHeight; // Height to maintain above target
    private final PhysicsEngine physics;
    private final DebugRenderer debugRenderer;
    private final RaycastHit hit = new RaycastHit();
    private MovementTask movementTask;
    private final float dropRange;
    private final float minHeight;

// for light activation
    private boolean active = false;
    private boolean triggerMaxChase = false;
    private final float triggerDistance = 2f;

    /**
     * Creates a chase task for a bomber-style enemy.
     *
     * @param target            the entity to chase
     * @param priority          task priority when chasing
     * @param viewDistance      maximum distance at which chasing can start (when inactive)
     * @param maxChaseDistance  maximum distance before chasing stops
     * @param hoverHeight       preferred vertical offset to maintain above the target
     * @param dropRange         horizontal range within which the target is considered in the bomb-drop zone
     * @param minHeight         minimum vertical difference (drone above target) to qualify for the drop zone
     */
    public BombChaseTask(Entity target, int priority, float viewDistance, float maxChaseDistance, float hoverHeight,
                         float dropRange, float minHeight) {
        this.target = target;
        this.priority = priority;
        this.viewDistance = viewDistance;
        this.maxChaseDistance = maxChaseDistance;
        this.hoverHeight = hoverHeight;
        this.dropRange = dropRange;
        this.minHeight = minHeight;
        physics = ServiceLocator.getPhysicsService().getPhysics();
        debugRenderer = ServiceLocator.getRenderService().getDebug();
    }

    /** Activate the chase task for light triggered activation */
    public void activate() {
        active = true;
        triggerMaxChase = false; // reset enforcement
        if (status != Status.ACTIVE) status = Status.INACTIVE;
    }

    public void deactivate() {
        active = false;
        owner.getEntity().getEvents().trigger("chaseEnd");
    }

    /**
     * Initialise and starts a movement task toward the chase target then triggers the chaseStart event.
     */
    @Override
    public void start() {
        super.start();
        movementTask = new MovementTask(getChaseTarget());
        movementTask.create(owner);
        movementTask.start();

        this.owner.getEntity().getEvents().trigger("chaseStart");
    }

    /**
     * Update the chase target each frame based on current position and hover rules.
     */
    @Override
    public void update() {
        movementTask.setTarget(getChaseTarget());
        movementTask.update();
        if (movementTask.getStatus() != Status.ACTIVE) {
            movementTask.start();
        }
    }

    /**
     * Calculate the desired chase position. For flying enemies, maintains a vertical offset above the target.
     * If the chaser is already horizontally aligned, the x-position is held to hover directly above the target.
     */
    private Vector2 getChaseTarget() {
        Vector2 targetPos = target.getPosition().cpy();

        // For flying enemies (drones), maintain a height above the target
        if (hoverHeight > 0) {
            targetPos.y += hoverHeight;

            // If already above target horizontally, just hover
            float horizontalDistance = Math.abs(owner.getEntity().getPosition().x - target.getPosition().x);
            if (horizontalDistance < 1f) {
                // Maintain position above target
                targetPos.x = owner.getEntity().getPosition().x;
            }
        }

        return targetPos;
    }


    /** Stop the task */
    @Override
    public void stop() {
        super.stop();
        if (movementTask != null) {
            movementTask.stop();
        }
    }

    /**
     * Get current priority of the task.
     * If target is within bomb-drop zone, -1, to allow bombing task to start.
     */
    @Override
    public int getPriority() {
        // If player in bomb_drop zone, then set chasing action to -1
        if (isPlayerInDropZone()) {
            return -1;
        }

        // Don't chase until light is activated
        if (!active) {
            return -1;
        }

        // Active task
        if (status == Status.ACTIVE) {
            return getActivePriority();
        }

        // Inactive task (activated but not started yet)
        return getInactivePriority();
    }

    /**
     * Check whether the target is in bomb drop zone relative to the drone.
     * The target is considered in-zone if the drop is a minHeight above the target.
     */
    private boolean isPlayerInDropZone() {
        Vector2 dronePos = owner.getEntity().getCenterPosition();
        Vector2 playerPos = target.getCenterPosition();

        float horizontalDistance = Math.abs(dronePos.x - playerPos.x);
        float verticalDistance = dronePos.y - playerPos.y;

        return verticalDistance >= minHeight && horizontalDistance <= dropRange;
    }

    /** Return the distance from owner to target (with error margin). */
    private float getDistanceToTarget() {
        Vector2 target_position=target.getPosition();
        Vector2 curr_position=owner.getEntity().getPosition();
        float distance= target_position.dst(curr_position);
        float threshold=0.5f;
        if(distance < threshold){
            return 0f;
        }
        return distance;

    }

    /** Get active priority of task */
    private int getActivePriority() {
        float dst = getDistanceToTarget();
        if (dst > maxChaseDistance || !isTargetVisible()) {
            owner.getEntity().getEvents().trigger("chaseEnd");
            return -1; // Too far, stop chasing
        }
        return priority;
    }

    /** Get inactive priority of task */
    private int getInactivePriority() {
        float dst = getDistanceToTarget();
        if (dst < viewDistance && isTargetVisible()) {
            return priority;
        }
        return -1;
    }

    /** Tests line of sight to the target */
    private boolean isTargetVisible() {
        Vector2 from = owner.getEntity().getCenterPosition();
        Vector2 to = target.getCenterPosition();

        // If there is an obstacle in the path to the player, not visible.
        if (physics.raycast(from, to, PhysicsLayer.OBSTACLE, hit)) {
            debugRenderer.drawLine(from, hit.point);
            return false;
        }
        debugRenderer.drawLine(from, to);
        return true;
    }
}
