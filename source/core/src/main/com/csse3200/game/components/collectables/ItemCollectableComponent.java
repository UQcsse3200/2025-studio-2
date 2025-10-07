package com.csse3200.game.components.collectables;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.utils.CollectableCounter;

public class ItemCollectableComponent extends CollectableComponent {
    @Override
    protected boolean onCollect(Entity collector) {
        CollectableCounter.increment();
        return true;
    }
}
