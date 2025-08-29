package com.csse3200.game.components.projectiles;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component for bomb projectiles that explode after a delay and deal area damage.
 * Requires CombatStatsComponent and HitboxComponent on this entity.
 */
public class BombComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(BombComponent.class);

    private final GameTime timeSource;
    private final float explosionDelay; // in seconds
    private final float explosionRadius;
    private final short targetLayer;
    private long dropTime;
    private boolean hasExploded = false;
    private CombatStatsComponent combatStats;
    private HitboxComponent hitboxComponent;

    /**
     * Create a bomb component with specified parameters
     * @param explosionDelay Time in seconds before explosion
     * @param explosionRadius Radius of explosion damage area
     * @param targetLayer Physics layer of entities to damage
     */
    public BombComponent(float explosionDelay, float explosionRadius, short targetLayer) {
        this.timeSource = ServiceLocator.getTimeSource();
        this.explosionDelay = explosionDelay;
        this.explosionRadius = explosionRadius;
        this.targetLayer = targetLayer;
    }

    @Override
    public void create() {
        super.create();
        combatStats = entity.getComponent(CombatStatsComponent.class);
        hitboxComponent = entity.getComponent(HitboxComponent.class);
        dropTime = timeSource.getTime();

        // Add collision listener to handle ground impact
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);

        logger.debug("Bomb created with {}s delay and {}m radius", explosionDelay, explosionRadius);
    }

    @Override
    public void update() {
        if (!hasExploded && timeSource.getTimeSince(dropTime) >= explosionDelay * 1000) {
            explode();
        }
    }

    private void onCollisionStart(Fixture me, Fixture other) {
        // If bomb hits the ground or an obstacle, start countdown immediately
        if (PhysicsLayer.contains(PhysicsLayer.OBSTACLE, other.getFilterData().categoryBits)) {
            logger.debug("Bomb hit ground/obstacle, starting explosion countdown");
            // Could add visual/audio feedback here
        }
    }

    private void explode() {
        hasExploded = true;
        logger.debug("Bomb exploding at position {}", entity.getPosition());

        // Trigger explosion event for visual/audio effects
        entity.getEvents().trigger("bombExplode");

        // Deal area damage
        dealAreaDamage();

        // Remove the bomb entity after explosion
        entity.dispose();
    }

    private void dealAreaDamage() {
        Vector2 bombPosition = entity.getCenterPosition();

        // Get all entities in the game world
        // This is a simplified approach - in a real game you might use spatial partitioning
        ServiceLocator.getPhysicsService().getPhysics().getWorld().QueryAABB(
                (fixture) -> {
                    if (!PhysicsLayer.contains(targetLayer, fixture.getFilterData().categoryBits)) {
                        return true; // Continue querying
                    }

                    Body body = fixture.getBody();
                    Entity targetEntity = ((BodyUserData) body.getUserData()).entity;
                    Vector2 targetPosition = targetEntity.getCenterPosition();

                    float distance = bombPosition.dst(targetPosition);
                    if (distance <= explosionRadius) {
                        // Deal damage to target
                        CombatStatsComponent targetStats = targetEntity.getComponent(CombatStatsComponent.class);
                        if (targetStats != null && combatStats != null) {
                            targetStats.hit(combatStats);
                            logger.debug("Bomb damaged {} at distance {}", targetEntity, distance);
                        }

                        // Apply knockback
                        PhysicsComponent physicsComponent = targetEntity.getComponent(PhysicsComponent.class);
                        if (physicsComponent != null) {
                            Vector2 knockbackDirection = targetPosition.cpy().sub(bombPosition).nor();
                            float knockbackForce = Math.max(0, explosionRadius - distance) * 5f; // Scale with distance
                            Vector2 impulse = knockbackDirection.scl(knockbackForce);
                            physicsComponent.getBody().applyLinearImpulse(impulse, body.getWorldCenter(), true);
                        }
                    }
                    return true; // Continue querying
                },
                bombPosition.x - explosionRadius, bombPosition.y - explosionRadius,
                bombPosition.x + explosionRadius, bombPosition.y + explosionRadius
        );
    }
}