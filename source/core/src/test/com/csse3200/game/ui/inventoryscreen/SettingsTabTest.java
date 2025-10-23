package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.Input;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
class SettingsTabTest {

  @Test
  void shouldCreateSettingsTab() {
    // Test basic object creation
    SettingsTab settingsTab = new SettingsTab();
    assertNotNull(settingsTab);
  }

  @Test
  void shouldDisposeWithoutErrors() {
    SettingsTab settingsTab = new SettingsTab();
    assertDoesNotThrow(settingsTab::dispose);
  }

  @Test
  void shouldHandleUpdateKeyBindButton() {
    SettingsTab settingsTab = new SettingsTab();
    // This should not throw an exception even without building the UI
    assertDoesNotThrow(() -> settingsTab.updateKeyBindButton("PlayerUp", Input.Keys.X));
  }
}
