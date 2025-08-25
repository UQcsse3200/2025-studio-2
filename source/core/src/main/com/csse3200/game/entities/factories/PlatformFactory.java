package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

public class PlatformFactory {

    public static Entity createPlatform() {
        Entity platform =
                new Entity()
                        .addComponent(new TextureRenderComponent("images/platform.png"))
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
        platform.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        platform.getComponent(TextureRenderComponent.class).scaleEntity();
        return platform;
    }
}
;