package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.collectables.KeyComponent;
import com.csse3200.game.components.collectables.ObjectivesComponent;
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

    /**
     * Creates an invisible objective pickup with a large sensor collider.
     * Default footprint is 2x2 world units (might tweak)
     *
     * @param objectiveId id routed through switch in ObjectiveCollectableComponent (e.g., "obj:plans")
     */
    public static Entity createObjective(String objectiveId) {
        return createObjective(objectiveId, 2f, 2f);
    }

    /**
     * Creates an invisible objective pickup with a custom sensor size (world units).
     *
     * @param objectiveId id routed through switch in ObjectiveCollectableComponent
     * @param width collider width in world units
     * @param height collider height in world units
     */
    public static Entity createObjective(String objectiveId, float width, float height) {
        Entity obj = new Entity()
                .addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody))
                .addComponent(new ColliderComponent()
                        .setLayer(PhysicsLayer.OBSTACLE) // collides with player's hitbox
                        .setSensor(true))                // no blocking, overlap only
                .addComponent(new ObjectivesComponent(objectiveId));

        // Size the sensor using the same pattern as your other collectables
        obj.setScale(width, height);
        PhysicsUtils.setScaledCollider(obj, width, height);

        // NOTE: No TextureRenderComponent -> invisible in world
        return obj;
    }

}