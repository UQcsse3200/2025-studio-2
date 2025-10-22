package com.csse3200.game.components.collectables.effects;

import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.statisticspage.StatsTracker;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.EffectConfig;

/** Adds an upgrade to the player's inventory and updates stats. */
public class AddUpgrade implements ItemEffectHandler {
    @Override
    public boolean apply(Entity player, EffectConfig cfg) {
        if (player == null || cfg == null) return false;

        if (cfg.target == null || cfg.target.isBlank()) {
            throw new IllegalArgumentException(
                    "AddUpgrade requires a non-null target (e.g., \"dash\")"
            );
        }
        final String upgradeId = cfg.target;

        InventoryComponent inv = player.getComponent(InventoryComponent.class);
        if (inv == null) return false;

        // Remove corresponding objectives and add to inventory
        inv.removeItem(InventoryComponent.Bag.OBJECTIVES, upgradeId);
        if (!inv.hasItem(InventoryComponent.Bag.UPGRADES, upgradeId)) {
            inv.addDirect(InventoryComponent.Bag.UPGRADES, upgradeId, 1);
            StatsTracker.addUpgrade();
        }
        return true;
    }
}
