package com.csse3200.game.components.collectables.effects;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.EffectConfig;

/**
 * An item effect that adds an objective to the OBJECTIVES bag
 * and triggers an "objectiveCollected" event.
 */
public class AddObjective implements ItemEffectHandler {
    @Override
    public boolean apply(Entity player, EffectConfig cfg) {
        if (player == null || cfg == null) return false;

        String target =
                (cfg.target != null && !cfg.target.isBlank())
                        ? cfg.target.trim()
                        : (cfg.params != null
                        ? cfg.params.getOrDefault("target", "").trim()
                        : "");

        if (target.isEmpty()) {
            throw new IllegalArgumentException(
                    "AddObjective requires a target (set cfg.target in JSON or `params.target` via setEffectParam)");
        }

        InventoryComponent inv = player.getComponent(InventoryComponent.class);
        if (inv == null) return false;

        inv.addDirect(InventoryComponent.Bag.OBJECTIVES, target, 1);
        player.getEvents().trigger("objectiveCollected", target);

        return true;
    }
}