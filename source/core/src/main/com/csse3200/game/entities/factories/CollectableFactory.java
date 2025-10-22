package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.collectables.*;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.minimap.MinimapComponent;
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
    private CollectableFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

    private static Map<String, CollectablesConfig> cfgs;

    /**
     * Creates a new collectable entity instance based on its configuration ID.
     *
     * <p>
     * The returned entity includes:
     * <ul>
     *   <li>A static {@link PhysicsComponent} and {@link ColliderComponent} configured
     *       as a sensor obstacle.</li>
     *   <li>A {@link CollectableComponent} to handle collisions and inventory logic.</li>
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
                .addComponent(new CollectableComponent(itemId));

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
            float sx = cfg.scale != null ? cfg.scale.get(0) : 0.5f;
            float sy = cfg.scale != null ? cfg.scale.get(1) : 0.5f;

            e.setScale(sx, sy);
            PhysicsUtils.setScaledCollider(e, sx, sy);
        }

        // set color based on glowColor in item config
        ConeLightComponent cone = getConeLightComponent(cfg);
        e.addComponent(cone);

        return e;
    }

    private static ConeLightComponent getConeLightComponent(CollectablesConfig cfg) {
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
        return cone;
    }
}