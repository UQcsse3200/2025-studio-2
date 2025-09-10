package com.csse3200.game.components.collectables;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Abstract component for collectable items in the game world.
 * Handles collision detection with the player and invokes {@link #onCollect(Entity)}
 * when picked up. Subclasses define what happens on collection
 * (e.g., adding to inventory, increasing score).
 */

public abstract class CollectableComponent extends Component {
    private boolean collected = false;

    /**
     * Registers a listener for {@code "onCollisionStart"} events to trigger collection logic.
     */
    @Override
    public void create() {
        entity.getEvents().addListener("onCollisionStart", this::onCollisionStart);
    }

    /**
     * Handles collision start events with other entities.
     * @param player the other entity in the collision, expected to be the Player
     */
    private void onCollisionStart(Entity player) {
        HitboxComponent cc = player.getComponent(HitboxComponent.class);
        if (cc == null || (cc.getLayer() != PhysicsLayer.PLAYER) || collected) return;

        collected = onCollect(player);
        if (collected) {
            TextureRenderComponent renderComponent = entity.getComponent(TextureRenderComponent.class);
            RenderService renderService = ServiceLocator.getRenderService();
            renderService.unregister(renderComponent);
            entity.setEnabled(false);
        }
    }

    /**
     * Called when this collectable is picked up by the player.
     *
     * @param collector the player entity collecting this item
     * @return true if collection succeeded (and the item should be removed), false otherwise
     */
    protected abstract boolean onCollect(Entity collector);
}
