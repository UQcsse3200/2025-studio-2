package com.csse3200.game.components.boss;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BossLaserAttackComponent handles the boss's laser pulse attack:
 * - Finds player position
 * - Triggers bossShootLaser animation
 * - Creates and shoots laser pulse projectile with shootEffect animation
 * - Handles hit detection and damage through the projectile entity
 * - Uses touchKillEffect as hit effect when player is damaged
 */
public class BossLaserAttackComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(BossLaserAttackComponent.class);

    private static final float ATTACK_COOLDOWN = 7f;  // seconds between laser shots
    private static final float ANIMATION_DELAY = 0.6f; // delay to sync projectile with animation
    private static final int LASER_DAMAGE = 20;
    private static final float LASER_SPEED = 15f;

    private float cooldownTimer = 0f;
    private Entity target;  // player entity
    private boolean canAttack = false;

    /**
     * Creates a new BossLaserAttackComponent
     * @param target The player entity to target
     */
    public BossLaserAttackComponent(Entity target) {
        this.target = target;
    }

    @Override
    public void create() {
        super.create();
        cooldownTimer = 0f;
        canAttack = false;
        logger.debug("BossLaserAttackComponent created for entity {}", entity.getId());
    }

    @Override
    public void update() {
        if (target == null || !target.isEnabled()) {
            return;
        }

        float delta = ServiceLocator.getTimeSource().getDeltaTime();
        cooldownTimer += delta;

        // Check if cooldown has elapsed
        if (cooldownTimer >= ATTACK_COOLDOWN) {
            cooldownTimer = 0f;
            shootLaser();
        }
    }

    /**
     * Initiates the laser shooting sequence:
     * 1. Find player position
     * 2. Trigger animation
     * 3. Spawn laser projectile after delay
     */
    private void shootLaser() {
        // Trigger bossShootLaser animation
        entity.getEvents().trigger("shootLaserStart");

        // Schedule projectile spawn to sync with animation
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                spawnLaserProjectile();
            }
        }, ANIMATION_DELAY);
    }

    /**
     * Creates and launches the laser projectile entity
     */
    private void spawnLaserProjectile() {
        // Calculate direction from boss to player
        Vector2 playerPos = target.getCenterPosition().cpy();
        Vector2 bossPos = entity.getCenterPosition().cpy();
        Vector2 direction = playerPos.sub(bossPos).nor();

        // Create laser projectile using factory
        Entity laserProjectile = ProjectileFactory.createLaserProjectile(
                entity,           // source (boss)
                bossPos.cpy(),    // spawn position
                direction,        // direction
                LASER_SPEED,      // speed
                LASER_DAMAGE      // damage
        );

        // Register the projectile entity
        ServiceLocator.getEntityService().register(laserProjectile);

        logger.debug("Laser projectile spawned at {} moving in direction {}",
                bossPos, direction);
    }

    /**
     * Enables or disables laser attacks
     * @param enabled Whether laser attacks should be enabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.canAttack = enabled;
        if (!enabled) {
            cooldownTimer = 0f;
        }
    }

    /**
     * Sets the target entity (player)
     * @param newTarget The new target entity
     */
    public void setTarget(Entity newTarget) {
        this.target = newTarget;
    }

    /**
     * Gets the current target entity
     * @return The target entity
     */
    public Entity getTarget() {
        return target;
    }
}