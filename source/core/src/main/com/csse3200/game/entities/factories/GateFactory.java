package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
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
public class GateFactory {

    /**
     * Creates a static platform entity.
     * @return entity
     */
    public static Entity createGate() {
        Entity gate =
                new Entity()
                        .addComponent(new TextureRenderComponent("images/gate.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
        gate.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        gate.getComponent(TextureRenderComponent.class).scaleEntity();
        return gate;
    }
    private GateFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}
;