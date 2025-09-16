package com.csse3200.game.components.collectables.effects;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.EffectConfig;

/**
 * An item effect that restores health to the player.
 */
public class HealEffect implements ItemEffectHandler {
    /**
     * Applies the heal effect to the given player entity.
     *
     * @param player the entity to heal (must have a {@link CombatStatsComponent})
     * @param cfg    the effect configuration containing the heal value
     * @return {@code true} if the effect was successfully applied, otherwise {@code false}
     */
    @Override
    public boolean apply(Entity player, EffectConfig cfg) {
        var stats = player.getComponent(CombatStatsComponent.class);
        stats.addHealth((int) cfg.value);
        return true;
    }
}
