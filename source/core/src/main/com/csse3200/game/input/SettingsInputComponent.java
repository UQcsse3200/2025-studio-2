package com.csse3200.game.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.csse3200.game.screens.SettingsScreen;

import java.util.Map;

/**
 * Input handler for settings menu key rebinding functionality.
 */
public class SettingsInputComponent extends InputComponent {

  private Map<String, TextButton> keyBindButtons;
  private String currentlyRebinding = null;
  private String originalButtonText = null;

  public SettingsInputComponent(SettingsScreen settingsScreen) {
    super(20); // High priority to capture input during rebinding
  }

  /**
   * Set the key binding buttons that this component will manage
   * @param keyBindButtons Map of action names to their corresponding buttons
   */
  public void setKeyBindButtons(Map<String, TextButton> keyBindButtons) {
    this.keyBindButtons = keyBindButtons;
  }

  /**
   * Start rebinding process for a specific action
   * @param actionName The action to rebind
   * @param button The button that was clicked
   */
  public void startRebinding(String actionName, TextButton button) {
    if (keyBindButtons == null || !keyBindButtons.containsKey(actionName)) {
      return; // Don't start rebinding if button doesn't exist
    }

    // Cancel any existing rebinding
    if (currentlyRebinding != null) {
      cancelRebinding();
    }

    currentlyRebinding = actionName;
    originalButtonText = button.getText().toString();
    button.setText("Press Key");
  }

  /**
   * Cancel the current rebinding process
   */
  public void cancelRebinding() {
    if (currentlyRebinding != null && keyBindButtons.containsKey(currentlyRebinding)) {
      TextButton button = keyBindButtons.get(currentlyRebinding);
      button.setText(originalButtonText);

      currentlyRebinding = null;
      originalButtonText = null;
    }
  }

  /**
   * Complete the rebinding process with a new key
   * @param newKeyCode The key code to bind to the current action
   */
  private void completeRebinding(int newKeyCode) {
    if (currentlyRebinding == null) return;

    TextButton button = keyBindButtons.get(currentlyRebinding);

    // Check if the key is already bound to another action
    for (Map.Entry<String, Integer> entry : Keymap.getKeyMap().entrySet()) {
      if (!entry.getKey().equals(currentlyRebinding) && entry.getValue() == newKeyCode) {
        cancelRebinding();
        return;
      }
    }

    // Update the keymap
    boolean success = Keymap.setActionKeyCode(currentlyRebinding, newKeyCode);

    if (success) {
      String keyName = Input.Keys.toString(newKeyCode);
      button.setText(keyName);
    } else {
      button.setText(originalButtonText);
    }

    currentlyRebinding = null;
    originalButtonText = null;
  }

  /**
   * Handle key down events for rebinding
   * @param keycode The key code that was pressed
   * @return true if the input was processed, false otherwise
   */
  @Override
  public boolean keyDown(int keycode) {
    if (currentlyRebinding != null) {
      if (keycode == Input.Keys.ESCAPE) {
        cancelRebinding();
      } else {
        completeRebinding(keycode);
      }
      return true; // Consume the input when rebinding
    }
    return false; // Don't consume input when not rebinding
  }

  /**
   * Check if currently in rebinding mode
   * @return true if rebinding is active
   */
  public boolean isRebinding() {
    return currentlyRebinding != null;
  }
}