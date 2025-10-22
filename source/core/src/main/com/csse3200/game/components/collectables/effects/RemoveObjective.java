package com.csse3200.game.components.collectables.effects;

import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.statisticspage.StatsTracker;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.EffectConfig;

/**
 * Effect handler that removes a specific objective from the OBJECTIVES bag
 * if it exists in the player's inventory.
 */
public class RemoveObjective implements ItemEffectHandler {
    /**
     * Removes the specified objective from the player's OBJECTIVES bag.
     *
     * @param player the entity whose inventory should be modified
     * @param cfg    the effect configuration containing the {@code target} objective ID
     * @return {@code true} if execution was valid (even if the item did not exist),
     *         {@code false} if the player or config was invalid
     */
    @Override
    public boolean apply(Entity player, EffectConfig cfg) {
        if (player == null || cfg == null) {
            return false;
        }

        InventoryComponent inv = player.getComponent(InventoryComponent.class);
        if (inv == null) return false;

        if (!inv.hasItem(InventoryComponent.Bag.UPGRADES, cfg.target)) {
            inv.addDirect(InventoryComponent.Bag.UPGRADES, cfg.target, 1);
            StatsTracker.addUpgrade();
        }
        return true;
    }
}
