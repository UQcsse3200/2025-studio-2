package com.csse3200.game.areas;

import com.csse3200.game.areas.terrain.TerrainFactory;

public class LevelTwoNewGameArea extends BaseLevelGameArea{
    public LevelTwoNewGameArea(TerrainFactory tf) { super(tf); }

    @Override protected String configPath() { return "levels/level-two/config.json"; }
    @Override protected String assetsPath() { return "levels/level-two/assets.json"; }
    @Override protected String parallaxPath() { return "levels/level-two/parallax.json"; }
}
