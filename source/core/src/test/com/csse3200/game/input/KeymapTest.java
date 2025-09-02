package com.csse3200.game.input;


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
  private Map<String, Integer> originalMap;

  @BeforeEach
  @DisplayName("Add test mappings to key map")
  void createTestMap() {
    Keymap.getKeyMap().put("TestAction1", 1);
    Keymap.getKeyMap().put("TestAction2", 2);

    originalMap = new HashMap<>(Keymap.getKeyMap());
  }

  @AfterEach
  @DisplayName("Clear test map before next test")
  void resetTestMap() {
    Keymap.getKeyMap().clear();
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
    assertFalse(Keymap.registerAction("TestAction1", 3));
    // Attempt to register with already bound key code
    assertFalse(Keymap.registerAction("TestAction3", 2));

    // Ensure the key map has not changed
    assertEquals(originalMap, Keymap.getKeyMap());

    // Add new valid entry
    assertTrue(Keymap.registerAction("TestAction3", 3));

    // Ensure the key map is different to original
    assertNotEquals(originalMap, Keymap.getKeyMap());
  }

  @Test
  @DisplayName("Update action with a key code")
  void updateActionTest() {
    // Attempt to change key code for non-existent action name
    assertFalse(Keymap.setActionKeyCode("TestAction3", 3));
    // Attempt to change key code for already bound key code
    assertFalse(Keymap.registerAction("TestAction1", 2));

    // Ensure key map has not changed
    assertEquals(originalMap, Keymap.getKeyMap());

    // Update action with valid key code
    assertTrue(Keymap.setActionKeyCode("TestAction1", 3));

    // Ensure the key map is different to original
    assertNotEquals(originalMap, Keymap.getKeyMap());
  }

  @Test
  @DisplayName("Retrieve key code from action")
  void getKeyCodeTest() {
    // Attempt to get key code for non-existent action
    assertEquals(-1, Keymap.getActionKeyCode("TestAction3"));

    // Ensure key map has not changed
    assertEquals(originalMap, Keymap.getKeyMap());

    // Attempt to get key code for existent action
    assertEquals(1, Keymap.getActionKeyCode("TestAction1"));

    // Ensure key map has not changed
    assertEquals(originalMap, Keymap.getKeyMap());
  }
}
