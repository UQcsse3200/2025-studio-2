package com.csse3200.game.entities.configs;
import java.util.List;

public class CollectablesConfig {
    public String id;
    public String name;
    public String sprite;
    public String description;
    public boolean autoConsume;
    public int ttl = 0;
    public List<String> tags;
    public List<EffectConfig> effects;
    public List<Integer> glowColor;
}