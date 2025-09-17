package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.collectables.CollectableComponentV2;
import com.csse3200.game.components.collectables.KeyComponent;
import com.csse3200.game.components.collectables.UpgradesComponent;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.CollectablesConfig;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.CollectableService;
import com.csse3200.game.services.ServiceLocator;

import java.util.Map;

/**
 * Factory for creating collectable entities (e.g., keys, coins, potions).
 */
public class CollectableFactory {

    private static Map<String, CollectablesConfig> cfgs;

    /**
     * Creates a new collectable entity instance based on its configuration ID.
     *
     * <p>
     * The returned entity includes:
     * <ul>
     *   <li>A static {@link PhysicsComponent} and {@link ColliderComponent} configured
     *       as a sensor obstacle.</li>
     *   <li>A {@link CollectableComponentV2} to handle collisions and inventory logic.</li>
     *   <li>Visual representation from either an animated {@link AnimationRenderComponent}
     *       if a sprite atlas is provided, or a {@link TextureRenderComponent} for static
     *       images.</li>
     *   <li>A {@link ConeLightComponent} for a visual backlight effect.</li>
     * </ul>
     *
     * @param itemId the identifier of the collectable, as defined in {@code items.json}
     * @return a collectable {@link Entity}
     * @throws IllegalArgumentException if {@code itemId} is not registered
     */
    public static Entity createCollectable(String itemId) {
        CollectablesConfig cfg = CollectableService.get(itemId);
        if (cfg == null) throw new IllegalArgumentException("Unknown collectable id: " + itemId);

        // create entity

        Entity e = new Entity()
                .addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody))
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.COLLECTABLE).setSensor(true))
                .addComponent(new CollectableComponentV2(itemId));

        // add sprites and animations

        if (cfg.sprite != null && cfg.sprite.endsWith(".atlas")) {
            TextureAtlas atlas = ServiceLocator.getResourceService().getAsset(cfg.sprite, TextureAtlas.class);
            AnimationRenderComponent anim = new AnimationRenderComponent(atlas);
            anim.addAnimation("collectable-spin", 0.1f, Animation.PlayMode.LOOP);
            anim.startAnimation("collectable-spin");
            e.addComponent(anim);
        } else {
            e.addComponent(new TextureRenderComponent(
                    (cfg.sprite != null && !cfg.sprite.isEmpty()) ? cfg.sprite : "images/missing.png"));
            // change scale of non potion collectables
            e.setScale(0.5f, 0.5f);
            PhysicsUtils.setScaledCollider(e, 0.5f, 0.5f);
        }

        // backlight because it looks cool (thanks tristyn)
        // set color based on glowColor in item config
        Color color = new Color();
        if (cfg.glowColor != null && !cfg.glowColor.isEmpty()) {
            color.set(cfg.glowColor.get(0) / 255f, cfg.glowColor.get(1) / 255f, cfg.glowColor.get(2) / 255f, 0.6f);
        } else {
            color.set(1f, 1f, 230f/255f, 0.6f);
        }
        ConeLightComponent cone = new ConeLightComponent(
                ServiceLocator.getLightingService().getEngine().getRayHandler(),
                128,
                color,
                1.5f,
                0f,
                180f
        );
        e.addComponent(cone);

        return e;
    }

    /**
     * Builds a collectable key for {@code target}.
     *
     * @return a new key entity
     * @see KeyComponent
     */
    public static Entity createKey(String target) {
        Entity key = new Entity()
                .addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody))
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.COLLECTABLE).setSensor(true))
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
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.COLLECTABLE).setSensor(true))
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
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.COLLECTABLE).setSensor(true))
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
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.COLLECTABLE).setSensor(true))
                .addComponent(new TextureRenderComponent("images/glide_powerup.png"))
                .addComponent(new UpgradesComponent("grapple"));

        grapple.setScale(0.5f, 0.5f);
        PhysicsUtils.setScaledCollider(grapple, 0.5f, 0.5f);

        return grapple;
    }
}