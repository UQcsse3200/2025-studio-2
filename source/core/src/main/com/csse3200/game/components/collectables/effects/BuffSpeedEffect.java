package com.csse3200.game.components.collectables.effects;

import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.EffectConfig;

/**
 * An item effect that temporarily increases the player's movement speed.
 */
public class BuffSpeedEffect implements ItemEffectHandler {
    /**
     * Applies the speed buff effect to the given player entity.
     * <p>
     * Immediately triggers the {@code "toggleAdrenaline"} event to activate the
     * speed buff. If {@code cfg.duration} is greater than zero, schedules a
     * second trigger after the duration to deactivate the buff.
     * </p>
     *
     * @param player the entity to receive the effect
     * @param cfg the effect configuration containing duration and parameters
     * @return {@code true} once the effect has been applied
     */
    @Override
    public boolean apply(Entity player, EffectConfig cfg) {
        player.getEvents().trigger("toggleAdrenaline");

        if (cfg.duration > 0) {
            Timer.schedule(new Timer.Task() {
                @Override public void run() {
                    player.getEvents().trigger("toggleAdrenaline");
                }
            }, (float) cfg.duration);
        }
        return true;
    }
}
