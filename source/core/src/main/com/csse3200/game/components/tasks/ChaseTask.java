package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 *  Event-activated chase behaviour.
 *  This task is made eligible for scheduling via external calls to activate() (e.g. 'enemyActivated' in EnemyFactory).
 *  The entity chases the target until either:
 *  - The target is farther than maxChaseDistance
 *  - Line of sight is broken for more than LOS_GRACE_MS after initial sighting
 *  To prevent the task from finishing immediately if the entity starts far away/out of LOS, these end conditions
 *  are only applicable after an initial active grace period ends.
 *  Re-activate chase via new calls to activate() only.
 **/
public class ChaseTask extends DefaultTask implements PriorityTask {
    private final Entity target;
    private final float maxChaseDistance;

    private MovementTask movementTask;

    private final PhysicsEngine physics;
    private final DebugRenderer debugRenderer;
    private final RaycastHit hit = new RaycastHit();
    private final GameTime timeSource;

    // Activation
    private boolean active = false;
    private final float activeGracePeriod; // Ignore end conditions for x seconds
    private long endGracePeriod;

    // Avoid LOS flickering: Must be out of LOS for > 250ms after first sighting
    private static final long LOS_GRACE_MS  = 250L;
    private long lastVisibleAt = 0L;
    private boolean hasSeenTarget = false;


    /**
     * Creates a new chase task that will pursue the given target entity
     * @param target entity to chase
     * @param maxChaseDistance threshold where chase ends
     * @param activeGracePeriod time in seconds that entity chases before end conditions apply
     */
    public ChaseTask(Entity target, float maxChaseDistance, float activeGracePeriod) {
        this.target = target;
        this.maxChaseDistance = maxChaseDistance;
        this.activeGracePeriod = activeGracePeriod;
        physics = ServiceLocator.getPhysicsService().getPhysics();
        debugRenderer = ServiceLocator.getRenderService().getDebug();
        timeSource = ServiceLocator.getTimeSource();
    }

    /**
     * Activate the chase task, making it eligible for scheduling.
     * Called by external systems (e.g. 'enemyActivated' in response to security camera)
     */
    public void activate() {
        if (active) return;
        active = true;
        long now = timeSource.getTime();
        endGracePeriod = now + (long)(activeGracePeriod * 1000);
        lastVisibleAt = now;
        hasSeenTarget = false;
    }

    /**
     * Deactivate the chase task, preventing it from being scheduled.
     */
    public void deactivate() {
        if (!active) return;
        active = false;
    }

    /**
     * Initialise and start movement towards target.
     */
    @Override
    public void start() {
        super.start();
        if (!active) return;

        if (movementTask == null) {
            movementTask = new MovementTask(target.getPosition());
            movementTask.create(owner);
        } else {
            movementTask.setTarget(target.getPosition());
        }
        movementTask.start();

        this.owner.getEntity().getEvents().trigger("chaseStart");
    }

    /**
     * Update the chase behaviour each frame.
     */
    @Override
    public void update() {
        if (!active || movementTask == null) return;

        movementTask.setTarget(target.getPosition());
        movementTask.update();
        if (movementTask.getStatus() != Status.ACTIVE) {
            movementTask.start();
        }
    }

    /**
     * Stop the chase behaviour and movement subtask.
     */
    @Override
    public void stop() {
        if (movementTask != null) movementTask.stop();
        deactivate();
        super.stop();

        this.owner.getEntity().getEvents().trigger("chaseEnd");
    }

    /**
     * Get the current priority of the task. Enables task handover if end conditions are met.
     * @return -1 if inactive or end conditions are met. Otherwise, 10 while active.
     */
    @Override
    public int getPriority() {
        if (!active) return -1;

        long now = timeSource.getTime();

        if (isTargetVisible()) {
            hasSeenTarget = true;
            lastVisibleAt = now;
        }

        if (now >= endGracePeriod) {
            float dst = getDistanceToTarget();
            boolean lostLos = hasSeenTarget && (now - lastVisibleAt) > LOS_GRACE_MS;
            if (dst > maxChaseDistance || lostLos) return -1;
        }

        return 10;
    }

    /**
     * Get the distance from owner to target
     * @return (float) distance in world units
     */
    private float getDistanceToTarget() {
        return owner.getEntity().getPosition().dst(target.getPosition());
    }

    /**
     * Checks whether the target is in line of sight
     * @return true if there is a LOS between the owner and target. Otherwise, false.
     */
    private boolean isTargetVisible() {
        Vector2 from = owner.getEntity().getCenterPosition();
        Vector2 to = target.getCenterPosition();

        if (physics.raycast(from, to, PhysicsLayer.OBSTACLE, hit)) {
            debugRenderer.drawLine(from, hit.point);
            return false;
        }
        debugRenderer.drawLine(from, to);
        return true;
    }
}