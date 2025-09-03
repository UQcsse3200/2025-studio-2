package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.Input;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
public class SettingsTabTest {

  @Test
  @DisplayName("Should create settings tab without errors")
  void shouldCreateSettingsTab() {
    // Test basic object creation
    SettingsTab settingsTab = new SettingsTab();
    assertNotNull(settingsTab);
  }

  @Test
  @DisplayName("Should dispose without errors")
  void shouldDisposeWithoutErrors() {
    SettingsTab settingsTab = new SettingsTab();
    assertDoesNotThrow(settingsTab::dispose);
  }

  @Test
  @DisplayName("Should handle updateKeyBindButton method call")
  void shouldHandleUpdateKeyBindButton() {
    SettingsTab settingsTab = new SettingsTab();
    // This should not throw an exception even without building the UI
    assertDoesNotThrow(() -> settingsTab.updateKeyBindButton("PlayerUp", Input.Keys.X));
  }
}
