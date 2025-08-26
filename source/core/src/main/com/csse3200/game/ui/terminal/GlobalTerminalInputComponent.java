package com.csse3200.game.ui.terminal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.csse3200.game.input.InputComponent;

public class GlobalTerminalInputComponent extends InputComponent {
  private static final int TOGGLE_KEY = Input.Keys.GRAVE; // ` key
  private Terminal terminal;
  private boolean ctrlHeld = false;

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
    if (keycode == TOGGLE_KEY && ctrlHeld) {
      TerminalService.toggle();
      return true;
    } else if (keycode == Input.Keys.CONTROL_LEFT || keycode == Input.Keys.CONTROL_RIGHT) {
      ctrlHeld = true;
    } else if (Input.Keys.toString(keycode).length() == 1 ) {
      TerminalService.focusTerminalInput();
    }

    return false;
  }

  @Override
  public boolean keyUp(int keycode) {
    if (keycode == Input.Keys.CONTROL_LEFT || keycode == Input.Keys.CONTROL_RIGHT) {
      ctrlHeld = false;
    } else if (Input.Keys.toString(keycode).length() == 1 ) {
      TerminalService.focusTerminalInput();
    }
    return false;
  }

  @Override
  public boolean keyTyped(char character) {
    if (!terminal.isOpen()) {
      return false;
    }

    if ((character == '\r' || character == '\n') && ctrlHeld) {
      // Command execution may take a long time
      Gdx.app.postRunnable(TerminalService::executeCurrentCommand);
    } else {
      TerminalService.focusTerminalInput();
      return false;
    }

    return true;
  }
}
