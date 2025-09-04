package com.csse3200.game.input;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.screens.SettingsScreen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(GameExtension.class)
class SettingsInputComponentTest {

  private SettingsInputComponent settingsInputComponent;
  private SettingsScreen mockScreen;
  private Map<String, TextButton> keyBindButtons;
  private TextButton mockButton;

  @BeforeEach
  void setUp() {
    mockScreen = mock(SettingsScreen.class);
    settingsInputComponent = new SettingsInputComponent(mockScreen);

    keyBindButtons = new HashMap<>();
    mockButton = mock(TextButton.class);
    when(mockButton.getText()).thenReturn(mock(StringBuilder.class));
    when(mockButton.getText().toString()).thenReturn("W");

    keyBindButtons.put("move_up", mockButton);
    settingsInputComponent.setKeyBindButtons(keyBindButtons);
  }

  @Test
  void shouldSetKeyBindButtons() {
    Map<String, TextButton> newButtons = new HashMap<>();
    newButtons.put("move_down", mock(TextButton.class));

    settingsInputComponent.setKeyBindButtons(newButtons);

    // Verify that the component stores the buttons
    assertFalse(settingsInputComponent.isRebinding());
  }

  @Test
  void shouldStartRebinding() {
    settingsInputComponent.startRebinding("move_up", mockButton);

    assertTrue(settingsInputComponent.isRebinding());
    verify(mockButton).setText("Press Key");
  }

  @Test
  void shouldCancelExistingRebindingWhenStartingNew() {
    TextButton anotherButton = mock(TextButton.class);
    when(anotherButton.getText()).thenReturn(mock(StringBuilder.class));
    when(anotherButton.getText().toString()).thenReturn("S");
    keyBindButtons.put("move_down", anotherButton);

    // Start first rebinding
    settingsInputComponent.startRebinding("move_up", mockButton);
    verify(mockButton).setText("Press Key");

    // Start second rebinding should cancel first
    settingsInputComponent.startRebinding("move_down", anotherButton);
    verify(mockButton).setText("W"); // Should restore original text
    verify(anotherButton).setText("Press Key");

    assertTrue(settingsInputComponent.isRebinding());
  }

  @Test
  void shouldCancelRebinding() {
    settingsInputComponent.startRebinding("move_up", mockButton);
    assertTrue(settingsInputComponent.isRebinding());

    settingsInputComponent.cancelRebinding();

    assertFalse(settingsInputComponent.isRebinding());
    verify(mockButton).setText("W"); // Should restore original text
  }

  @Test
  void shouldCompleteRebindingWithValidKey() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {
      Map<String, Integer> mockKeyMap = new HashMap<>();
      mockKeyMap.put("move_down", 83); // S key
      keymap.when(Keymap::getKeyMap).thenReturn(mockKeyMap);
      keymap.when(() -> Keymap.setActionKeyCode("move_up", Input.Keys.A)).thenReturn(true);

      settingsInputComponent.startRebinding("move_up", mockButton);
      assertTrue(settingsInputComponent.isRebinding());

      boolean result = settingsInputComponent.keyDown(Input.Keys.A);

      assertTrue(result);
      assertFalse(settingsInputComponent.isRebinding());
      keymap.verify(() -> Keymap.setActionKeyCode("move_up", Input.Keys.A));
      verify(mockButton).setText("A");
    }
  }

  @Test
  void shouldRejectDuplicateKeyBinding() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {
      Map<String, Integer> mockKeyMap = new HashMap<>();
      mockKeyMap.put("move_down", Input.Keys.S); // S key already bound
      keymap.when(Keymap::getKeyMap).thenReturn(mockKeyMap);

      settingsInputComponent.startRebinding("move_up", mockButton);
      assertTrue(settingsInputComponent.isRebinding());

      boolean result = settingsInputComponent.keyDown(Input.Keys.S);

      assertTrue(result);
      assertFalse(settingsInputComponent.isRebinding());
      keymap.verify(() -> Keymap.setActionKeyCode(anyString(), anyInt()), never());
      verify(mockButton).setText("W"); // Should restore original text
    }
  }

  @Test
  void shouldCancelRebindingOnEscape() {
    settingsInputComponent.startRebinding("move_up", mockButton);
    assertTrue(settingsInputComponent.isRebinding());

    boolean result = settingsInputComponent.keyDown(Input.Keys.ESCAPE);

    assertTrue(result);
    assertFalse(settingsInputComponent.isRebinding());
    verify(mockButton).setText("W"); // Should restore original text
  }

  @Test
  void shouldNotProcessInputWhenNotRebinding() {
    assertFalse(settingsInputComponent.isRebinding());

    boolean result = settingsInputComponent.keyDown(Input.Keys.A);

    assertFalse(result); // Should not consume input
    assertFalse(settingsInputComponent.isRebinding());
  }

  @Test
  void shouldHandleFailedKeyMapping() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {
      Map<String, Integer> mockKeyMap = new HashMap<>();
      keymap.when(Keymap::getKeyMap).thenReturn(mockKeyMap);
      keymap.when(() -> Keymap.setActionKeyCode("move_up", Input.Keys.A)).thenReturn(false);

      settingsInputComponent.startRebinding("move_up", mockButton);

      boolean result = settingsInputComponent.keyDown(Input.Keys.A);

      assertTrue(result);
      assertFalse(settingsInputComponent.isRebinding());
      verify(mockButton).setText("W"); // Should restore original text on failure
    }
  }

  @Test
  void shouldHandleRebindingWithoutButtons() {
    SettingsInputComponent componentWithoutButtons = new SettingsInputComponent(mockScreen);

    componentWithoutButtons.startRebinding("move_up", mockButton);
    componentWithoutButtons.cancelRebinding();

    // Should not throw exception even without buttons set
    assertFalse(componentWithoutButtons.isRebinding());
  }
}