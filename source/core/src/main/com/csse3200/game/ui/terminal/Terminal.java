package com.csse3200.game.ui.terminal;

import com.csse3200.game.components.Component;

import java.util.ArrayList;

/**
 * A component that holds the state of the debug terminal.
 * This includes the history of messages, the current input line, and its open/closed state.
 */
public class Terminal extends Component {
  private static final int MAX_HISTORY = 10000; // 10k history commands

  private final ArrayList<String> commandHistory = new ArrayList<>();
  private boolean isOpen = false;

  public boolean isOpen() {
    return isOpen;
  }

  public void toggleIsOpen() {
    isOpen = !isOpen;
  }

  public void addCommandToHistory(String command) {
    while (commandHistory.size() >= MAX_HISTORY) {
      commandHistory.removeFirst();
    }
    commandHistory.add(command);
  }
}
