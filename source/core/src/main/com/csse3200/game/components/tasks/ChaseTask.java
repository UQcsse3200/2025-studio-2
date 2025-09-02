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
    private final float hoverHeight; // Height to maintain above target
    private final PhysicsEngine physics;
    private final DebugRenderer debugRenderer;
    private final RaycastHit hit = new RaycastHit();
    private MovementTask movementTask;
    private final float dropRange;
    private final float minHeight;

    /**
     * @param target The entity to chase.
     * @param priority Task priority when chasing (0 when not chasing).
     * @param viewDistance Maximum distance from the entity at which chasing can start.
     * @param maxChaseDistance Maximum distance from the entity while chasing before giving up.
     * @param hoverHeight Preferred height to maintain above target (for flying enemies)
     */
    public ChaseTask(Entity target, int priority, float viewDistance, float maxChaseDistance, float hoverHeight,
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

    // Original constructor for backward compatibility
    public ChaseTask(Entity target, int priority, float viewDistance, float maxChaseDistance) {
        this(target, priority, viewDistance, maxChaseDistance, 0f, 1.5f, 2f);
    }

    @Override
    public void start() {
        super.start();
        movementTask = new MovementTask(getChaseTarget());
        movementTask.create(owner);
        movementTask.start();

        this.owner.getEntity().getEvents().trigger("chaseStart");
    }

    @Override
    public void update() {
        movementTask.setTarget(getChaseTarget());
        movementTask.update();
        if (movementTask.getStatus() != Status.ACTIVE) {
            movementTask.start();
        }
    }

    /**
     * Calculate chase target position - for flying enemies, maintain height above target
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


    @Override
    public void stop() {
        super.stop();
        if (movementTask != null) {
            movementTask.stop();
        }
    }

    @Override
    public int getPriority() {
        // If player in bomb_drop zone, then set chasing action to -1
        if (isPlayerInDropZone()) {
            return -1;
        }

        if (status == Status.ACTIVE) {
            return getActivePriority();
        }
        return getInactivePriority();
    }

    private boolean isPlayerInDropZone() {
        Vector2 dronePos = owner.getEntity().getCenterPosition();
        Vector2 playerPos = target.getCenterPosition();

        float horizontalDistance = Math.abs(dronePos.x - playerPos.x);
        float verticalDistance = dronePos.y - playerPos.y;

        return verticalDistance >= minHeight && horizontalDistance <= dropRange;
    }


  /**
   *
   * @return
   */

  //updated this function to stop chasing once the player is in threshold
  private float getDistanceToTarget() {
    Vector2 target_position=target.getPosition();
    Vector2 curr_position=owner.getEntity().getPosition();
    float distancee= target_position.dst(curr_position);
    float threshold=0.5f;
    if(distancee < threshold){
      return 0f;
    }
    return distancee;

  }

  private int getActivePriority() {
    float dst = getDistanceToTarget();
    if (dst > maxChaseDistance || !isTargetVisible()) {
      owner.getEntity().getEvents().trigger("chaseEnd");
      return -1; // Too far, stop chasing
    }
    return priority;
  }

  private int getInactivePriority() {
    float dst = getDistanceToTarget();
    if (dst < viewDistance && isTargetVisible()) {
      return priority;
    }
    return -1;
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
