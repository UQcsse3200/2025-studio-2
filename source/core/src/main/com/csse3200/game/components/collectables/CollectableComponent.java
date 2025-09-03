package com.csse3200.game.components.collectables;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;

/**
 * Component that tracks and manages a player's inventory as a multiset of item identifiers
 * (stack counts).
 */
public abstract class CollectableComponent extends Component {
    private boolean collected = false;

    /**
     * Registers a two-argument listener for {@code "collisionStart"} that delegates to
     */
    @Override
    public void create() {
        // physics emits a 2-arg event: (collectable, playerOrOther)
        entity.getEvents().addListener("onCollisionStart", this::onCollisionStart);
    }

    /**
     * Handles begin-contact events for this collectable.
     *
 //    * @param collectable the event's "self" argument (this entity)
     * @param player      the other object in the collision; expected to be a Player {@link Entity}
     */
    private void onCollisionStart(Entity player) {
        HitboxComponent cc = player.getComponent(HitboxComponent.class);

        if (cc == null) {
            return; // Not a collidable entity, ignore
        }

        if ((cc.getLayer() != PhysicsLayer.PLAYER) || collected) return;


        collected = onCollect(player);
        if (collected) {
            TextureRenderComponent renderComponent = entity.getComponent(TextureRenderComponent.class);
            RenderService renderService = ServiceLocator.getRenderService();
            renderService.unregister(renderComponent);
            entity.setEnabled(false);
        }
    }

    /** Return true if collection succeeded (then the collectable will be disposed). */
    protected abstract boolean onCollect(Entity collector);
}
