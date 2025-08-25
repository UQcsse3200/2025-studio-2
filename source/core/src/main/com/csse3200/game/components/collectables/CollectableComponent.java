package com.csse3200.game.components.collectables;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;

public abstract class CollectableComponent extends Component {
    private boolean collected = false;

    @Override
    public void create() {
        // physics emits a 2-arg event: (collectable, playerOrOther)
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    }

    private void onCollisionStart(Object collectable, Object player) {
        if (collected || !(player instanceof Entity p)) return;

        ColliderComponent cc = p.getComponent(ColliderComponent.class);
        if (cc == null || cc.getLayer() != PhysicsLayer.PLAYER) return;

        if (onCollect(p)) {
            collected = true;
            entity.dispose(); // dispose collectable
        }
    }

    /** Return true if collection succeeded (then the collectable will be disposed). */
    protected abstract boolean onCollect(Entity collector);
}
