package com.csse3200.game.components.projectiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ExplosionFactory;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component for laser projectile entities that handles:
 * - Movement in a straight line
 * - Collision detection with player
 * - Damage dealing
 * - Hit effect (touchKillEffect) animation
 * - Auto-disposal after hitting or traveling too far
 */
public class LaserProjectileComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(LaserProjectileComponent.class);

    private static final float MAX_DISTANCE = 50f; // Max travel distance
    private static final float HIT_EFFECT_DURATION = 1.0f; // Duration of hit effect

    private final Vector2 direction;
    private final float speed;
    private final Entity source;
    private final Vector2 startPosition;

    private boolean hasHit = false;
    private float distanceTraveled = 0f;
    private PhysicsComponent physicsComponent;

    /**
     * Creates a laser projectile component
     * @param direction Normalized direction vector
     * @param speed Movement speed
     * @param source The entity that created this projectile (boss)
     */
    public LaserProjectileComponent(Vector2 direction, float speed, Entity source) {
        this.direction = direction.cpy().nor();
        this.speed = speed;
        this.source = source;
        this.startPosition = new Vector2();
    }

    @Override
    public void create() {
        super.create();
        physicsComponent = entity.getComponent(PhysicsComponent.class);

        // Store starting position from physics body (already set correctly in factory)
        if (physicsComponent != null && physicsComponent.getBody() != null) {
            startPosition.set(physicsComponent.getBody().getPosition());

            logger.debug("Laser projectile created at position {} with direction {} and speed {}",
                    startPosition, direction, speed);
        }

        // Physics is already fully configured in the factory
        // No need to modify it here - just register collision listener
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    }

    @Override
    public void update() {
        if (hasHit) {
            return; // Stop updating after hit
        }

        // Calculate distance traveled
        Vector2 currentPos = entity.getCenterPosition();
        distanceTraveled = currentPos.dst(startPosition);

        // Dispose if traveled too far
        if (distanceTraveled >= MAX_DISTANCE) {
            logger.debug("Laser projectile traveled max distance ({}), disposing", distanceTraveled);
            disposeProjectile();
        }
    }

    /**
     * Handles collision with other entities
     * @param me This projectile's fixture
     * @param other The other entity's fixture
     */
    private void onCollisionStart(Fixture me, Fixture other) {
        if (hasHit) {
            return; // Already hit something
        }

        if (other == null || other.getBody() == null) {
            return;
        }

        Object userData = other.getBody().getUserData();
        if (!(userData instanceof BodyUserData bodyData)) {
            return;
        }

        Entity targetEntity = bodyData.entity;
        if (targetEntity == null || targetEntity == source) {
            // Don't hit the boss itself - projectile should pass through source
            logger.debug("Laser projectile ignoring collision with source entity");
            return;
        }

        // Check if hit the player
        if (isPlayer(targetEntity)) {
            onHitPlayer(targetEntity);
        }
        // Check if hit an obstacle/wall
        else if (isObstacle(other)) {
            onHitObstacle();
        }
    }

    /**
     * Checks if the target entity is a player
     * @param target The target entity
     * @return true if target is player
     */
    private boolean isPlayer(Entity target) {
        // Check if entity has hitbox with PLAYER layer
        com.csse3200.game.physics.components.HitboxComponent hitbox =
                target.getComponent(com.csse3200.game.physics.components.HitboxComponent.class);

        if (hitbox == null) {
            return false;
        }

        // Check if the hitbox layer matches PLAYER
        short layer = hitbox.getLayer();
        return (layer & PhysicsLayer.PLAYER) != 0;
    }

    /**
     * Checks if the fixture is an obstacle
     * @param fixture The fixture to check
     * @return true if obstacle
     */
    private boolean isObstacle(Fixture fixture) {
        short category = fixture.getFilterData().categoryBits;
        return (category & PhysicsLayer.OBSTACLE) != 0;
    }

    /**
     * Handles hitting the player:
     * - Deal damage
     * - Show hit effect (touchKillEffect)
     * - Dispose projectile
     */
    private void onHitPlayer(Entity player) {
        if (hasHit) {
            return;
        }

        hasHit = true;
        logger.debug("Laser projectile hit player at {}", entity.getCenterPosition());

        // Get player's combat stats and deal damage
        CombatStatsComponent targetStats = player.getComponent(CombatStatsComponent.class);
        CombatStatsComponent projectileStats = entity.getComponent(CombatStatsComponent.class);

        if (targetStats != null && projectileStats != null) {
            // Deal damage to player
            targetStats.hit(projectileStats);
            // Slow the player by crouch for 2.5 seconds
            player.getEvents().trigger("crouch");
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    player.getEvents().trigger("crouch");
                }
            }, 2.5f);
            logger.debug("Dealt {} damage to player", projectileStats.getBaseAttack());
        }

        // Show hit effect (touchKillEffect animation)
        showHitEffect();

        disposeProjectile();
    }

    /**
     * Handles hitting an obstacle - just dispose
     */
    private void onHitObstacle() {
        if (hasHit) {
            return;
        }

        hasHit = true;
        logger.debug("Laser projectile hit obstacle");
        disposeProjectile();
    }

    /**
     * Shows the hit effect animation using touchKillEffect
     */
    private void showHitEffect() {
        if (entity == null || !entity.isEnabled()) {
            return;
        }

        AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);
        if (animator != null) {
            // Stop movement
            if (physicsComponent != null && physicsComponent.getBody() != null) {
                physicsComponent.getBody().setLinearVelocity(0, 0);
            }

            // Switch to hit effect animation
            logger.debug("Playing touchKillEffect animation");

            // Calculate rotation angle from direction vector
            float rotationAngle = direction.angleDeg();
            // Convert to radians for Box2D
            float rotationRadians = (float) Math.toRadians(rotationAngle);

            // Create black hole effect at impact position (reusing existing effect)
            try {
                float baseOffsetX = 1f;
                float baseOffsetY = 3.6f;
                Vector2 impactPos = (entity.getCenterPosition().cpy());
                impactPos.x += baseOffsetX * Math.cos(rotationRadians);
                impactPos.y += baseOffsetY* Math.sin(rotationRadians);
                Entity hitEffect = ExplosionFactory.createBlackHole(impactPos, 4f);

                if (hitEffect != null) {
                    ServiceLocator.getEntityService().register(hitEffect);

                    // Auto-dispose hit effect after duration
                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            if (hitEffect != null) {
                                hitEffect.dispose();
                            }
                        }
                    }, HIT_EFFECT_DURATION);
                }
            } catch (Exception e) {
                logger.warn("Could not create hit effect: {}", e.getMessage());
            }
        }
    }

    /**
     * Safely disposes the projectile entity.
     * Defers disposal to next frame to avoid physics world lock issues.
     */
    private void disposeProjectile() {
        if (entity != null && entity.isEnabled()) {
            // Disable entity first to stop updates
            entity.setEnabled(false);

            // Defer disposal to next frame (safe from physics callbacks)
            Gdx.app.postRunnable(() -> {
                if (entity != null) {
                    entity.dispose();
                    logger.debug("Laser projectile disposed");
                }
            });
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        logger.debug("LaserProjectileComponent disposed");
    }
}