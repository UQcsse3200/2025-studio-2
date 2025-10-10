package com.csse3200.game.entities.configs;

import java.util.ArrayList;
import java.util.List;

public class ParallaxConfig {
    public List<Layer> layers = new ArrayList<>();
    public List<Layer> overlays = new ArrayList<>();

    public static class Layer {
        public String texture;
        public float factor = 0f;
        public float offsetX = 0f;
        public float offsetY = 0f;
        public float scale = 0f;
        public float coverage = 0f;
        public boolean tiled = false;
    }
}
