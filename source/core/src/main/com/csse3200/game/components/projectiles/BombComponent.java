package com.csse3200.game.components.projectiles;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.DisposalComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ExplosionFactory;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BombComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(BombComponent.class);

    private final GameTime timeSource;
    private final float explosionDelay;
    private final float explosionRadius;
    private final short targetLayer;
    private long dropTime;
    private boolean hasExploded = false;
    private boolean hasLanded = false;
    private float blinkTimer = 0f;
    private boolean isVisible = true;

    public BombComponent(float explosionDelay, float explosionRadius, short targetLayer) {
        this.timeSource = ServiceLocator.getTimeSource();
        this.explosionDelay = explosionDelay;
        this.explosionRadius = explosionRadius;
        this.targetLayer = targetLayer;
    }

    @Override
    public void create() {
        super.create();
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        dropTime = timeSource.getTime();
        logger.debug("Bomb created with {}s delay", explosionDelay);
        // Add disposal component for safe cleanup after 0.1 seconds
        entity.addComponent(new DisposalComponent(0.1f));
    }

    @Override
    public void update() {
        if (hasExploded) return;

        // Visual warning: make bomb blink faster as it approaches explosion
        float timeLeft = explosionDelay - (timeSource.getTimeSince(dropTime) / 1000f);
        if (timeLeft < 1f && hasLanded) {
            // Blink effect in last second
            blinkTimer += timeSource.getDeltaTime();
            if (blinkTimer > 0.1f) {
                toggleVisibility();
                blinkTimer = 0f;
            }
        }

        // Check for explosion
        if (timeSource.getTimeSince(dropTime) >= explosionDelay * 1000) {
            explode();
        }
    }

    private void onCollisionStart(Fixture me, Fixture other) {
        // Bomb has hit the ground or obstacle
        if (PhysicsLayer.contains(PhysicsLayer.OBSTACLE, other.getFilterData().categoryBits)) {
            hasLanded = true;
            // Stop horizontal movement when landed
            PhysicsComponent physicsComponent = entity.getComponent(PhysicsComponent.class);
            if (physicsComponent != null) {
                Body body = physicsComponent.getBody();
                body.setLinearVelocity(0, body.getLinearVelocity().y);
            }
            logger.debug("Bomb landed, starting countdown");
        }
    }

    private void toggleVisibility() {
        isVisible = !isVisible;
        if (!isVisible) {
            entity.setScale(0f, 0f);
        } else {
            entity.setScale(0.5f, 0.5f);
        }
    }

    private void explode() {
        if (hasExploded) return;
        hasExploded = true;
        logger.debug("Bomb exploding at position {}", entity.getPosition());

        // Deal area damage
        dealAreaDamage();

        // Create visual explosion effect
        createExplosionEffect();

        // Hide the bomb immediately
        entity.setScale(0f, 0f);

        // Start disposal countdown
        entity.getEvents().trigger("scheduleDisposal");

        // Disable this component to prevent further updates
        this.setEnabled(false);
    }

    private void dealAreaDamage() {
        Vector2 bombPos = entity.getCenterPosition();

        ServiceLocator.getPhysicsService().getPhysics().getWorld().QueryAABB(
                (fixture) -> {
                    if (!PhysicsLayer.contains(targetLayer, fixture.getFilterData().categoryBits)) {
                        return true;
                    }

                    Body body = fixture.getBody();
                    BodyUserData userData = (BodyUserData) body.getUserData();
                    if (userData == null || userData.entity == null) {
                        return true;
                    }

                    Entity target = userData.entity;
                    Vector2 targetPos = target.getCenterPosition();

                    float distance = bombPos.dst(targetPos);
                    if (distance <= explosionRadius) {
                        // Damage falloff based on distance
                        float damageFactor = 1f - (distance / explosionRadius) * 0.5f;

                        CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
                        CombatStatsComponent bombStats = entity.getComponent(CombatStatsComponent.class);

                        if (targetStats != null && bombStats != null) {
                            int finalDamage = (int)(bombStats.getBaseAttack() * damageFactor);
                            targetStats.addHealth(-finalDamage);
                            logger.debug("Bomb dealt {} damage to {}", finalDamage, target);
                        }

                        // Apply knockback
                        PhysicsComponent physics = target.getComponent(PhysicsComponent.class);
                        if (physics != null) {
                            Vector2 knockback = targetPos.cpy().sub(bombPos).nor();
                            float force = (explosionRadius - distance) * 10f;
                            knockback.scl(force);
                            physics.getBody().applyLinearImpulse(knockback,
                                    physics.getBody().getWorldCenter(), true);
                        }
                    }
                    return true;
                },
                bombPos.x - explosionRadius, bombPos.y - explosionRadius,
                bombPos.x + explosionRadius, bombPos.y + explosionRadius
        );
    }

    private void createExplosionEffect() {
        Entity explosion = ExplosionFactory.createExplosion(entity.getCenterPosition(), this.explosionRadius);
        ServiceLocator.getEntityService().register(explosion);

        logger.debug("Created explosion effect entity at {}", entity.getCenterPosition());
    }
}