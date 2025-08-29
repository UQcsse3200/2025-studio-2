package com.csse3200.game.components.collectables;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;

/**
 * Component that tracks and manages a player's inventory as a multiset of item identifiers
 * (stack counts).
 */
public abstract class CollectableComponent extends Component {
    private boolean collected = false;

    /**
     * Registers a two-argument listener for {@code "collisionStart"} that delegates to
     * {@link #onCollisionStart(Object, Object)} when this entity collides with another.
     */
    @Override
    public void create() {
        // physics emits a 2-arg event: (collectable, playerOrOther)
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    }

    /**
     * Handles begin-contact events for this collectable.
     *
     * @param collectable the event's "self" argument (this entity)
     * @param player      the other object in the collision; expected to be a Player {@link Entity}
     */
    private void onCollisionStart(Object collectable, Object player) {
        if (collected || !(player instanceof Entity p)) return;

        ColliderComponent cc = p.getComponent(ColliderComponent.class);
        if (cc == null || cc.getLayer() != PhysicsLayer.PLAYER) return;

        if (onCollect(p)) {
            collected = true;
            entity.setEnabled(false);
            // entity.dispose(); // dispose collectable
        }
    }

    /** Return true if collection succeeded (then the collectable will be disposed). */
    protected abstract boolean onCollect(Entity collector);
}
