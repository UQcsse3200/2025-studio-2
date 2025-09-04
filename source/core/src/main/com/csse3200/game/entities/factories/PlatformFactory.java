package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.platforms.*;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
/**
 * Factory to create Platform entities.
 *
 * <p>Each Platform entity type should have a creation method that returns a corresponding entity.
 */
public class PlatformFactory {

    /**
     * Creates a static platform entity.
     * @return entity
     */
    public static Entity createStaticPlatform() {
        Entity platform =
                new Entity()
                        .addComponent(new TextureRenderComponent("images/platform.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
        platform.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        platform.getComponent(TextureRenderComponent.class).scaleEntity();
        return platform;
    }

    /**
     * Creates a dynamic moving platform entity. Movement is based upon the calculation of offsetWorld.
     * @param offsetWorld the world offset for the platform's movement
     * @param speed the speed at which the platform moves
     * @return entity the created moving platform entity
     */
    public static Entity createMovingPlatform(Vector2 offsetWorld, float speed) {
        ColliderComponent collider = new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE);
        collider.setFriction(7f);
        Entity platform = new Entity()
                .addComponent(new TextureRenderComponent("images/platform.png"))
                .addComponent(new PhysicsComponent())
                .addComponent(collider)
                .addComponent(new MovingPlatformComponent(offsetWorld, speed));

        platform.getComponent(PhysicsComponent.class).setBodyType(BodyType.KinematicBody);
        platform.getComponent(TextureRenderComponent.class).scaleEntity();
        return platform;
    }

    /**
     * Creates button triggered platform, where if the player presses the button, the platform moves
     * @param offsetWorld
     * @param speed
     * @return
     */
    public static Entity createButtonTriggeredPlatform(Vector2 offsetWorld, float speed) {
        Entity platform = new Entity()
                .addComponent(new TextureRenderComponent("images/platform.png"))
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
                .addComponent(new ButtonTriggeredPlatformComponent(offsetWorld, speed));
        platform.getComponent(PhysicsComponent.class).setBodyType(BodyType.KinematicBody);
        platform.getComponent(TextureRenderComponent.class).scaleEntity();
        return platform;
    }
    private PlatformFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}