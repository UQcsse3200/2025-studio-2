package com.csse3200.game.components.player;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.input.Keymap;
import com.csse3200.game.utils.math.Vector2Utils;

/**
 * Input handler for the player for keyboard and touch (mouse) input.
 * This input handler only uses keyboard input.
 */
public class KeyboardPlayerInputComponent extends InputComponent {
  private final Vector2 walkDirection = Vector2.Zero.cpy();

  public KeyboardPlayerInputComponent() {
    super(5);
  }

  /**
   * Triggers player events on specific keycodes.
   *
   * @return whether the input was processed
   * @see InputProcessor#keyDown(int)
   */
  @Override
  public boolean keyDown(int keycode) {
    if (keycode == Keymap.getActionKeyCode("PlayerUp")) {
      walkDirection.add(Vector2Utils.UP);
      triggerWalkEvent();
      return true;
    } else if (keycode == Keymap.getActionKeyCode("PlayerLeft")) {
      walkDirection.add(Vector2Utils.LEFT);
      triggerWalkEvent();
      return true;
    } else if (keycode == Keymap.getActionKeyCode("PlayerDown")) {
      walkDirection.add(Vector2Utils.DOWN);
      triggerWalkEvent();
      return true;
    } else if (keycode == Keymap.getActionKeyCode("PlayerRight")) {
      walkDirection.add(Vector2Utils.RIGHT);
      triggerWalkEvent();
      return true;
    } else if (keycode == Keymap.getActionKeyCode("PlayerAttack")) {
      entity.getEvents().trigger("attack");
      return true;
    } else if (keycode == Keymap.getActionKeyCode("PlayerInteract")) {
      entity.getEvents().trigger("interact");
      return true;
    }
    else if (keycode == Keys.SHIFT_LEFT || keycode == Keys.SHIFT_RIGHT) {
      entity.getEvents().trigger("sprintStart");
      return true;
    }

    return false;
  }

  /**
   * Triggers player events on specific keycodes.
   *
   * @return whether the input was processed
   * @see InputProcessor#keyUp(int)
   */
  @Override
  public boolean keyUp(int keycode) {
    if (keycode == Keymap.getActionKeyCode("PlayerUp")) {
      walkDirection.sub(Vector2Utils.UP);
      triggerWalkEvent();
      return true;
    } else if (keycode == Keymap.getActionKeyCode("PlayerLeft")) {
      walkDirection.sub(Vector2Utils.LEFT);
      triggerWalkEvent();
      return true;
    } else if (keycode == Keymap.getActionKeyCode("PlayerDown")) {
      walkDirection.sub(Vector2Utils.DOWN);
      triggerWalkEvent();
      return true;
    } else if (keycode == Keymap.getActionKeyCode("PlayerRight")) {
      walkDirection.sub(Vector2Utils.RIGHT);
      triggerWalkEvent();
      return true;
    }
    else if (keycode == Keys.SHIFT_LEFT || keycode == Keys.SHIFT_RIGHT) {
      entity.getEvents().trigger("sprintStop");
      return true;
    }

    return false;
  }

  private void triggerWalkEvent() {
    if (walkDirection.epsilonEquals(Vector2.Zero)) {
      entity.getEvents().trigger("walkStop");
    } else {
      entity.getEvents().trigger("walk", walkDirection);
    }
  }
}
