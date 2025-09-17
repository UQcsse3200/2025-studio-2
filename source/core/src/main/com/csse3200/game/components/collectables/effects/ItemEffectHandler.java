package com.csse3200.game.components.collectables.effects;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.EffectConfig;

/**
 * Functional interface for defining item effects that can be applied to entities.
 */
@FunctionalInterface
public interface ItemEffectHandler {
    /**
     * Apply a single effect to the player.
     *
     * @param player the player entity
     * @param cfg the effect configuration containing effect parameters
     * @return {@code true} if the effect was successfully applied; {@code false} otherwise
     */
    boolean apply(Entity player, EffectConfig cfg);
}
