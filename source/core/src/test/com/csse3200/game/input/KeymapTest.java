package com.csse3200.game.input;


import com.badlogic.gdx.Input;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
public class KeymapTest {

  @BeforeEach
  @DisplayName("Add test mappings to key map")
  void createTestMap() {
    Keymap.setKeyMapDefaults();
  }

  @AfterEach
  @DisplayName("Clear test map before next test")
  void resetTestMap() {
    Keymap.clearKeyMap();
  }

  @Test
  @DisplayName("Class should never initialise")
  void neverInitialiseTest() {
    assertThrows(IllegalStateException.class, Keymap::new);
  }


  @Test
  @DisplayName("Register action with a keycode")
  void registerActionTest() {
    // Attempt to register with pre-existing action name
    assertFalse(Keymap.registerAction("PlayerUp", Input.Keys.UP, true));
    // Attempt to register with already bound key code
    assertFalse(Keymap.registerAction("PlayerUpTwo", Keymap.getActionKeyCode("PlayerUp"), true));

    // Add new valid entry
    assertTrue(Keymap.registerAction("PlayerUpTwo", Input.Keys.UP, true));
  }

  @Test
  @DisplayName("Update action with a key code")
  void updateActionTest() {
    // Attempt to change key code for non-existent action name
    assertFalse(Keymap.setActionKeyCode("PlayerUpTwo", Input.Keys.UP));
    // Attempt to change key code for already bound key code
    assertFalse(Keymap.setActionKeyCode("PlayerUp", Keymap.getActionKeyCode("PlayerDown")));

    // Update action with valid key code
    assertTrue(Keymap.setActionKeyCode("PlayerUp", Input.Keys.W));
  }

  @Test
  @DisplayName("Retrieve key code from action")
  void getKeyCodeTest() {
    // Attempt to get key code for non-existent action
    assertEquals(-1, Keymap.getActionKeyCode("PlayerUpTwo"));

    // Attempt to get key code for existent action
    assertEquals(Input.Keys.W, Keymap.getActionKeyCode("PlayerUp"));
  }

  @Test
  @DisplayName("Get display-friendly values from keymap")
  void getKeymapTest() {
    Map<String, Integer> displayable = Keymap.getKeyMap();

    // Check to see if displayable action is present
    assertTrue(displayable.containsKey("PlayerUp"));
    // Check to see if non-displayable action is present
    assertFalse(displayable.containsKey("TerminalModifier"));
  }
}
