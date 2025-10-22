package com.csse3200.game.components.collectables.effects;

import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.statisticspage.StatsTracker;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.EffectConfig;

/** Adds an upgrade to the player's inventory and updates stats. */
public class AddUpgrade implements ItemEffectHandler {
    /**
     * Applies the upgrade effect to the given player.
     *
     * @param player the player entity that should receive the upgrade
     * @param cfg the effect configuration specifying the target upgrade
     * @return {@code true} if the upgrade was successfully applied,
     *         {@code false} if the player lacks an {@link InventoryComponent}
     *         or parameters were invalid
     * @throws IllegalArgumentException if the {@code cfg.target} is null or blank
     */
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
