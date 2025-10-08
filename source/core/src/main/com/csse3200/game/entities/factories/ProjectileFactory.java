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
import com.csse3200.game.components.DisposalComponent;
import com.csse3200.game.components.projectiles.LaserComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.components.TouchAttackComponent;

/**
 * Factory to create projectile entities like bombs, bullets, etc.
 */
public class ProjectileFactory {

    /**
     * Creates a bomb entity that falls from source position toward target
     * @param source The entity that created the bomb (drone)
     * @param spawnPosition Position where bomb spawns (drone's position)
     * @param targetPosition Position where bomb should land (player's position)
     * @param explosionDelay Time in seconds before explosion after landing
     * @param explosionRadius Radius of explosion damage area
     * @param damage Damage the bomb will deal
     * @return Bomb entity
     */
    public static Entity createBomb(Entity source, Vector2 spawnPosition, Vector2 targetPosition,
                                    float explosionDelay, float explosionRadius, int damage) {
        Entity bomb = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.NPC))
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                .addComponent(new CombatStatsComponent(1, damage))
                .addComponent(new BombComponent(explosionDelay, explosionRadius, PhysicsLayer.PLAYER));

        // Try to add texture, use a default if bomb.png doesn't exist
        try {
            bomb.addComponent(new TextureRenderComponent("images/bomb.png"));
        } catch (Exception e) {
            // Use a small box as placeholder for bomb
            bomb.addComponent(new TextureRenderComponent("images/box_boy_leaf.png"));
        }

        // Position bomb at spawn position (drone's location)
        bomb.setPosition(spawnPosition);

        // Set up physics for falling
        bomb.getComponent(PhysicsComponent.class).setBodyType(BodyDef.BodyType.DynamicBody);

        // Calculate horizontal velocity to reach target
        float horizontalDistance = targetPosition.x - spawnPosition.x;
        float fallTime = 1.5f; // Approximate time to fall
        float horizontalVelocity = horizontalDistance / fallTime;

        // Set initial velocity - horizontal movement toward target, vertical falling
        bomb.getComponent(PhysicsComponent.class).getBody().setLinearVelocity(
                horizontalVelocity,  // Horizontal velocity toward target
                -2f                  // Initial downward velocity
        );

        // Set collision properties
        bomb.getComponent(ColliderComponent.class)
                .setSensor(false)   // Should collide with ground
                .setDensity(2f)     // Make it heavy
                .setRestitution(0.1f); // Low bounce

        // Scale the bomb to be small
        bomb.setScale(0.3f, 0.3f);
        PhysicsUtils.setScaledCollider(bomb, 0.3f, 0.3f);

        return bomb;
    }

    public static Entity createLaser(Entity shooter, Vector2 direction, float speed, int damage) {
        Entity laser = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new PhysicsMovementComponent())
                .addComponent(new ColliderComponent().setSensor(true).setLayer(PhysicsLayer.PROJECTILE))
                .addComponent(new DisposalComponent(5f));

// Create CombatStatsComponent for the laser
        CombatStatsComponent laserStats = new CombatStatsComponent(1, damage);
        laser.addComponent(laserStats);
        laser.addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 0f, laserStats));

        laser.addComponent(new HitboxComponent().setLayer(PhysicsLayer.PROJECTILE));
        laser.addComponent(new LaserComponent(shooter, speed, damage));

// Animation render using laser atlas
        TextureAtlas laserAtlas = ServiceLocator.getResourceService()
                .getAsset("images/Laser.atlas", TextureAtlas.class);
        AnimationRenderComponent animator = new AnimationRenderComponent(laserAtlas);
        animator.addAnimation("laser_attack", 0.05f, Animation.PlayMode.LOOP); // moving laser
        animator.addAnimation("laser_effact", 0.05f, Animation.PlayMode.NORMAL); // hit effect
        animator.startAnimation("laser_attack");
        laser.addComponent(animator);

// Set initial velocity
        laser.getComponent(PhysicsComponent.class)
                .setBodyType(BodyDef.BodyType.DynamicBody);
        laser.getComponent(PhysicsComponent.class)
                .getBody()
                .setLinearVelocity(direction.cpy().scl(speed));

// Scale laser beam and collider
        laser.setScale(1.8f, 0.8f);
        PhysicsUtils.setScaledCollider(laser, 1.8f, 0.8f);

// Register entity
        ServiceLocator.getEntityService().register(laser);

        return laser;
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