package com.csse3200.game.entities.configs;
import java.util.List;

/*
 * Configuration class for defining collectable entities in the game.
 */
public class CollectablesConfig {
    public String id;
    public String name;
    public String sprite;
    public String description;
    public String sfx;
    public boolean autoConsume;
    public List<String> tags;
    public List<EffectConfig> effects;
    public List<Integer> glowColor;
    public List<Float> scale;
    public String bag;
}