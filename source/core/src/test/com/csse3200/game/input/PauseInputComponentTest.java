package com.csse3200.game.input;

import com.badlogic.gdx.Input;
import com.csse3200.game.components.pausemenu.PauseMenuDisplay;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.screens.MainGameScreen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
public class PauseInputComponentTest {
  // Keycodes for testing
  private static final int SETTINGS_KEY = Input.Keys.ESCAPE;
  private static final int INVENTORY_KEY = Input.Keys.I;

  // Mock the main game screen
  @Mock
  MainGameScreen mockGameScreen;

  // Holds the input component being tested
  private PauseInputComponent pauseInputComponent;

  @BeforeEach
  void setup() {
    // Mock the keymap (IDE complains if done without try-catch)
    try (MockedStatic<Keymap> mockedKeymap = mockStatic(Keymap.class)) {
      mockedKeymap.when(() -> Keymap.getActionKeyCode("PauseSettings")).thenReturn(SETTINGS_KEY);
      mockedKeymap.when(() -> Keymap.getActionKeyCode("PauseInventory")).thenReturn(INVENTORY_KEY);

      // Create new pause input component
      pauseInputComponent = new PauseInputComponent(mockGameScreen);
    }
  }

  @Test
  @DisplayName("Keycodes that aren't considered a pause key should do nothing")
  void shouldIgnoreNonPauseKey() {
    // Press a key not mapped to any pause action
    assertFalse(pauseInputComponent.keyDown(Input.Keys.A));

    // Verify no pause-related methods were called
    verify(mockGameScreen, never()).togglePaused();
    verify(mockGameScreen, never()).togglePauseMenu(any(PauseMenuDisplay.Tab.class));
  }

  @Test
  @DisplayName("Pause-related key should open menu")
  void shouldPauseAndOpenMenuOnFirstPress() {
    // Assume game is not paused initially
    when(mockGameScreen.isPaused()).thenReturn(false);

    // Pressing a pause key should be handled
    assertTrue(pauseInputComponent.keyDown(SETTINGS_KEY));

    // Verify game state is toggled and the correct menu tab is opened
    verify(mockGameScreen).togglePaused();
    verify(mockGameScreen).togglePauseMenu(PauseMenuDisplay.Tab.SETTINGS);
  }

  @Test
  @DisplayName("Pause with some key, followed by same key, should unpause")
  void shouldTogglePauseOnSecondPressOfSameKey() {
    // First press to pause the game
    when(mockGameScreen.isPaused()).thenReturn(false);

    // First press of key
    assertTrue(pauseInputComponent.keyDown(SETTINGS_KEY));
    // Second press of the same key
    assertTrue(pauseInputComponent.keyDown(SETTINGS_KEY));

    // Verify pause is toggled twice
    verify(mockGameScreen, times(2)).togglePaused();
    verify(mockGameScreen, times(2)).togglePauseMenu(PauseMenuDisplay.Tab.SETTINGS);
  }

  @Test
  @DisplayName("Pause with some key, followed by different key, should switch tab")
  void shouldSwitchTabsWithoutTogglingPauseWhenAlreadyPaused() {
    // First, pause the game with one key
    when(mockGameScreen.isPaused()).thenReturn(false);
    assertTrue(pauseInputComponent.keyDown(SETTINGS_KEY));
    verify(mockGameScreen, times(1)).togglePaused();

    // Now, assume the game is paused
    when(mockGameScreen.isPaused()).thenReturn(true);

    // Press a different pause key to switch tabs
    assertTrue(pauseInputComponent.keyDown(INVENTORY_KEY));

    // Verify the pause state was NOT toggled again, as a different key was pressed
    verify(mockGameScreen, times(1)).togglePaused();
    // Verify the menu was told to switch to the new tab
    verify(mockGameScreen).togglePauseMenu(PauseMenuDisplay.Tab.INVENTORY);
  }
}
