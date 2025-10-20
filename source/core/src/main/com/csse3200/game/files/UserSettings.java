package com.csse3200.game.files;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.audio.Music;
import com.csse3200.game.files.FileLoader.Location;
import com.csse3200.game.input.Keymap;
import com.csse3200.game.services.ServiceLocator;

import java.io.File;
import java.util.HashMap;
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

    applyKeybindSettings(settings.keyBindSettings);
      try {
          String bgm = "sounds/BGM_03_mp3.mp3";
          Music music = ServiceLocator.getResourceService().getAsset(bgm, Music.class);
          if (music != null && music.isPlaying()) {
              music.setVolume(settings.musicVolume);
          }
      } catch (Exception e) {
          // ignore if not loaded
      }
  }

  /**
   * Applies custom keybind settings to the current keymap.
   * Only applies keybinds if custom settings exist, otherwise uses defaults.
   *
   * @param keyBindSettings The keybind settings to apply, may be null
   */
  private static void applyKeybindSettings(KeyBindSettings keyBindSettings) {
    if (keyBindSettings != null && keyBindSettings.customKeybinds != null) {
      // Apply custom keybinds
      for (Map.Entry<String, Integer> entry : keyBindSettings.customKeybinds.entrySet()) {
        Keymap.setActionKeyCode(entry.getKey(), entry.getValue());
      }
    }
  }

  /**
   * Saves the current keymap state to user settings.
   * Creates a snapshot of all current keybinds and stores them as custom settings.
   */
  public static void saveCurrentKeybinds() {
    Settings settings = get();
    if (settings.keyBindSettings == null) {
      settings.keyBindSettings = new KeyBindSettings();
    }

    // get current keybinds from Keymap and save
    settings.keyBindSettings.customKeybinds = new HashMap<>(Keymap.getKeyMap());

  set(settings, false); // save without applying (already applied)
  }

  /**
   * Resets all keybinds to their default values and saves the settings.
   * Clears any custom keybind overrides and restores the original keymap.
   */
  public static void resetKeybindsToDefaults() {
    Settings settings = get();

    // Clear custom keybinds
    if (settings.keyBindSettings != null) {
      settings.keyBindSettings.customKeybinds = null;
    }

    // Reset keymap to defaults
    Keymap.clearKeyMap();
    Keymap.setKeyMapDefaults();

    set(settings, false); // Save the cleared custom keybinds
  }

  /**
   * Initialises keybinds on game startup by applying saved settings or defaults
   */
  public static void initialiseKeybinds() {
    // Set defaults
    Keymap.setKeyMapDefaults();

    // Then apply any saved custom keybinds
    Settings settings = get();
    applyKeybindSettings(settings.keyBindSettings);
  }

  /**
   * Finds a DisplayMode that matches the desired display settings.
   * Searches through available display modes for exact width, height, and refresh rate match.
   *
   * @param desiredSettings The display settings to match against
   * @return Matching DisplayMode or null if no match found
   */
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
    return settings.musicVolume;
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

    /**
     * Members for controlling volume of sound effects.
     */
    public float masterVolume = 1f;
    public float musicVolume = 1f;
    /**
     * Member for Brightness control;
     */
    private float brightnessValue = 0.3f;
    public float getBrightnessValue() {
      return brightnessValue;
    }
    public void setBrightnessValue(float value) {
      brightnessValue = value;
    }

    /**
     * Custom keybinds
     */
    public KeyBindSettings keyBindSettings = null;
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

  /**
   * Stores custom keybind settings. Can be serialised/deserialised.
   */
  public static class KeyBindSettings {
    /**
     * Map of action names to custom key codes.
     * Only stores keybinds that differ from defaults.
     * Null means use all default keybinds.
     */
    public Map<String, Integer> customKeybinds = null;

    public KeyBindSettings() {}
  }

  private UserSettings() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
