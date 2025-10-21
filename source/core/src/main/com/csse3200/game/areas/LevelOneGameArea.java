package com.csse3200.game.areas;

import com.csse3200.game.areas.terrain.TerrainFactory;

public final class LevelOneGameArea extends BaseLevelGameArea {

    public LevelOneGameArea(TerrainFactory tf) { super(tf); }

    @Override protected String configPath() { return "levels/level-one/config.json"; }
    @Override protected String assetsPath() { return "levels/level-one/assets.json"; }
    @Override protected String parallaxPath() { return "levels/level-one/parallax.json"; }
}
