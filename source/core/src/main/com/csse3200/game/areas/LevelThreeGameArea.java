package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.achievements.AchievementProgression;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.LevelAssetsConfig;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.entities.configs.LevelConfig;
import com.csse3200.game.entities.spawn.SpawnRegistry;
import com.csse3200.game.entities.spawn.Spawners;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.rendering.parallax.ParallaxBackgroundComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.achievements.AchievementToastUI;
import com.csse3200.game.ui.achievements.AchievementsMenuUI;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.files.FileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Game area implementation for Level Three (Surface level).
 * <p>
 * Loads level configuration and asset manifests from JSON, initialises terrain and
 * world boundaries, spawns entities via {@link SpawnRegistry}, and creates the player.
 * </p>
 */
public final class LevelThreeGameArea extends BaseLevelGameArea {

    public LevelThreeGameArea(TerrainFactory tf) { super(tf); }

    @Override protected String configPath() { return "levels/level-three/config.json"; }
    @Override protected String assetsPath() { return "levels/level-three/assets.json"; }
    @Override protected String parallaxPath() { return "levels/level-three/parallax.json"; }
}
