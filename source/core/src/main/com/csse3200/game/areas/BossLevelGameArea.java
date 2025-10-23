package com.csse3200.game.areas;

import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.entities.spawn.SpawnRegistry;

/**
 * Game area implementation for Level Four (Boss level).
 * <p>././
 * Loads level configuration and asset manifests from JSON, initialises terrain and
 * world boundaries, spawns entities via {@link SpawnRegistry}, and creates the player.
 * </p>
 */
public class BossLevelGameArea extends BaseLevelGameArea {
    public BossLevelGameArea(TerrainFactory tf) { super(tf); }
    @Override protected String configPath() { return "levels/boss-level/config.json"; }
    @Override protected String assetsPath() { return "levels/boss-level/assets.json"; }
    @Override protected String parallaxPath() { return "levels/boss-level/parallax.json"; }

}

