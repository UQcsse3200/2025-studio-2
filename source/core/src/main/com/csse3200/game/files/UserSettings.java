package com.csse3200.game.files;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.csse3200.game.files.FileLoader.Location;
import com.csse3200.game.input.Keymap;

import java.io.File;
import java.util.Map;

/**
 * Reading, Writing, and applying user settings in the game.
 */
public class UserSettings {
  private static final String ROOT_DIR = "CSSE3200Game";
  private static final String SETTINGS_FILE = "settings.json";

  private static final int WINDOW_WIDTH = 1280;
  private static final int WINDOW_HEIGHT = 800;

  /**
   * Get the stored user settings
   * @return Copy of the current settings
   */
  public static Settings get() {
    String path = ROOT_DIR + File.separator + SETTINGS_FILE;
    Settings fileSettings = FileLoader.readClass(Settings.class, path, Location.EXTERNAL);
    // Use default values if file doesn't exist
    return fileSettings != null ? fileSettings : new Settings();
  }

  /**
   * Set the stored user settings
   * @param settings New settings to store
   * @param applyImmediate true to immediately apply new settings.
   */
  public static void set(Settings settings, boolean applyImmediate) {
    String path = ROOT_DIR + File.separator + SETTINGS_FILE;
    FileLoader.writeClass(settings, path, Location.EXTERNAL);

    if (applyImmediate) {
      applySettings(settings);
    }
  }

  /**
   * Apply the given settings without storing them.
   * @param settings Settings to apply
   */
  public static void applySettings(Settings settings) {
    Gdx.graphics.setForegroundFPS(settings.fps);
    Gdx.graphics.setVSync(settings.vsync);

    DisplayMode displayMode = findMatching(settings.displayMode);
    if (displayMode == null) {
        displayMode = Gdx.graphics.getDisplayMode();
    }

    if (settings.fullscreen) {
      Gdx.graphics.setFullscreenMode(displayMode);
    } else {
      Gdx.graphics.setWindowedMode(displayMode.width, displayMode.height);
    }
  }

  private static DisplayMode findMatching(DisplaySettings desiredSettings) {
    if (desiredSettings == null) {
      return null;
    }
    for (DisplayMode displayMode : Gdx.graphics.getDisplayModes()) {
      if (displayMode.refreshRate == desiredSettings.refreshRate
          && displayMode.height == desiredSettings.height
          && displayMode.width == desiredSettings.width) {
        return displayMode;
      }
    }

    return null;
  }

  /**
   * Returns the normalized music volume stored in the user settings. That is, the music volume is
   * a fraction of it's intended volume according to the master volume.
   * @return The normalized volume for the music.
   */
  public static float getMusicVolumeNormalized() {
    Settings settings = get();
    return settings.musicVolume * settings.masterVolume;
  }

  /**
   * Returns the multiplier corresponding to master volume. All sound effects' volume should be
   * multiplied by this float before they are played.
   * Example: A return value of 0.5 means the user has set the master volume to 50%, and thus all
   * sounds should be reduced by 50%.
   * @return The master volume
   */
  public static float getMasterVolume() {
    Settings settings = get();
    return settings.masterVolume;
  }

  /**
   * Stores game settings, can be serialised/deserialised.
   */
  public static class Settings {
    /**
     * FPS cap of the game. Independant of screen FPS.
     */
    public int fps = 60;
    public boolean fullscreen = true;
    public boolean vsync = true;
    /**
     * ui Scale. Currently unused, but can be implemented.
     */
//    public float uiScale = 1f;
    public DisplaySettings displayMode = null;

    /*
     * Members for controlling volume of sound effects.
     */
    public float masterVolume = 1f;
    public float musicVolume = 1f;
  }

  /**
   * Stores chosen display settings. Can be serialised/deserialised.
   */
  public static class DisplaySettings {
    public int width;
    public int height;
    public int refreshRate;

    public DisplaySettings() {}

    public DisplaySettings(DisplayMode displayMode) {
      this.width = displayMode.width;
      this.height = displayMode.height;
      this.refreshRate = displayMode.refreshRate;
    }
  }

  private UserSettings() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
