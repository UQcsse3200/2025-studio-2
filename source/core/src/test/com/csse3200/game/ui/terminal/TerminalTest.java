package com.csse3200.game.ui.terminal;

import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.ui.terminal.commands.Command;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class TerminalTest {
  Command command = mock(Command.class);

  @Test
  void shouldSetOpenClosed() {
    Terminal terminal = spy(Terminal.class);

    terminal.setClosed();
    assertFalse(terminal.isOpen());

    terminal.setOpen();
    assertTrue(terminal.isOpen());

    terminal.setClosed();
    assertFalse(terminal.isOpen());
  }

  @Test
  void shouldToggleIsOpen() {
    Terminal terminal = spy(Terminal.class);

    terminal.setClosed();

    terminal.toggleIsOpen();
    assertTrue(terminal.isOpen());
    terminal.toggleIsOpen();
    assertFalse(terminal.isOpen());
  }

  @Test
  void shouldModifyEnteredMessage() {
    Terminal terminal = new Terminal();

    terminal.appendToMessage('a');
    terminal.appendToMessage('b');
    assertEquals("ab", terminal.getEnteredMessage());

    terminal.handleBackspace();
    assertEquals("a", terminal.getEnteredMessage());
  }
}
