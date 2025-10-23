package com.csse3200.game.areas;

import com.csse3200.game.areas.terrain.GridFactory;
import com.csse3200.game.entities.spawn.SpawnRegistry;

/**
 * Game area implementation for Level One (The Depths).
 * <p>
 * Loads level configuration and asset manifests from JSON, initialises terrain and
 * world boundaries, spawns entities via {@link SpawnRegistry}, and creates the player.
 * </p>
 */
public final class LevelOneGameArea extends BaseLevelGameArea {

    public LevelOneGameArea(GridFactory tf) { super(tf); }

    @Override protected String configPath() { return "levels/level-one/config.json"; }
    @Override protected String assetsPath() { return "levels/level-one/assets.json"; }
    @Override protected String parallaxPath() { return "levels/level-one/parallax.json"; }
}
