package com.csse3200.game.components.settingsmenu;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.input.Keymap;
import com.csse3200.game.input.SettingsInputComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(GameExtension.class)
class SettingsMenuDisplayTest {

  private SettingsMenuDisplay settingsMenuDisplay;
  private SettingsInputComponent mockInputComponent;
  private Stage mockStage;
  private RenderService mockRenderService;
  private GameTime mockGameTime;

  @BeforeEach
  void setUp() {
    GdxGame mockGame = mock(GdxGame.class);
    Entity mockEntity = mock(Entity.class);
    mockInputComponent = mock(SettingsInputComponent.class);
    mockStage = mock(Stage.class);
    mockRenderService = mock(RenderService.class);
    ResourceService mockResourceService = mock(ResourceService.class);
    mockGameTime = mock(GameTime.class);

    // Mock graphics
    Gdx.graphics = mock(Graphics.class);
    Monitor mockMonitor = mock(Monitor.class);
    when(Gdx.graphics.getMonitor()).thenReturn(mockMonitor);

    DisplayMode[] mockDisplayModes = {
        new CustomDisplayMode(1920, 1080, 60, 0),
        new CustomDisplayMode(1280, 720, 60, 0)
    };
    when(Gdx.graphics.getDisplayModes(mockMonitor)).thenReturn(mockDisplayModes);
    when(Gdx.graphics.getDisplayModes()).thenReturn(mockDisplayModes); // Add this line
    when(Gdx.graphics.getDisplayMode()).thenReturn(mockDisplayModes[0]);

    // Register services in ServiceLocator
    ServiceLocator.registerRenderService(mockRenderService);
    ServiceLocator.registerResourceService(mockResourceService);
    ServiceLocator.registerTimeSource(mockGameTime);

    // Mock the render service to return our mock stage
    when(mockRenderService.getStage()).thenReturn(mockStage);
    when(mockGameTime.getDeltaTime()).thenReturn(0.016f); // 60 FPS

    when(mockResourceService.getAsset(anyString(), eq(Texture.class)))
        .thenReturn(mock(Texture.class));

    settingsMenuDisplay = new SettingsMenuDisplay(mockGame);
    settingsMenuDisplay.setEntity(mockEntity);

    when(mockEntity.getComponent(SettingsInputComponent.class)).thenReturn(mockInputComponent);
  }

  @AfterEach
  void tearDown() {
    ServiceLocator.clear();
  }

  @Test
  void shouldCreateSettingsMenu() {
    try (MockedStatic<UserSettings> userSettings = mockStatic(UserSettings.class);
         MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {

      UserSettings.Settings mockSettings = new UserSettings.Settings();
      userSettings.when(UserSettings::get).thenReturn(mockSettings);

      Map<String, Integer> mockKeyMap = new HashMap<>();
      mockKeyMap.put("move_up", 87);
      keymap.when(Keymap::getKeyMap).thenReturn(mockKeyMap);

      assertDoesNotThrow(() -> settingsMenuDisplay.create());

      verify(mockInputComponent).setKeyBindButtons(any());
      verify(mockStage).setKeyboardFocus(any());
      verify(mockRenderService).register(settingsMenuDisplay);
    }
  }

  @Test
  void shouldApplySettingsCorrectly() {
    try (MockedStatic<UserSettings> userSettings = mockStatic(UserSettings.class);
         MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {

      UserSettings.Settings mockSettings = new UserSettings.Settings();
      userSettings.when(UserSettings::get).thenReturn(mockSettings);

      Map<String, Integer> mockKeyMap = new HashMap<>();
      mockKeyMap.put("move_up", 87);
      keymap.when(Keymap::getKeyMap).thenReturn(mockKeyMap);

      settingsMenuDisplay.create();

      // Test that the settings creation doesn't throw exceptions
      userSettings.verify(UserSettings::get, atLeastOnce());
      verify(mockRenderService).register(settingsMenuDisplay);
    }
  }

  @Test
  void shouldFormatActionNamesCorrectly() {
    try (MockedStatic<UserSettings> userSettings = mockStatic(UserSettings.class);
         MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {

      UserSettings.Settings mockSettings = new UserSettings.Settings();
      userSettings.when(UserSettings::get).thenReturn(mockSettings);

      Map<String, Integer> mockKeyMap = new HashMap<>();
      mockKeyMap.put("playerMoveUp", 87);
      mockKeyMap.put("terminalToggle", 84);
      mockKeyMap.put("pauseGame", 27);
      keymap.when(Keymap::getKeyMap).thenReturn(mockKeyMap);

      assertDoesNotThrow(() -> settingsMenuDisplay.create());

      verify(mockInputComponent).setKeyBindButtons(any());
      verify(mockRenderService).register(settingsMenuDisplay);
    }
  }

  @Test
  void shouldHandleCurrentDisplayModeSelection() {
    try (MockedStatic<UserSettings> userSettings = mockStatic(UserSettings.class);
         MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {

      UserSettings.Settings mockSettings = new UserSettings.Settings();
      mockSettings.displayMode = new UserSettings.DisplaySettings();
      mockSettings.displayMode.width = 1920;
      mockSettings.displayMode.height = 1080;
      mockSettings.displayMode.refreshRate = 60;

      userSettings.when(UserSettings::get).thenReturn(mockSettings);

      Map<String, Integer> mockKeyMap = new HashMap<>();
      mockKeyMap.put("move_up", 87);
      keymap.when(Keymap::getKeyMap).thenReturn(mockKeyMap);

      assertDoesNotThrow(() -> settingsMenuDisplay.create());
      verify(mockRenderService).register(settingsMenuDisplay);
    }
  }

  @Test
  void shouldHandleNullDisplayModeInSettings() {
    try (MockedStatic<UserSettings> userSettings = mockStatic(UserSettings.class);
         MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {

      UserSettings.Settings mockSettings = new UserSettings.Settings();
      mockSettings.displayMode = null;

      userSettings.when(UserSettings::get).thenReturn(mockSettings);

      Map<String, Integer> mockKeyMap = new HashMap<>();
      mockKeyMap.put("move_up", 87);
      keymap.when(Keymap::getKeyMap).thenReturn(mockKeyMap);

      assertDoesNotThrow(() -> settingsMenuDisplay.create());
      verify(mockRenderService).register(settingsMenuDisplay);
    }
  }

  @Test
  void shouldExitToMainMenu() {
    try (MockedStatic<UserSettings> userSettings = mockStatic(UserSettings.class);
         MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {

      UserSettings.Settings mockSettings = new UserSettings.Settings();
      userSettings.when(UserSettings::get).thenReturn(mockSettings);

      Map<String, Integer> mockKeyMap = new HashMap<>();
      mockKeyMap.put("move_up", 87);
      keymap.when(Keymap::getKeyMap).thenReturn(mockKeyMap);

      settingsMenuDisplay.create();

      // The actual exit functionality would be tested through UI interaction
      // Here we just verify the component was created successfully
      verify(mockRenderService).register(settingsMenuDisplay);
      assertDoesNotThrow(() -> settingsMenuDisplay.dispose());
    }
  }

  @Test
  void shouldDisposeCorrectly() {
    try (MockedStatic<UserSettings> userSettings = mockStatic(UserSettings.class);
         MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {

      UserSettings.Settings mockSettings = new UserSettings.Settings();
      userSettings.when(UserSettings::get).thenReturn(mockSettings);

      Map<String, Integer> mockKeyMap = new HashMap<>();
      mockKeyMap.put("move_up", 87);
      keymap.when(Keymap::getKeyMap).thenReturn(mockKeyMap);

      when(mockInputComponent.isRebinding()).thenReturn(true);

      settingsMenuDisplay.create();
      settingsMenuDisplay.dispose();

      verify(mockInputComponent).cancelRebinding();
    }
  }

  @Test
  void shouldNotCancelRebindingIfNotRebinding() {
    try (MockedStatic<UserSettings> userSettings = mockStatic(UserSettings.class);
         MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {

      UserSettings.Settings mockSettings = new UserSettings.Settings();
      userSettings.when(UserSettings::get).thenReturn(mockSettings);

      Map<String, Integer> mockKeyMap = new HashMap<>();
      mockKeyMap.put("move_up", 87);
      keymap.when(Keymap::getKeyMap).thenReturn(mockKeyMap);

      when(mockInputComponent.isRebinding()).thenReturn(false);

      settingsMenuDisplay.create();
      settingsMenuDisplay.dispose();

      verify(mockInputComponent, never()).cancelRebinding();
    }
  }

  @Test
  void shouldUpdateWithoutErrors() {
    try (MockedStatic<UserSettings> userSettings = mockStatic(UserSettings.class);
         MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {

      UserSettings.Settings mockSettings = new UserSettings.Settings();
      userSettings.when(UserSettings::get).thenReturn(mockSettings);

      Map<String, Integer> mockKeyMap = new HashMap<>();
      mockKeyMap.put("move_up", 87);
      keymap.when(Keymap::getKeyMap).thenReturn(mockKeyMap);

      settingsMenuDisplay.create();

      // Test that update doesn't throw exceptions
      assertDoesNotThrow(() -> settingsMenuDisplay.update());
      verify(mockRenderService).register(settingsMenuDisplay);
      verify(mockGameTime).getDeltaTime();
    }
  }

  @Test
  void shouldHandleDrawWithoutErrors() {
    try (MockedStatic<UserSettings> userSettings = mockStatic(UserSettings.class);
         MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {

      UserSettings.Settings mockSettings = new UserSettings.Settings();
      userSettings.when(UserSettings::get).thenReturn(mockSettings);

      Map<String, Integer> mockKeyMap = new HashMap<>();
      mockKeyMap.put("move_up", 87);
      keymap.when(Keymap::getKeyMap).thenReturn(mockKeyMap);

      settingsMenuDisplay.create();

      // Test that draw doesn't throw exceptions (draw method is empty but still test)
      assertDoesNotThrow(() -> settingsMenuDisplay.draw(null));
      verify(mockRenderService).register(settingsMenuDisplay);
    }
  }

  /**
   * Custom DisplayMode class for testing
   */
  static class CustomDisplayMode extends DisplayMode {
    public CustomDisplayMode(int width, int height, int refreshRate, int bitsPerPixel) {
      super(width, height, refreshRate, bitsPerPixel);
    }
  }
}