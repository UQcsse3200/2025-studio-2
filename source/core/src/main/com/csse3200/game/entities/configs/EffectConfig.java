package com.csse3200.game.entities.configs;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines properties for effect types to be loaded in ItemEffectRegistry.
 */
public class EffectConfig {
    public String type;
    public double value;
    public int duration = 0;
    public String target;
    public Map<String, String> params = new HashMap<>();
}
