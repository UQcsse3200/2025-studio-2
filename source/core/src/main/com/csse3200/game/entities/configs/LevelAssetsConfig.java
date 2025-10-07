package com.csse3200.game.entities.configs;

import java.util.List;

/**
 * Configuration container for level assets such as textures, atlases, and sounds.
 */
public class LevelAssetsConfig {

    /**
     * File paths for texture assets used in this level
     */
    public List<String> textures;

    /**
     * File paths for texture atlas assets (e.g., .atlas files) used in this level.
     */
    public List<String> atlases;

    /**
     * File paths for sound assets (e.g., WAV or MP3 files) used in this level.
     */
    public List<String> sounds;
}