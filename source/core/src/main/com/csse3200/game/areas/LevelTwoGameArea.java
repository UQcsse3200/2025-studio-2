package com.csse3200.game.areas;

import com.csse3200.game.areas.terrain.GridFactory;
import com.csse3200.game.entities.spawn.SpawnRegistry;

/**
 * Game area implementation for Level Two (The Lab).
 * <p>
 * Loads level configuration and asset manifests from JSON, initialises terrain and
 * world boundaries, spawns entities via {@link SpawnRegistry}, and creates the player.
 * </p>
 */
public final class LevelTwoGameArea extends BaseLevelGameArea {
    public LevelTwoGameArea(GridFactory tf) { super(tf); }

    @Override protected String configPath() { return "levels/level-two/config.json"; }
    @Override protected String assetsPath() { return "levels/level-two/assets.json"; }
    @Override protected String parallaxPath() { return "levels/level-two/parallax.json"; }
}
