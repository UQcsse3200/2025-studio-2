package com.csse3200.game.input;

import com.badlogic.gdx.Input;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * Static class that manages all keybindings for specific actions within the game.
 */
public class Keymap {

  /**
   * Linked Hash map (linked to maintain order of registered action):
   * Keys are the name of a certain action.
   * Values are record containing key code action is mapped to and editable flag.
   */
  private static Map<String, KeyBinding> keyMap = new LinkedHashMap<>();

  /**
   * Keymap should never be instantiated
   */
  public Keymap() {
    throw new IllegalStateException("Cannot instantiate static Keymap class");
  }

  /**
   * Registers a new action into the keymap with a given key code. Returns false if the action is
   * already in the keymap, or if the key code is already in use.
   *
   * @param actionName The name of the action to be added to the map.
   * @param keyCode    The key code to be assigned to the action.
   * @return Boolean for if the action and key were registered/set correctly (true) or not
   * (false).
   */
  public static boolean registerAction(String actionName, int keyCode, boolean display) {
    // Return false if action is in keymap
    if (keyMap.containsKey(actionName)) {
      return false;
    }
    // Check if key code is already a value in map
    if (keyMap.values().stream().anyMatch(binding -> binding.keyCode() == keyCode)) {
      return false;
    }

    // Register new action
    keyMap.put(actionName, new KeyBinding(keyCode, display));
    return true;
  }

  /**
   * Updates an existing action in the keymap with a given key code. Returns false if the action is
   * not in the keymap, or if the key code is already in use by a different action. Does not
   * affect the display flag.
   *
   * @param actionName The name of the action to be added to the map.
   * @param keyCode    The key code to be assigned to the action.
   * @return Boolean for if the action and key were registered/set correctly (true) or not
   * (false).
   */
  public static boolean setActionKeyCode(String actionName, int keyCode) {
    // Get existing binding
    KeyBinding existing = keyMap.get(actionName);

    // Return false if action is not in keymap
    if (existing == null) {
      return false;
    }


    // Check if key code is already a value in map
    if (keyMap.entrySet().stream()
            .anyMatch(entry -> !entry.getKey().equals(actionName) && entry.getValue().keyCode() == keyCode)) {
      return false;
    }

    // Register new action
    keyMap.put(actionName, new KeyBinding(keyCode, existing.display()));
    return true;
  }

  /**
   * Returns the keycode associated with an action.
   *
   * @param actionName The name of the action whose key code is being fetched.
   * @return The key code of the associated action. -1 if action does not exist or key code is
   * not set for action.
   */
  public static int getActionKeyCode(String actionName) {
    // Key code is -1 if action does not exist
    if (!keyMap.containsKey(actionName)) {
      return -1;
    }

    return keyMap.get(actionName).keyCode();
  }

  /**
   * Attempts to register and set key codes for all actions in the game. Any actions that require
   * a keybind in the game should be set here with there default key codes.
   */
  public static void setKeyMapDefaults() {
    // Player keybindings
    registerAction("PlayerUp", Input.Keys.W, false);
    registerAction("PlayerDown", Input.Keys.S, false);
    registerAction("PlayerLeft", Input.Keys.A, true);
    registerAction("PlayerRight", Input.Keys.D, true);
    registerAction("PlayerAttack", Input.Keys.Q, true);
    registerAction("PlayerInteract", Input.Keys.E, true);
    registerAction("PlayerJump", Input.Keys.SPACE, true);
    registerAction("PlayerAdrenaline", Input.Keys.V, true);
    registerAction("PlayerDash", Input.Keys.SHIFT_LEFT, true);
    registerAction("PlayerCrouch", Input.Keys.C, true);
    registerAction("Reset", Input.Keys.R, true);
    registerAction("Enter", Input.Keys.ENTER, false);
    registerAction("Grapple", Input.Keys.F, true);

    // Debug Terminal keybindings
    registerAction("TerminalModifier", Input.Keys.CONTROL_LEFT, false);
    registerAction("TerminalModifierAlt", Input.Keys.CONTROL_RIGHT, false);
    registerAction("TerminalToggle", Input.Keys.GRAVE, false);

    // Pause
    registerAction("PauseSettings", Input.Keys.ESCAPE, true);
    registerAction("PauseInventory", Input.Keys.I, true);
    registerAction("PauseMap", Input.Keys.M, true);
    registerAction("PauseUpgrades", Input.Keys.U, true);

  }

  /**
   * Removes all actions and associated keybinds from the map.
   */
  public static void clearKeyMap() {
    keyMap = new LinkedHashMap<>();
  }

  /**
   * Getter method that returns a display-friendly version of the keybinding map. Entries whose
   * display flag is set as false are ignored.
   *
   * @return The hash map with actions mapped to their keycodes.
   */
  public static Map<String, Integer> getKeyMap() {
    // Convert to stream to do functional processing
    // Filter out entries with display flag set as true
    // Then, convert back to map from stream
    return keyMap.entrySet().stream()
            .filter(entry -> entry.getValue().display())
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().keyCode(),
                    (oldValue, newValue) -> oldValue,
                    LinkedHashMap::new
            ));
  }

  /**
   * Record that holds a keycode and flag for whether keybind should be displayed in settings menu.
   *
   * @param keyCode The key code associated with the keybinding.
   * @param display A flag determining if binding should be shown in settings menu.
   */
  private record KeyBinding(int keyCode, boolean display) {
  }
}
