package com.csse3200.game.components.tasks;

import com.badlogic.gdx.Gdx;
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
 * Task that makes drone automatically drop bombs at regular intervals while patrolling.
 * This task runs in the background and doesn't interrupt other tasks - it just
 * drops bombs on a timer regardless of what else the drone is doing.
 */
public class AutoBombDropTask extends DefaultTask implements PriorityTask {
    private static final Logger logger = LoggerFactory.getLogger(AutoBombDropTask.class);

    private final Entity target;
    private final float cooldown;

    private final GameTime timeSource;
    private long lastDropTime = 0;

    /**
     * Create an automatic bomb dropping task.
     * @param target reference entity (usually player) for bomb targeting
     * @param cooldown minimum time between consecutive drops in seconds
     */
    public AutoBombDropTask(Entity target, float cooldown) {
        this.target = target;
        this.cooldown = cooldown;
        this.timeSource = ServiceLocator.getTimeSource();
        // Initialize lastDropTime to allow immediate first drop
        this.lastDropTime = timeSource.getTime() - (long)(cooldown * 1000);
    }

    /**
     * Returns priority 0 when cooldown has elapsed, -1 otherwise.
     * Priority 0 is lower than patrol (1), so patrol stays active.
     * Bombs drop via update() being called by the task system.
     * @return 0 when ready to drop, -1 otherwise
     */
    @Override
    public int getPriority() {
        // Always return 0 so this task is always considered but never interrupts patrol
        return 0;
    }

    /**
     * Initialize the task
     */
    @Override
    public void start() {
        super.start();
        logger.debug("Auto bomb drop task started - will drop bombs every {} seconds", cooldown);
    }

    /**
     * Check cooldown and drop bomb when task becomes active
     */
    @Override
    public void update() {
        // Drop bomb immediately when this task becomes active
        dropBomb();
        lastDropTime = timeSource.getTime();

        // Finish immediately so patrol can resume
        status = Status.FINISHED;
    }

    /**
     * Stop the task
     */
    @Override
    public void stop() {
        super.stop();
        logger.debug("Stopping automatic bomb drop task");
    }

    /**
     * Spawn a bomb entity at the drone's current position
     */
    private void dropBomb() {
        Vector2 dronePos = owner.getEntity().getPosition().cpy();
        Vector2 bombSpawnPos = new Vector2(
                dronePos.x + owner.getEntity().getScale().x / 2,
                dronePos.y - 0.5f  // Spawn slightly below drone
        );

        // Target position - aim towards player or straight down
        Vector2 targetPos = target != null ? target.getPosition().cpy() :
                new Vector2(bombSpawnPos.x, bombSpawnPos.y - 5f);

        // Defer entity registration to the next frame to avoid nested iteration
        Gdx.app.postRunnable(() -> {
            Entity bomb = ProjectileFactory.createBomb(
                    owner.getEntity(),
                    bombSpawnPos,
                    targetPos,
                    2.0f,  // explosion delay
                    2f,    // explosion radius
                    25     // damage
            );

            ServiceLocator.getEntityService().register(bomb);
            logger.debug("Auto-bomb dropped at {}", bombSpawnPos);
        });
    }
}