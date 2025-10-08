package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.ai.tasks.TaskRunner;
import com.csse3200.game.components.enemy.BombTrackerComponent;
import com.csse3200.game.components.lighting.ConeDetectorComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task that makes drone drop bombs when the bomber's cone light detects a target below.
 * Works in conjunction with ConeDetectorComponent for detection.
 */
public class BombDropTask extends DefaultTask implements PriorityTask {
    private static final Logger logger = LoggerFactory.getLogger(BombDropTask.class);

    private final Entity target;
    private final int priority;
    private final float cooldown;
    private final float optimalHeight;  // Optimal height for dropping bombs
    private final float heightTolerance; // Tolerance for height positioning

    private final GameTime timeSource;
    private ConeDetectorComponent detectorComponent;
    private long lastDropTime = 0;
    private boolean isAttacking = false;
    private float attackTimer = 0f;
    private static final float BOMB_RELEASE_DELAY = 0.25f;

    // State tracking
    private boolean targetDetected = false;

    /**
     * Create a bomb dropping task that works with light detection.
     * @param target player or other entity to attack
     * @param priority priority of task
     * @param cooldown min time between consecutive drops in seconds
     * @param optimalHeight optimal height above target for dropping bombs
     * @param heightTolerance tolerance for height positioning
     */
    public BombDropTask(Entity target, int priority, float cooldown,
                        float optimalHeight, float heightTolerance) {
        this.target = target;
        this.priority = priority;
        this.cooldown = cooldown;
        this.optimalHeight = optimalHeight;
        this.heightTolerance = heightTolerance;
        this.timeSource = ServiceLocator.getTimeSource();
    }

    @Override
    public void create(TaskRunner owner) {
        super.create(owner);
        // Get the bomber's detector component
        detectorComponent = owner.getEntity().getComponent(ConeDetectorComponent.class);

        // Listen for bomber-specific events (triggered by EnemyFactory setup)
        owner.getEntity().getEvents().addListener("bomberTargetAcquired", () -> {
            targetDetected = true;
            logger.debug("Bomber acquired target through cone light detection");
        });

        owner.getEntity().getEvents().addListener("bomberTargetLost", () -> {
            targetDetected = false;
            logger.debug("Bomber lost target");
        });
    }

    /**
     * Computes scheduling priority based on light detection and positioning.
     * @return priority of the task
     */
    @Override
    public int getPriority() {
        // Only activate if:
        // 1. Cone detector has detected target
        // 2. Bomber is at appropriate height
        // 3. Cooldown has elapsed
        if (detectorComponent != null && detectorComponent.isDetected() &&
                isAtBombingHeight() && canDrop()) {
            return priority;
        }
        return -1;
    }

    /**
     * Start drone attack and trigger a dropStart event on owner entity
     */
    @Override
    public void start() {
        super.start();
        owner.getEntity().getEvents().trigger("dropStart");
        logger.debug("Starting bomb drop - target detected by cone light");
        isAttacking = false;
        attackTimer = 0f;
    }

    /**
     * Advance attack sequence when attacking
     */
    @Override
    public void update() {
        // If target moves out of light cone, stop attack
        if (detectorComponent != null && !detectorComponent.isDetected()) {
            logger.debug("Target lost during attack sequence");
            stop();
            return;
        }

        if (isAttacking) {
            attackTimer += timeSource.getDeltaTime();
            if (attackTimer >= BOMB_RELEASE_DELAY) {
                dropBomb();
                lastDropTime = timeSource.getTime();
                isAttacking = false;
                attackTimer = 0f;
            }
        } else if (canDrop() && detectorComponent.isDetected()) {
            // Start attack sequence
            isAttacking = true;
            attackTimer = 0f;
            owner.getEntity().getEvents().trigger("dropStart");
            logger.debug("Initiating bomb drop sequence");
        }
    }

    /**
     * Stop attack task
     */
    @Override
    public void stop() {
        super.stop();
        isAttacking = false;
        attackTimer = 0f;
        owner.getEntity().getEvents().trigger("dropEnd");
        logger.debug("Stopping bomb drop task");
    }

    /**
     * Check if bomber is at appropriate height for bombing.
     * Also checks that target is relatively below the bomber (not to the side).
     * @return true if at correct height and position
     */
    private boolean isAtBombingHeight() {
        Vector2 bomberPos = owner.getEntity().getCenterPosition();
        Vector2 targetPos = target.getCenterPosition();

        // Check vertical positioning
        float heightDiff = bomberPos.y - targetPos.y;
        if (Math.abs(heightDiff - optimalHeight) > heightTolerance) {
            return false;
        }

        // Check horizontal alignment (bomber should be relatively above target)
        float horizontalDistance = Math.abs(bomberPos.x - targetPos.x);
        return horizontalDistance < 2f; // Within 2 units horizontally
    }

    /**
     * Determine whether cooldown period has elapsed since last drop
     */
    private boolean canDrop() {
        return timeSource.getTimeSince(lastDropTime) >= cooldown * 1000;
    }

    /**
     * Spawn a bomb entity at the bomber's current position
     */
    private void dropBomb() {
        Vector2 origin = owner.getEntity().getCenterPosition();
        Vector2 bombSpawnCenter = new Vector2(origin.x, origin.y - 0.5f); // Drop below bomber

        Entity bomb = ProjectileFactory.createBomb(
                owner.getEntity(),
                bombSpawnCenter,
                target.getPosition().cpy(),
                2.0f,  // explosion delay
                2f,  // explosion radius
                30     // damage
        );

        ServiceLocator.getEntityService().register(bomb);
        BombTrackerComponent bomberComp = owner.getEntity().getComponent(BombTrackerComponent.class);
        if (bomberComp != null) {
            bomberComp.trackBomb(bomb);
        }

        logger.debug("Bomb dropped at {} - target detected by cone light", bombSpawnCenter);
    }

    /**
     * Check if target is currently detected.
     * @return true if target is detected
     */
    public boolean isTargetDetected() {
        return targetDetected;
    }
}