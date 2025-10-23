package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.projectiles.BombComponent;
import com.csse3200.game.components.projectiles.LaserProjectileComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
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
        final float SIZE_W = 0.5f, SIZE_H = 0.5f;

        bomb.addComponent(new TextureRenderComponent("images/bomb.png"));
        bomb.setScale(SIZE_W, SIZE_H);

        // Physics collider
        PhysicsUtils.setScaledCollider(bomb, SIZE_W, SIZE_H);

        Vector2 bottomLeft = new Vector2(spawnCenter.x - SIZE_W * 0.5f, spawnCenter.y - SIZE_H * 0.5f);
        bomb.setPosition(bottomLeft);

        // Set up physics for falling
        PhysicsComponent physics = bomb.getComponent(PhysicsComponent.class);
        physics.setBodyType(BodyDef.BodyType.DynamicBody);
        physics.getBody().setTransform(spawnCenter, 0f);
        physics.getBody().setGravityScale(1f);
        physics.getBody().setLinearVelocity(0f, 0f);

        // Set collision properties
        bomb.getComponent(ColliderComponent.class)
                .setSensor(false)   // Should collide with ground
                .setDensity(2f)     // Make it heavy
                .setRestitution(0f); // No bounce

        return bomb;
    }

    /**
     * Creates a laser pulse projectile entity
     * @param source The entity that created the projectile (boss)
     * @param spawnPos Position where projectile spawns (CENTER position)
     * @param direction Direction the projectile should travel (normalized)
     * @param speed Speed of the projectile
     * @param damage Damage the projectile deals
     * @return Laser projectile entity
     */
    public static Entity createLaserProjectile(Entity source, Vector2 spawnPos,
                                               Vector2 direction, float speed, int damage) {
        // Load boss atlas for shootEffect animation
        TextureAtlas bossAtlas = ServiceLocator.getResourceService()
                .getAsset("images/boss.atlas", TextureAtlas.class);

        if (bossAtlas == null) {
            throw new RuntimeException("boss.atlas not loaded - required for laser projectile");
        }

        // Calculate rotation angle from direction vector
        float rotationAngle = direction.angleDeg();
        // Convert to radians for Box2D
        float rotationRadians = (float) Math.toRadians(rotationAngle);

        // Setup animation component with shootEffect
        AnimationRenderComponent animator = new AnimationRenderComponent(bossAtlas);
        animator.addAnimation("shootEffect", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("touchKillEffect", 0.2f, Animation.PlayMode.NORMAL);

        // Set rotation to match movement direction
        animator.setRotation(rotationAngle);

        // Set origin to center for proper rotation
        animator.setOrigin(0.5f, 0.5f);

        // CRITICAL: shootEffect sprite is 285x96 pixels (not 640x640 like other boss sprites)
        // Base size in world units (assuming 640 pixels = 1 unit for boss sprites)
        float shootEffectBaseWidth = 285f / 640f;   // ~0.445 units
        float shootEffectBaseHeight = 96f / 640f;   // ~0.15 units

        // Scale up the visual size
        float visualScale = 1f;

        // Create projectile entity
        Entity laserProjectile = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.NPC))
                .addComponent(new CombatStatsComponent(1, damage))
                .addComponent(animator)
                .addComponent(new LaserProjectileComponent(direction, speed, source));

        // Set scale - this affects rendering size
        laserProjectile.setScale(visualScale+3f, visualScale+0.3f);

        // Calculate actual rendered size after scaling
        float renderedWidth = shootEffectBaseWidth * visualScale;
        float renderedHeight = shootEffectBaseHeight * visualScale;

        // Offset spawn position to the left to align with boss's visual attack point
        Vector2 adjustedSpawnPos = spawnPos.cpy();
        adjustedSpawnPos.x += 0.5f;  // Move 3 units to the left

        // CRITICAL: adjustedSpawnPos is CENTER position, but setPosition() expects BOTTOM-LEFT
        // Convert center to bottom-left based on ACTUAL sprite dimensions
        Vector2 bottomLeftPos = new Vector2(
                adjustedSpawnPos.x - renderedWidth * 0.5f,
                adjustedSpawnPos.y - renderedHeight * 0.5f
        );
        laserProjectile.setPosition(bottomLeftPos);

        // Set collider size to match the visual sprite (slightly smaller for better gameplay)
        float colliderWidth = renderedWidth + 2.9f;
        float colliderHeight = renderedHeight + 0.7f;

        // Define the base offset (when rotation is 0, pointing right)
        // Adjust these values to position the collider relative to the body center:
        // - baseOffsetX: positive = forward (right), negative = backward (left)
        // - baseOffsetY: positive = up, negative = down
        float baseOffsetX = 2f;  // Shifts collider forward along projectile direction
        float baseOffsetY = 0.6f;  // Shifts collider perpendicular to projectile direction

        // Calculate the collider center that rotates around the body origin
        // As rotationRadians changes, this point moves in a circle
        float colliderCenterX = (float)(baseOffsetX * Math.cos(rotationRadians) - baseOffsetY * Math.sin(rotationRadians));
        float colliderCenterY = (float)(baseOffsetX * Math.sin(rotationRadians) + baseOffsetY * Math.cos(rotationRadians));
        Vector2 colliderCenter = new Vector2(colliderCenterX, colliderCenterY);

        // Create the rotated polygon shape
        PolygonShape rotatedBox = new PolygonShape();
        rotatedBox.setAsBox(
                colliderWidth / 2f,      // half-width
                colliderHeight / 2f,     // half-height
                colliderCenter,          // center rotates in a circle as angle changes
                rotationRadians                       // no additional rotation (body itself is rotated)
        );

        // Set the rotated shape to the collider
        laserProjectile.getComponent(ColliderComponent.class).setShape(rotatedBox);

        // Set physics body transform to adjusted spawn center position WITH ROTATION
        PhysicsComponent physics = laserProjectile.getComponent(PhysicsComponent.class);
        if (physics != null && physics.getBody() != null) {
            physics.setBodyType(BodyDef.BodyType.DynamicBody);

            // Set body position AND rotation to match animation
            physics.getBody().setTransform(adjustedSpawnPos, rotationRadians);

            // Physics properties for the laser
            physics.getBody().setGravityScale(0f);
            physics.getBody().setBullet(true);
            physics.getBody().setLinearDamping(0f);
            physics.getBody().setAngularDamping(0f);

            // Prevent rotation during flight
            physics.getBody().setFixedRotation(true);

            // Set velocity based on direction and speed (reduced from original)
            Vector2 velocity = direction.cpy().nor().scl(speed * 0.4f);  // Reduce speed to 40% of original
            physics.getBody().setLinearVelocity(velocity);
        }

        // Start shootEffect animation
        animator.startAnimation("shootEffect");
        animator.setLayer(3); // Render above most entities

        return laserProjectile;
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