package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.MovingPlatformComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
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

    public static Entity createMovingPlatform(Vector2 start, Vector2 end, float speed) {
        Entity platform_d =
                new Entity()
                        .addComponent(new TextureRenderComponent("images/platform.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
                        .addComponent(new MovingPlatformComponent(start, end, speed));
        platform_d.getComponent(PhysicsComponent.class).setBodyType(BodyType.KinematicBody);
        platform_d.getComponent(TextureRenderComponent.class).scaleEntity();
        return platform_d;
    }
    private PlatformFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}