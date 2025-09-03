package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.collectables.KeyComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Factory for creating collectable entities (e.g., keys, coins, potions).
 */
public class CollectableFactory {

    /**
     * Builds a collectable key for {@code target}.
     *
     * @return a new key entity
     * @see KeyComponent
     */
    public static Entity createKey(String target) {
        Entity key = new Entity()
                .addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody))
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE).setSensor(true))
                .addComponent(new TextureRenderComponent("images/key.png"))
                .addComponent(new KeyComponent(target));

        key.setScale(0.5f, 0.5f);
        PhysicsUtils.setScaledCollider(key, 0.5f, 0.5f);

        return key;
    }
}