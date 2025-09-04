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

/** Chases a target entity until they get too far away or line of sight is lost */
public class ChaseTask extends DefaultTask implements PriorityTask {
    private final Entity target;
    private final int priority;
    private final float viewDistance;
    private final float maxChaseDistance;
    private final PhysicsEngine physics;
    private final DebugRenderer debugRenderer;
    private final RaycastHit hit = new RaycastHit();
    private MovementTask movementTask;
    private boolean active = false;
    private boolean triggerMaxChase = false; // whether we start enforcing maxChaseDistance
    private final float triggerDistance = 4f; // distance at which maxChaseDistance starts counting


    /**
     * @param target The entity to chase.
     * @param priority Task priority when chasing (0 when not chasing).
     * @param viewDistance Maximum distance from the entity at which chasing can start.
     * @param maxChaseDistance Maximum distance from the entity while chasing before giving up.
     */
    public ChaseTask(Entity target, int priority, float viewDistance, float maxChaseDistance) {
        this.target = target;
        this.priority = priority;
        this.viewDistance = viewDistance;
        this.maxChaseDistance = maxChaseDistance;
        physics = ServiceLocator.getPhysicsService().getPhysics();
        debugRenderer = ServiceLocator.getRenderService().getDebug();
    }
    public void activate() {
        active = true;
        triggerMaxChase = false; // reset so first chase ignores distance
        if (status != Status.ACTIVE) {
            status = Status.INACTIVE;
        }


    }

    public void deactivate() {

        active = false;
        owner.getEntity().getEvents().trigger("chaseEnd");
    }

    @Override
    public void start() {
        super.start();
        movementTask = new MovementTask(target.getPosition());
        movementTask.create(owner);
        movementTask.start();


        this.owner.getEntity().getEvents().trigger("chaseStart");
    }

    @Override
    public void update() {
        movementTask.setTarget(target.getPosition());
        movementTask.update();
        if (movementTask.getStatus() != Status.ACTIVE) {
            movementTask.start();
        }

    }

    @Override
    public void stop() {
        super.stop();
        movementTask.stop();
    }

    @Override
    public int getPriority() {
        if (status == Status.ACTIVE) {
            return getActivePriority();
        }
        return getInactivePriority();
    }


    // Calculates the current distance between the drone and its target
    private float getDistanceToTarget() {
        return target.getPosition().dst(owner.getEntity().getPosition());
    }

    // In getInactivePriority, only return priority if active
    private int getInactivePriority() {
        if (active) {
            // Activated by security light, start chasing
            return priority;
        }
        return -1; // Not active, don't chase
    }

    // In getActivePriority, also check active
    private int getActivePriority() {
        if (!active) return -1;

        float dst = getDistanceToTarget();

        // Check if we are close enough to start enforcing maxChaseDistance
        if (!triggerMaxChase && dst <= triggerDistance) {
            triggerMaxChase = true;
        }

        // Only stop chasing if we are enforcing maxChaseDistance
        if (triggerMaxChase && dst > maxChaseDistance) {
            deactivate();
            return -1;
        }

        // Otherwise keep chasing
        return priority;
    }

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