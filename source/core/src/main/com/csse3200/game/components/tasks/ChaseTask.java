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

    private final PhysicsEngine physics;
    private final DebugRenderer debugRenderer;
    private final RaycastHit hit = new RaycastHit();
    private MovementTask movementTask;
    private boolean active = false;

    /**
     * @param target The entity to chase.
     */
    public ChaseTask(Entity target) {
        this.target = target;
        physics = ServiceLocator.getPhysicsService().getPhysics();
        debugRenderer = ServiceLocator.getRenderService().getDebug();
    }

    public void activate() {
        if (active) return;
        active = true;
    }

    public void deactivate() {
        if (!active) return;
        active = false;
        owner.getEntity().getEvents().trigger("chaseEnd");
    }

    @Override
    public void start() {
        super.start();

        if (movementTask == null) {
            movementTask = new MovementTask(target.getPosition());
            movementTask.create(owner);
        } else {
            movementTask.setTarget(target.getPosition());
        }

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
        if (movementTask != null) movementTask.stop();
        super.stop();
    }

    @Override
    public int getPriority() {
        return active ? 10 : -1;
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