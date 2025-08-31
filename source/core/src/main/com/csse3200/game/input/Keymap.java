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
  public static boolean registerAction(String actionName, int keyCode) {
    // Return false if action is in keymap
    if (keyMap.containsKey(actionName)) {
      return false;
    }
    // Check if key code is already a value in map
    if (keyMap.containsValue(keyCode)) {
      return false;
    }

    // Register new action
    keyMap.put(actionName, keyCode);
    return true;
  }

  /**
   * Updates an existing action in the keymap with a given key code. Returns false if the action is
   * not in the keymap, or if the key code is already in use.
   *
   * @param actionName The name of the action to be added to the map.
   * @param keyCode    The key code to be assigned to the action.
   * @return Boolean for if the action and key were registered/set correctly (true) or not
   * (false).
   */
  public static boolean setActionKeyCode(String actionName, int keyCode) {
    // Return false if action is not in keymap
    if (!keyMap.containsKey(actionName)) {
      return false;
    }
    // Check if key code is already a value in map
    if (keyMap.containsValue(keyCode)) {
      return false;
    }

    // Register new action
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
    // Key code is -1 if action does not exist
    if (!keyMap.containsKey(actionName)) {
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

    // Pause
    registerAction("PauseGame", Input.Keys.ESCAPE);
  }

  /**
   * Getter function for the keymap.
   * @return The keymap.
   */
  public static Map<String, Integer> getKeyMap() {
    return keyMap;
  }
}
