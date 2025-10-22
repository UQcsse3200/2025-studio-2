package com.csse3200.game.components.collectables.effects;

import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.EffectConfig;

public class AddHardware implements ItemEffectHandler {
    @Override
    public boolean apply(Entity player, EffectConfig cfg) {
        return true;
    }
}
