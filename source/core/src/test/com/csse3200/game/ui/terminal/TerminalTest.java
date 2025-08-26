package com.csse3200.game.ui.terminal;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;

@ExtendWith(GameExtension.class)
class TerminalTest {
  private Terminal terminal;

  @BeforeEach
  void setUp() {
    terminal = new Terminal();
  }

  @Test
  void shouldBeClosedInitially() {
    assertFalse(terminal.isOpen());
  }

  @Test
  void shouldToggleOpen() {
    assertFalse(terminal.isOpen());
    terminal.toggleIsOpen();
    assertTrue(terminal.isOpen());
    terminal.toggleIsOpen();
    assertFalse(terminal.isOpen());
  }

  @Test
  void shouldAddCommandToHistory() throws NoSuchFieldException, IllegalAccessException {
    Field historyField = Terminal.class.getDeclaredField("commandHistory");
    historyField.setAccessible(true);

    @SuppressWarnings("unchecked")
    ArrayList<String> commandHistory = (ArrayList<String>) historyField.get(terminal);

    assertEquals(0, commandHistory.size());
    terminal.addCommandToHistory("test command");
    assertEquals(1, commandHistory.size());
    assertEquals("test command", commandHistory.getFirst());
  }

  @Test
  void shouldRespectMaxHistory() throws NoSuchFieldException, IllegalAccessException {
    Field maxHistoryField = Terminal.class.getDeclaredField("MAX_HISTORY");
    maxHistoryField.setAccessible(true);
    int maxHistory = (int) maxHistoryField.get(null);

    Field historyField = Terminal.class.getDeclaredField("commandHistory");
    historyField.setAccessible(true);
    @SuppressWarnings("unchecked")
    ArrayList<String> commandHistory = (ArrayList<String>) historyField.get(terminal);

    for (int i = 0; i < maxHistory; i++) {
      terminal.addCommandToHistory("command " + i);
    }

    assertEquals(maxHistory, commandHistory.size());
    assertEquals("command 0", commandHistory.getFirst());

    // Add one more to push the first one out
    terminal.addCommandToHistory("new command");
    assertEquals(maxHistory, commandHistory.size());
    assertEquals("command 1", commandHistory.getFirst());
    assertEquals("new command", commandHistory.getLast());
  }

  // Test from the prompt
  @Test
  void shouldSetOpenClosed() {
    Terminal terminal = spy(Terminal.class);
    terminal.toggleIsOpen();
    assertTrue(terminal.isOpen());
    terminal.toggleIsOpen();
    assertFalse(terminal.isOpen());
  }
}