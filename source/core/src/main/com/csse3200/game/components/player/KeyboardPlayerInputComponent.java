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

  private final int LEFT_KEY = Keymap.getActionKeyCode("PlayerLeft");
  private final int RIGHT_KEY = Keymap.getActionKeyCode("PlayerRight");
  private final int JUMP_KEY = Keymap.getActionKeyCode("PlayerJump");
  private final int INTERACT_KEY = Keymap.getActionKeyCode("PlayerInteract");
  private final int ADRENALINE_KEY = Keymap.getActionKeyCode("PlayerAdrenaline");
  private final int DASH_KEY = Keymap.getActionKeyCode("PlayerDash");
  private final int CROUCH_KEY = Keymap.getActionKeyCode("PlayerCrouch");
  private final int RESET_KEY = Keymap.getActionKeyCode("Reset");

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

    if (keycode == JUMP_KEY) {
      triggerJumpEvent();
      return true;
    } else if (keycode == LEFT_KEY) {
      walkDirection.add(Vector2Utils.LEFT);
      triggerWalkEvent();
      return true;
    } else if (keycode == RIGHT_KEY) {
      walkDirection.add(Vector2Utils.RIGHT);
      triggerWalkEvent();
      return true;
    } else if (keycode == INTERACT_KEY) {
      entity.getEvents().trigger("interact");
    } else if (keycode == ADRENALINE_KEY) {
      triggerAdrenalineEvent();
        return true;
    } else if (keycode == DASH_KEY) {
        triggerDashEvent();
        return true;
    } else if (keycode == CROUCH_KEY) {
      triggerCrouchEvent();
      return true;
      // debug
    } else if (keycode == RESET_KEY) {
        entity.getEvents().trigger("reset"); // This might cause a memory leak?
        return true;
    }
    // Sprint: TAB (and optionally a Keymap binding named "PlayerSprint")
    else if (keycode == Keys.TAB || keycode == Keymap.getActionKeyCode("PlayerSprint")) {
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
      if (keycode == LEFT_KEY) {
        walkDirection.sub(Vector2Utils.LEFT);
        triggerWalkEvent();
        return true;
      } else if (keycode == RIGHT_KEY) {
        walkDirection.sub(Vector2Utils.RIGHT);
        triggerWalkEvent();
        return true;
      }
      else if (keycode == com.badlogic.gdx.Input.Keys.TAB) {
          // Stop sprinting when Tab is released
          entity.getEvents().trigger("sprintStop");
          return true;
     } else if (keycode == Keys.TAB || keycode == Keymap.getActionKeyCode("PlayerSprint")) {
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

  /**
   * Return current walk direction.
   * (Only current use is for transfers between resets.)
   * @return walkDirection
   */
  public Vector2 getWalkDirection() {
    return walkDirection;
  }
  /**
   * Set current walk direction.
   * (Only current use is for transfers between resets.)
   * @param walkDirection - walkDirection to set.
   */

  public void setWalkDirection(Vector2 walkDirection) {
    this.walkDirection.set(walkDirection);
  }

  /**
   * Use this to start a jump event
   */
  private void triggerJumpEvent() {
    entity.getEvents().trigger("jump"); //put jump here

  }

  private void triggerAdrenalineEvent() {
    entity.getEvents().trigger("toggleAdrenaline");
  }

  private void triggerDashEvent() {
    entity.getEvents().trigger("dash");
  }

  private void triggerCrouchEvent() {
    entity.getEvents().trigger("crouch");

  }
}
