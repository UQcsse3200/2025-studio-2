package com.csse3200.game.components.collectables.effects;

import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.EffectConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Effect handler that removes a specific objective from the OBJECTIVES bag
 * if it exists in the player's inventory.
 */
public class RemoveObjective implements ItemEffectHandler {
    private static final Logger logger = LoggerFactory.getLogger(RemoveObjective.class);

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

        if (inv.hasItem(InventoryComponent.Bag.OBJECTIVES, cfg.target)) {
            inv.removeItem(InventoryComponent.Bag.OBJECTIVES, cfg.target);
        }
        return true;
    }
}
