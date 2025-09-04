package com.csse3200.game.files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.UserSettings.DisplaySettings;
import com.csse3200.game.files.UserSettings.KeyBindSettings;
import com.csse3200.game.files.UserSettings.Settings;
import com.csse3200.game.input.Keymap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(GameExtension.class)
class UserSettingsTest {

  @BeforeEach
  void setUp() {
    // Clear any existing settings before each test
    try (MockedStatic<FileLoader> fileLoader = mockStatic(FileLoader.class)) {
      fileLoader.when(() -> FileLoader.readClass(eq(Settings.class), anyString(), any()))
          .thenReturn(null);
    }
  }

  @Test
  void shouldApplySettings() {
    Gdx.graphics = mock(Graphics.class);
    DisplayMode displayMode = mock(DisplayMode.class);
    when(Gdx.graphics.getDisplayMode()).thenReturn(displayMode);

    Settings settings = new Settings();
    settings.vsync = true;
    settings.displayMode = null;
    settings.fullscreen = true;
    settings.fps = 40;
    UserSettings.applySettings(settings);

    verify(Gdx.graphics).setForegroundFPS(settings.fps);
    verify(Gdx.graphics).setFullscreenMode(displayMode);
    verify(Gdx.graphics).setVSync(settings.vsync);
  }

  @Test
  void shouldFindMatchingDisplay() {
    Gdx.graphics = mock(Graphics.class);
    DisplayMode correctMode = new CustomDisplayMode(200, 100, 60, 0);
    DisplayMode[] displayModes = {
        new CustomDisplayMode(100, 200, 30, 0),
        new CustomDisplayMode(100, 200, 60, 0),
        correctMode
    };
    when(Gdx.graphics.getDisplayModes()).thenReturn(displayModes);

    Settings settings = new Settings();
    settings.displayMode = new DisplaySettings();
    settings.displayMode.height = 100;
    settings.displayMode.width = 200;
    settings.displayMode.refreshRate = 60;
    settings.fullscreen = true;
    UserSettings.applySettings(settings);

    verify(Gdx.graphics).setFullscreenMode(correctMode);
  }

  @Test
  void shouldReturnDefaultSettingsWhenFileNotExists() {
    try (MockedStatic<FileLoader> fileLoader = mockStatic(FileLoader.class)) {
      fileLoader.when(() -> FileLoader.readClass(eq(Settings.class), anyString(), any()))
          .thenReturn(null);

      Settings settings = UserSettings.get();

      assertEquals(60, settings.fps);
      assertTrue(settings.fullscreen);
      assertTrue(settings.vsync);
      assertEquals(1f, settings.masterVolume);
      assertEquals(1f, settings.musicVolume);
      assertNull(settings.displayMode);
      assertNull(settings.keyBindSettings);
    }
  }

  @Test
  void shouldSaveAndApplySettings() {
    try (MockedStatic<FileLoader> fileLoader = mockStatic(FileLoader.class)) {
      Gdx.graphics = mock(Graphics.class);
      DisplayMode displayMode = mock(DisplayMode.class);
      when(Gdx.graphics.getDisplayMode()).thenReturn(displayMode);

      Settings settings = new Settings();
      settings.fps = 120;
      settings.fullscreen = false;
      settings.vsync = false;
      settings.masterVolume = 0.8f;
      settings.musicVolume = 0.6f;

      UserSettings.set(settings, true);

      verify(Gdx.graphics).setForegroundFPS(120);
      verify(Gdx.graphics).setVSync(false);
      fileLoader.verify(() -> FileLoader.writeClass(eq(settings), anyString(), any()));
    }
  }

  @Test
  void shouldSaveWithoutApplying() {
    try (MockedStatic<FileLoader> fileLoader = mockStatic(FileLoader.class)) {
      Gdx.graphics = mock(Graphics.class);

      Settings settings = new Settings();
      settings.fps = 120;

      UserSettings.set(settings, false);

      verify(Gdx.graphics, never()).setForegroundFPS(anyInt());
      fileLoader.verify(() -> FileLoader.writeClass(eq(settings), anyString(), any()));
    }
  }

  @Test
  void shouldApplyKeybindSettings() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {
      Gdx.graphics = mock(Graphics.class);
      DisplayMode displayMode = mock(DisplayMode.class);
      when(Gdx.graphics.getDisplayMode()).thenReturn(displayMode);

      Settings settings = new Settings();
      settings.keyBindSettings = new KeyBindSettings();
      settings.keyBindSettings.customKeybinds = new HashMap<>();
      settings.keyBindSettings.customKeybinds.put("move_up", 87); // W key
      settings.keyBindSettings.customKeybinds.put("move_down", 83); // S key

      UserSettings.applySettings(settings);

      keymap.verify(() -> Keymap.setActionKeyCode("move_up", 87));
      keymap.verify(() -> Keymap.setActionKeyCode("move_down", 83));
    }
  }

  @Test
  void shouldNotApplyKeybindsWhenNull() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {
      Gdx.graphics = mock(Graphics.class);
      DisplayMode displayMode = mock(DisplayMode.class);
      when(Gdx.graphics.getDisplayMode()).thenReturn(displayMode);

      Settings settings = new Settings();
      settings.keyBindSettings = null;

      UserSettings.applySettings(settings);

      keymap.verify(() -> Keymap.setActionKeyCode(anyString(), anyInt()), never());
    }
  }

  @Test
  void shouldSaveCurrentKeybinds() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class);
         MockedStatic<FileLoader> fileLoader = mockStatic(FileLoader.class)) {

      Map<String, Integer> currentKeymap = new HashMap<>();
      currentKeymap.put("move_up", 87);
      currentKeymap.put("move_down", 83);
      keymap.when(Keymap::getKeyMap).thenReturn(currentKeymap);

      Settings mockSettings = new Settings();
      fileLoader.when(() -> FileLoader.readClass(eq(Settings.class), anyString(), any()))
          .thenReturn(mockSettings);

      UserSettings.saveCurrentKeybinds();

      assertNotNull(mockSettings.keyBindSettings);
      assertEquals(currentKeymap, mockSettings.keyBindSettings.customKeybinds);
      fileLoader.verify(() -> FileLoader.writeClass(eq(mockSettings), anyString(), any()));
    }
  }

  @Test
  void shouldResetKeybindsToDefaults() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class);
         MockedStatic<FileLoader> fileLoader = mockStatic(FileLoader.class)) {

      Settings mockSettings = new Settings();
      mockSettings.keyBindSettings = new KeyBindSettings();
      mockSettings.keyBindSettings.customKeybinds = new HashMap<>();
      mockSettings.keyBindSettings.customKeybinds.put("move_up", 87);

      fileLoader.when(() -> FileLoader.readClass(eq(Settings.class), anyString(), any()))
          .thenReturn(mockSettings);

      UserSettings.resetKeybindsToDefaults();

      assertNull(mockSettings.keyBindSettings.customKeybinds);
      keymap.verify(Keymap::clearKeyMap);
      keymap.verify(Keymap::setKeyMapDefaults);
      fileLoader.verify(() -> FileLoader.writeClass(eq(mockSettings), anyString(), any()));
    }
  }

  @Test
  void shouldInitialiseKeybinds() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class);
         MockedStatic<FileLoader> fileLoader = mockStatic(FileLoader.class)) {

      Settings mockSettings = new Settings();
      mockSettings.keyBindSettings = new KeyBindSettings();
      mockSettings.keyBindSettings.customKeybinds = new HashMap<>();
      mockSettings.keyBindSettings.customKeybinds.put("move_up", 87);

      fileLoader.when(() -> FileLoader.readClass(eq(Settings.class), anyString(), any()))
          .thenReturn(mockSettings);

      UserSettings.initialiseKeybinds();

      keymap.verify(Keymap::setKeyMapDefaults);
      keymap.verify(() -> Keymap.setActionKeyCode("move_up", 87));
    }
  }

  @Test
  void shouldCalculateNormalizedMusicVolume() {
    try (MockedStatic<FileLoader> fileLoader = mockStatic(FileLoader.class)) {
      Settings mockSettings = new Settings();
      mockSettings.masterVolume = 0.8f;
      mockSettings.musicVolume = 0.6f;

      fileLoader.when(() -> FileLoader.readClass(eq(Settings.class), anyString(), any()))
          .thenReturn(mockSettings);

      float normalized = UserSettings.getMusicVolumeNormalized();

      assertEquals(0.48f, normalized, 0.001f);
    }
  }

  @Test
  void shouldReturnMasterVolume() {
    try (MockedStatic<FileLoader> fileLoader = mockStatic(FileLoader.class)) {
      Settings mockSettings = new Settings();
      mockSettings.masterVolume = 0.75f;

      fileLoader.when(() -> FileLoader.readClass(eq(Settings.class), anyString(), any()))
          .thenReturn(mockSettings);

      float masterVolume = UserSettings.getMasterVolume();

      assertEquals(0.75f, masterVolume, 0.001f);
    }
  }

  @Test
  void shouldHandleNullDisplayModeInSettings() {
    Gdx.graphics = mock(Graphics.class);
    DisplayMode currentDisplayMode = new CustomDisplayMode(1920, 1080, 60, 0);
    when(Gdx.graphics.getDisplayMode()).thenReturn(currentDisplayMode);

    Settings settings = new Settings();
    settings.fullscreen = true;
    settings.displayMode = null;

    UserSettings.applySettings(settings);

    verify(Gdx.graphics).setFullscreenMode(currentDisplayMode);
  }

  /**
   * This exists to make the constructor public
   */
  static class CustomDisplayMode extends DisplayMode {
    public CustomDisplayMode(int width, int height, int refreshRate, int bitsPerPixel) {
      super(width, height, refreshRate, bitsPerPixel);
    }
  }
}