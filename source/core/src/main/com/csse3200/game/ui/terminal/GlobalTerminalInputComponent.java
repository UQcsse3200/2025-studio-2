package com.csse3200.game.ui.terminal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.input.Keymap;

public class GlobalTerminalInputComponent extends InputComponent {
  private Terminal terminal;
  private boolean modifierHeld = false;

  public GlobalTerminalInputComponent() {
    // Highest priority to capture input first
    super(Integer.MAX_VALUE);
  }

  @Override
  public void create() {
    super.create();
    terminal = entity.getComponent(Terminal.class);
  }

  @Override
  public boolean keyDown(int keycode) {
    if (keycode == Keymap.getActionKeyCode("TerminalToggle") && modifierHeld) {
      TerminalService.toggle();
      return true;
    } else if (keycode == Keymap.getActionKeyCode("TerminalModifier")
            || keycode == Keymap.getActionKeyCode("TerminalModifierAlt")) {
      modifierHeld = true;
    }
    handleFocusChange(keycode);
    return false;
  }

  @Override
  public boolean keyUp(int keycode) {
    if (keycode == Keymap.getActionKeyCode("TerminalModifier")
            || keycode == Keymap.getActionKeyCode("TerminalModifierAlt")) {
      modifierHeld = false;
    }
    handleFocusChange(keycode);
    return false;
  }

  private void handleFocusChange(int keycode) {
    if (!terminal.isOpen()) return;

    final String key = Input.Keys.toString(keycode);
    boolean isCtrlC = modifierHeld && ("c".equals(key) || "C".equals(key));
    if (key.length() == 1 && !isCtrlC) {
      TerminalService.focusTerminalInput();
    }
  }

  @Override
  public boolean keyTyped(char character) {
    if (!terminal.isOpen()) return false;

    if ((character == '\r' || character == '\n') && modifierHeld) {
      // Command execution may take a long time
      Gdx.app.postRunnable(TerminalService::executeCurrentCommand);
    } else {
      TerminalService.focusTerminalInput();
      return false;
    }

    return true;
  }
}
