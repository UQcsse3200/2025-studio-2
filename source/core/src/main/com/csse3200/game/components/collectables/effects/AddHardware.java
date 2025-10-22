package com.csse3200.game.components.collectables.effects;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.EffectConfig;
import com.csse3200.game.utils.CollectablesSave;

import static java.lang.Float.parseFloat;

/**
 * Effect that tracks item collectables in the world.
 * <p>
 * When applied, the player's collected count is incremented if the current
 * position has not already been collected. The updated count is then broadcast
 * via the {@code updateCollectables} event.
 * </p>
 */
public class AddHardware implements ItemEffectHandler {
    Vector2[] collected;
    Vector2 currentPos;

    public AddHardware() {}


    /**
     * Applies the hardware collection effect to the given player.
     *
     * @param player the player entity that collected the hardware
     * @param cfg the effect configuration containing positional parameters
     * @return {@code true} if the effect executed successfully, {@code false} if input was invalid
     */
    @Override
    public boolean apply(Entity player, EffectConfig cfg) {
        if (player == null || cfg == null) return false;

        currentPos = resolvePickupPosition(cfg);
        collected = CollectablesSave.loadCollectedPositions();

        if (!hasBeenCollected(collected, currentPos)) {
            addCollected(collected, currentPos);
            CollectablesSave.incrementCollectedCount();
        }

        int count = CollectablesSave.getCollectedCount();
        player.getEvents().trigger("updateCollectables", count);
        return true;
    }


    /**
     * Resolves the pickup position from effect parameters.
     * <p>
     * Expects {@code cfg.params} to contain string keys {@code "x"} and {@code "y"}.
     * </p>
     *
     * @param cfg the effect configuration containing positional parameters
     * @return a {@link Vector2} of the resolved position
     */
    private Vector2 resolvePickupPosition(EffectConfig cfg) {
        float x = parseFloat(cfg.params.get("x"));
        float y = parseFloat(cfg.params.get("y"));
        return new Vector2(x, y);
    }

    /**
     * Checks whether the given position has already been collected.
     *
     * @param collected the array of previously collected positions
     * @param pos the position to test
     * @return {@code true} if the position has already been collected, otherwise {@code false}
     */
    private boolean hasBeenCollected(Vector2[] collected, Vector2 pos) {
        for (Vector2 v : collected) {
            if (v != null && v.epsilonEquals(pos, (float) 0.01)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the given position to the collected array and persists it using {@link CollectablesSave}.
     *
     * @param collected the array of collected positions
     * @param pos       the new position to add
     */
    private void addCollected(Vector2[] collected, Vector2 pos) {
        for (int i = 0; i < collected.length; i++) {
            if (collected[i] == null || (collected[i].x == 0 && collected[i].y == 0)) {
                collected[i] = new Vector2(pos);
                CollectablesSave.saveCollectedPositions(i, pos);
                CollectablesSave.saveCollectedPositions(collected);
                return;
            }
        }
    }
}