package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.projectiles.BombComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.BaseEntityConfig;
import com.csse3200.game.entities.configs.ProjectileConfigs;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Factory to create projectile entities like bombs, bullets, etc.
 */
public class ProjectileFactory {

    /**
     * Creates a bomb entity that explodes after a delay
     * @param source The entity that created the bomb
     * @param targetPosition Position where the bomb should be placed
     * @param explosionDelay Time in seconds before explosion
     * @param explosionRadius Radius of explosion damage area
     * @param damage Damage the bomb will deal
     * @return Bomb entity
     */
    public static Entity createBomb(Entity source, Vector2 targetPosition, float explosionDelay,
                                    float explosionRadius, int damage) {
        Entity bomb = createProjectile(source, new Vector2(0,-1), 3,4);

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/drone.atlas", TextureAtlas.class));

        // Add bomb animations
        animator.addAnimation("bomb", 0.1f, Animation.PlayMode.LOOP);

        bomb
                .addComponent(animator)
                .addComponent(new CombatStatsComponent(1, damage)) // 1 health, specified damage
                .addComponent(new BombComponent(explosionDelay, explosionRadius, PhysicsLayer.PLAYER));

        // Set bomb position
        bomb.setPosition(targetPosition);

        // Set up physics - make it a dynamic body so it can fall/move
        bomb.getComponent(PhysicsComponent.class).setBodyType(BodyDef.BodyType.DynamicBody);

        // Scale the bomb appropriately
        PhysicsUtils.setScaledCollider(bomb, 1.0f, 1.0f);

        // Make the bomb render properly
        bomb.getComponent(AnimationRenderComponent.class).scaleEntity();

        return bomb;
    }

    /**
     * Creates a simple projectile (for potential future use)
     * @param source The entity that created the projectile
     * @param direction Direction the projectile should travel
     * @param speed Speed of the projectile
     * @param damage Damage the projectile deals
     * @return Projectile entity
     */
    public static Entity createProjectile(Entity source, Vector2 direction, float speed, int damage) {
        Entity projectile = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                .addComponent(new CombatStatsComponent(1, damage));

        // Set initial position at source
        projectile.setPosition(source.getPosition());

        // Set up physics for movement
        projectile.getComponent(PhysicsComponent.class).setBodyType(BodyDef.BodyType.DynamicBody);

        // Apply initial velocity
        Vector2 velocity = direction.nor().scl(speed);
        projectile.getComponent(PhysicsComponent.class).getBody().setLinearVelocity(velocity);

        // Scale appropriately
        PhysicsUtils.setScaledCollider(projectile, 1.0f, 1.0f);

        return projectile;
    }

    private ProjectileFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}