package com.csse3200.game.input;

import com.badlogic.gdx.Input;

import java.util.HashMap;
import java.util.Map;

/*
 * Static class that manages all keybindings for specific actions within the game
 */
public class Keymap {

  /**
   * Hash map:
   * Keys are the name of a certain action.
   * Values are the integer key codes for a specific key
   */
  private static final Map<String, Integer> keyMap = new HashMap<>();

  /**
   * Keymap should never be instantiated
   */
  private Keymap() {
    throw new IllegalStateException("Cannot instantiate static Keymap class");
  }

  /**
   * Registers a new action into the keymap with a null key code (-1). Returns false if the action
   * is already in the keymap.
   *
   * @param actionName The name of the action to be added to the map.
   * @return Boolean for if the action was registered (true) or not (false).
   */
  private static boolean registerAction(String actionName) {
    // If action has already been registered, do nothing and return false
    if (keyMap.containsKey(actionName)) {
      return false;
    }

    // Create mapping with null keycode, return true
    keyMap.put(actionName, -1);
    return true;
  }

  /**
   * Registers a new action into the keymap with a given key code. Returns false if the action is
   * already in the keymap or if the key code is already in use.
   *
   * @param actionName The name of the action to be added to the map.
   * @param keyCode    The key code to be assigned to the action.
   * @return Boolean for if the action and key were registered/set correctly (true) or not
   * (false).
   */
  public static boolean registerAction(String actionName, int keyCode) {
    // Register action, return false if unsuccessful
    if (!registerAction(actionName)) {
      return false;
    }

    // Try to set keycode for action, return false if unsuccessful
    return setActionKeyCode(actionName, keyCode);
  }

  /**
   * Sets a key code as the value for a key labelled actionName. Returns false if the action does
   * not exist, or if the key code has already been assigned to an action.
   *
   * @param actionName The action whose value is being altered.
   * @param keyCode    The key code to be assigned to the action.
   * @return Boolean for if the key code was set successfully (true) or not (false).
   */
  public static boolean setActionKeyCode(String actionName, int keyCode) {
    // If action does not exist, do nothing and return false
    if (!keyMap.containsKey(actionName)) {
      return false;
    }

    // Check to see if key code is already a value in map, return false and do nothing if so
    for (Integer value : keyMap.values()) {
      if (value == keyCode) {
        return false;
      }
    }

    // Otherwise, set key code to specified action.
    keyMap.put(actionName, keyCode);
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
    // Key code is -1 if action does not exist or key code not set for action
    if (!keyMap.containsKey(actionName) || keyMap.get(actionName) == null) {
      return -1;
    }

    return keyMap.get(actionName);
  }

  /**
   * Attempts to register and set key codes for all actions in the game. Any actions that require
   * a keybind in the game should be set here with there default key codes.
   */
  public static void setKeyMapDefaults() {
    // Player keybindings
    registerAction("PlayerUp", Input.Keys.W);
    registerAction("PlayerDown", Input.Keys.S);
    registerAction("PlayerLeft", Input.Keys.A);
    registerAction("PlayerRight", Input.Keys.D);
    registerAction("PlayerAttack", Input.Keys.SPACE);
    registerAction("PlayerInteract", Input.Keys.E);

    // Debug Terminal keybindings
    registerAction("TerminalModifier", Input.Keys.CONTROL_LEFT);
    registerAction("TerminalModifierAlt", Input.Keys.CONTROL_RIGHT);
    registerAction("TerminalToggle", Input.Keys.GRAVE);
  }
}
