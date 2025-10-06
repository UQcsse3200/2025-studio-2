package com.csse3200.game.components.collectables;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract component for collectable items in the game world.
 * Handles collision detection with the player and invokes onCollect
 * when picked up. Subclasses define what happens on collection
 * (e.g., adding to inventory, increasing score).
 */
public class CollectableComponentV2 extends Component {
    private final String itemId;
    private boolean collected = false;

    ConeLightComponent cone;
    ColliderComponent collider;

    private static final Logger logger = LoggerFactory.getLogger(CollectableComponentV2.class);

    public CollectableComponentV2(String itemId) {
        this.itemId = itemId;
    }

    /**
     * Registers a listener for {@code "onCollisionStart"} events to trigger collection logic.
     */
    @Override
    public void create() {
        entity.getEvents().addListener("onCollisionStart", this::onCollisionStart);
        cone = entity.getComponent(ConeLightComponent.class);
        collider = entity.getComponent(ColliderComponent.class);
    }

    /**
     * Handles collision start events with other entities.
     * @param player the other entity in the collision, expected to be the Player
     */
    private void onCollisionStart(Entity player) {
        HitboxComponent cc = player.getComponent(HitboxComponent.class);
        if (cc == null || (cc.getLayer() != PhysicsLayer.PLAYER) || collected) return;

        collected = collect(player);
        if (collected) {
            // retrieve render service and visual components
            RenderService renderService = ServiceLocator.getRenderService();
            AnimationRenderComponent animation = entity.getComponent(AnimationRenderComponent.class);
            ConeLightComponent cone = entity.getComponent(ConeLightComponent.class);

            // remove atlas or static texture from game area
            if (animation != null) {
                renderService.unregister(animation);
                cone.dispose();
            } else {
                TextureRenderComponent texture = entity.getComponent(TextureRenderComponent.class);
                if (texture != null) {
                    renderService.unregister(texture);
                    cone.dispose();
                }
            }
            entity.setEnabled(false);
        }

    }

    /**
     * Attempts to collect this key into the player's inventory.
     * <p>
     * On success, the key is added to the {@link InventoryComponent} of the
     * colliding player entity, and debug information is logged to the console.
     * </p>
     *
     * @param player the player entity attempting to collect the key
     * @return {@code true} if the key was successfully collected and stored,
     *         {@code false} if the player entity is null or has no inventory
     */
    protected boolean collect(Entity player) {
        if (player == null) return false;

        var inventory = player.getComponent(InventoryComponent.class);
        if (inventory != null) {
            inventory.addItem(InventoryComponent.Bag.INVENTORY, itemId);
            return true;
        }
        return false;
    }

    /**
     * Toggles the visibility state of this entity and updates its components accordingly.
     * <p>
     * When hidden:
     * <ul>
     *   <li>Animations are disabled.</li>
     *   <li>The texture is unregistered from the render service.</li>
     *   <li>Lights (cone) are deactivated.</li>
     *   <li>Collider layer is set to {@code PhysicsLayer.NONE}.</li>
     * </ul>
     * When visible:
     * <ul>
     *   <li>Animations are enabled.</li>
     *   <li>The texture is registered with the render service.</li>
     *   <li>Lights (cone) are activated.</li>
     *   <li>Collider layer is set to {@code PhysicsLayer.COLLECTABLE}.</li>
     * </ul>
     */
    public void setVisible(boolean visible ) {
        AnimationRenderComponent animation = entity.getComponent(AnimationRenderComponent.class);
        TextureRenderComponent texture = entity.getComponent(TextureRenderComponent.class);

        if (animation != null) animation.setEnabled(visible);

        if (texture != null) {
            texture.setEnabled(visible);
            texture.
        }

        if (cone != null) {
            cone.setActive(visible);
        }

        if (collider != null) {
            collider.setLayer(visible ? PhysicsLayer.COLLECTABLE : PhysicsLayer.NONE);
            collider.setSensor(visible);
        }
    }

}
