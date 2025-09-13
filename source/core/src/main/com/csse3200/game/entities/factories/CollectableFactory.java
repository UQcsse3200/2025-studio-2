package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.collectables.KeyComponent;
import com.csse3200.game.components.collectables.UpgradesComponent;
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

    /**
     * Builds a collectable upgrade that enable the dash ability
     * @return a new upgrade entity
     * @see UpgradesComponent
     */
    public static Entity createDashUpgrade() {
        Entity dash = new Entity()
                .addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody))
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE).setSensor(true))
                .addComponent(new TextureRenderComponent("images/dash_powerup.png"))
                .addComponent(new UpgradesComponent("dash"));

        dash.setScale(0.5f, 0.5f);
        PhysicsUtils.setScaledCollider(dash, 0.5f, 0.5f);

        return dash;
    }

    /**
     * Build a new collectable glider upgrade that enables the glide ability
     *
     * @return a new upgrade entity
     * @see UpgradesComponent
     */
    public static Entity createGlideUpgrade() {
        Entity glide = new Entity()
                .addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody))
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE).setSensor(true))
                .addComponent(new TextureRenderComponent("images/glide_powerup.png"))
                .addComponent(new UpgradesComponent("glider"));

        glide.setScale(0.5f, 0.5f);
        PhysicsUtils.setScaledCollider(glide, 0.5f, 0.5f);

        return glide;
    }

    /**
     * Builds a new collectable grappler upgrade that enables the grapple ability
     *
     * @return a new upgrade entity
     * @see UpgradesComponent
     */
    public static Entity createGrappleUpgrade() {
        Entity grapple = new Entity()
                .addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody))
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE).setSensor(true))
                .addComponent(new TextureRenderComponent("images/glide_powerup.png"))
                .addComponent(new UpgradesComponent("grapple"));

        grapple.setScale(0.5f, 0.5f);
        PhysicsUtils.setScaledCollider(grapple, 0.5f, 0.5f);

        return grapple;
    }
}