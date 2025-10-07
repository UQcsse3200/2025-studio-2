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
     * Creates a bomb entity that falls from source position toward target
     * @param source The entity that created the bomb (drone)
     * @param spawnCenter Position where bomb spawns (drone's position)
     * @param targetPosition Position where bomb should land (player's position)
     * @param explosionDelay Time in seconds before explosion after landing
     * @param explosionRadius Radius of explosion damage area
     * @param damage Damage the bomb will deal
     * @return Bomb entity
     */
    public static Entity createBomb(Entity source, Vector2 spawnCenter, Vector2 targetPosition,
                                    float explosionDelay, float explosionRadius, int damage) {
        Entity bomb = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.NPC))
                .addComponent(new CombatStatsComponent(1, damage))
                .addComponent(new BombComponent(explosionDelay, explosionRadius, PhysicsLayer.PLAYER));

        // Bomb size
        final float SIZE_W = 0.5f;
        final float SIZE_H = 0.5f;

        TextureRenderComponent bomb_render = new TextureRenderComponent("images/bomb.png");
        bomb.addComponent(bomb_render);

        // Visual scale
        bomb.setScale(SIZE_W, SIZE_H);

        // Physics collider
        PhysicsUtils.setScaledCollider(bomb, SIZE_W, SIZE_H);

        // Place sprite
        Vector2 bottomLeft = new Vector2(spawnCenter.x - SIZE_W * 0.5f, spawnCenter.y - SIZE_H * 0.5f);
        bomb.setPosition(bottomLeft);

        // Set up physics for falling
        PhysicsComponent physics = bomb.getComponent(PhysicsComponent.class);
        physics.setBodyType(BodyDef.BodyType.DynamicBody);
        physics.getBody().setTransform(spawnCenter, 0f);

        // Calculate horizontal velocity to reach target
        float horizontalDistance = targetPosition.x - spawnCenter.x;
        float fallTime = 1.5f; // Approximate time to fall
        float horizontalVelocity = horizontalDistance / fallTime;

        // Set initial velocity - horizontal movement toward target, vertical falling
        physics.getBody().setLinearVelocity(
                horizontalVelocity,  // Horizontal velocity toward target
                -2f                  // Initial downward velocity
        );

        // Set collision properties
        bomb.getComponent(ColliderComponent.class)
                .setSensor(false)   // Should collide with ground
                .setDensity(2f)     // Make it heavy
                .setRestitution(0f); // No bounce

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
        projectile.setPosition(source.getCenterPosition());

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