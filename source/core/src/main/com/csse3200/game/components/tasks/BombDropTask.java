package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task that makes drone drop bombs when player is directly below.
 * Higher priority than chase when conditions are met.
 */
public class BombDropTask extends DefaultTask implements PriorityTask {
    private static final Logger logger = LoggerFactory.getLogger(BombDropTask.class);

    private final Entity target;
    private final int priority;
    private final float dropRange;
    private final float minHeight;
    private final float cooldown;
    private final GameTime timeSource;
    private long lastDropTime = 0;
    private boolean isAttacking = false;
    private float attackTimer = 0f;
    private static final float BOMB_RELEASE_DELAY = 0.25f;
    private static final float HYSTERESIS = 0.3f; // Adding hysteresis areas to prevent frequent switching

    private boolean wasInDropZone = false; // record whether it was in the bomb drop zone before

    public BombDropTask(Entity target, int priority, float dropRange, float minHeight, float cooldown) {
        this.target = target;
        this.priority = priority;
        this.dropRange = dropRange;
        this.minHeight = minHeight;
        this.cooldown = cooldown;
        this.timeSource = ServiceLocator.getTimeSource();
    }

    @Override
    public int getPriority() {
        boolean inDropZone = isPlayerInDropZone();

        // Use lag logic to prevent frequent switching
        if (!wasInDropZone && inDropZone) {
            // Enter the bomb drop zone
            wasInDropZone = true;
        } else if (wasInDropZone && !isPlayerInExtendedDropZone()) {
            // Exit bomb drop mode only if you leave the extended area completely
            wasInDropZone = false;
        }

        // Return to high priority only in the bomb drop zone and cooling is completed
        if (wasInDropZone && canDrop()) {
            return priority;
        }
        return -1;
    }

    @Override
    public void start() {
        super.start();
        owner.getEntity().getEvents().trigger("dropStart");
        logger.debug("Starting bomb drop ATTACK MODE");
        isAttacking = false;
        attackTimer = 0f;
    }

    @Override
    public void update() {
        if (isAttacking) {
            attackTimer += timeSource.getDeltaTime();
            if (attackTimer >= BOMB_RELEASE_DELAY) {
                dropBomb();
                lastDropTime = timeSource.getTime();
                isAttacking = false;
                attackTimer = 0f;
            }
        } else if (canDrop() && isPlayerInDropZone()) {
            // Attack only if the player is indeed in the bomb drop zone
            isAttacking = true;
            attackTimer = 0f;
            owner.getEntity().getEvents().trigger("dropStart");
            logger.debug("Starting attack sequence.");
        }
    }

    @Override
    public void stop() {
        super.stop();
        isAttacking = false;
        attackTimer = 0f;
        logger.debug("Stopping bomb drop task.");
    }

    private boolean isPlayerInDropZone() {
        Vector2 dronePos = owner.getEntity().getCenterPosition();
        Vector2 playerPos = target.getCenterPosition();

        float horizontalDistance = Math.abs(dronePos.x - playerPos.x);
        float verticalDistance = dronePos.y - playerPos.y;

        return verticalDistance >= minHeight && horizontalDistance <= dropRange;
    }

    private boolean isPlayerInExtendedDropZone() {
        // Extended bomb drop zone for lag judgment
        Vector2 dronePos = owner.getEntity().getCenterPosition();
        Vector2 playerPos = target.getCenterPosition();

        float horizontalDistance = Math.abs(dronePos.x - playerPos.x);
        float verticalDistance = dronePos.y - playerPos.y;

        return verticalDistance >= (minHeight - HYSTERESIS) &&
                horizontalDistance <= (dropRange + HYSTERESIS);
    }

    private boolean canDrop() {
        return timeSource.getTimeSince(lastDropTime) >= cooldown * 1000;
    }

    private void dropBomb() {
        Vector2 dronePos = owner.getEntity().getPosition().cpy();
        Vector2 bombSpawnPos = new Vector2(
                dronePos.x + owner.getEntity().getScale().x / 2,
                dronePos.y
        );

        Entity bomb = ProjectileFactory.createBomb(
                owner.getEntity(),
                bombSpawnPos,
                target.getPosition().cpy(),
                2.0f,  // explosion delay
                2.5f,  // explosion radius
                50     // damage
        );

        ServiceLocator.getEntityService().register(bomb);
        logger.debug("Bomb dropped at {}", bombSpawnPos);
    }
}