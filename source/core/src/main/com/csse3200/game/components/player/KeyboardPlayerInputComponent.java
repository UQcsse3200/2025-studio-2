package com.csse3200.game.components.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.input.Keymap;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.Vector2Utils;
import com.csse3200.game.components.player.InventoryComponent;
import java.lang.reflect.Array;
import java.security.Key;
import java.util.Arrays;
import java.util.HashMap;

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
  private final int UP_KEY = Keymap.getActionKeyCode("PlayerUp");
  private final int DOWN_KEY = Keymap.getActionKeyCode("PlayerDown");
  private final int ENTER_CHEAT_KEY = Keymap.getActionKeyCode("Enter");
  private final int GLIDE_KEY = Keymap.getActionKeyCode("Glide");
  private int[] CHEAT_INPUT_HISTORY = new int[4];
  private int cheatPosition = 0;
  private Boolean cheatsOn = false;

  private HashMap<Integer, Boolean> pressedKeys = new HashMap<>();

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
    } else if (keycode == LEFT_KEY) {
      walkDirection.add(Vector2Utils.LEFT);
      triggerWalkEvent();
    } else if (keycode == RIGHT_KEY) {
      walkDirection.add(Vector2Utils.RIGHT);
      triggerWalkEvent();
    } else if (keycode == INTERACT_KEY) {
      entity.getEvents().trigger("interact");
    } else if (keycode == ADRENALINE_KEY) {
      triggerAdrenalineEvent();
    } else if (keycode == DASH_KEY) {
        triggerDashEvent();
    } else if (keycode == CROUCH_KEY) {
      triggerCrouchEvent();
    } else if (keycode == RESET_KEY) {
        entity.getEvents().trigger("reset"); // This might cause a memory leak?
    } else if (keycode == GLIDE_KEY) {
      triggerGlideEvent(true);
    }
    // Sprint: TAB (and optionally a Keymap binding named "PlayerSprint")
    else if (keycode == Keys.TAB || keycode == Keymap.getActionKeyCode("PlayerSprint")) {
      entity.getEvents().trigger("sprintStart");
    } else if (keycode == UP_KEY) {
      CHEAT_INPUT_HISTORY = addToCheatHistory(CHEAT_INPUT_HISTORY, cheatPosition, UP_KEY);
      cheatPosition++;

      triggerJetpackEvent();

      if (cheatsOn) {
        walkDirection.add(Vector2Utils.UP);
      }

      triggerWalkEvent();
    } else if (keycode == DOWN_KEY) {

      CHEAT_INPUT_HISTORY = addToCheatHistory(CHEAT_INPUT_HISTORY, cheatPosition, DOWN_KEY);
      cheatPosition++;
      if (cheatsOn) {
        walkDirection.add(Vector2Utils.DOWN);
        triggerWalkEvent();
      }
    } else if (keycode == ENTER_CHEAT_KEY) {
      enableCheats();
    }
    else {
      return false;
    }

    // Mark key as pressed
    pressedKeys.put(keycode, true);
    return true;
  }

  /**
   * Triggers player events on specific keycodes.
   *
   * @return whether the input was processed
   * @see InputProcessor#keyUp(int)
   */
  @Override
  public boolean keyUp(int keycode) {
    // If the key hasn't been pressed but has somehow been released then it's a legacy input
    // from an earlier KeyboardPlayerInputComponent
    if ((pressedKeys.get(keycode) == null) || (!pressedKeys.get(keycode))) {
      return false;
    }

    if (keycode == LEFT_KEY) {
      walkDirection.sub(Vector2Utils.LEFT);
      triggerWalkEvent();
    } else if (keycode == RIGHT_KEY) {
      walkDirection.sub(Vector2Utils.RIGHT);
      triggerWalkEvent();
    } else if (keycode == UP_KEY) {
      triggerJetpackOffEvent();
      if (cheatsOn) {
        walkDirection.sub(Vector2Utils.UP);
        triggerWalkEvent();
      }
    } else if (keycode == DOWN_KEY) {
      if (cheatsOn) {
        walkDirection.sub(Vector2Utils.DOWN);
        triggerWalkEvent();
      }
    } else if (keycode == com.badlogic.gdx.Input.Keys.TAB) {
      // Stop sprinting when Tab is released
      entity.getEvents().trigger("sprintStop");
    } else if (keycode == Keys.TAB || keycode == Keymap.getActionKeyCode("PlayerSprint")) {
      entity.getEvents().trigger("sprintStop");
    } else if (keycode == GLIDE_KEY) {
      triggerGlideEvent(false);
    }
    else {
      return false;
    }

    // Mark key as released
    pressedKeys.put(keycode, false);
    return true;
  }

  private void triggerWalkEvent() {
    if (walkDirection.epsilonEquals(Vector2.Zero)) {
      entity.getEvents().trigger("walkStop");
    } else {
      entity.getEvents().trigger("walk", walkDirection);
    }
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
    if (entity.getComponent(InventoryComponent.class).hasItem(InventoryComponent.Bag.UPGRADES,"dash")) {
      entity.getEvents().trigger("dash");
    }
  }

  private void triggerCrouchEvent() {
    entity.getEvents().trigger("crouch");

  }

  private void triggerGlideEvent(boolean status) {
    if (entity.getComponent(InventoryComponent.class).hasItem(InventoryComponent.Bag.UPGRADES,"glider")) {
      entity.getEvents().trigger("glide", status);
    }
  }

  private void triggerJetpackEvent() {

    if (entity.getComponent(InventoryComponent.class).hasItem(InventoryComponent.Bag.UPGRADES,"jetpack")) {
      entity.getEvents().trigger("jetpackOn");
    }

  }

  private void triggerJetpackOffEvent() {

    entity.getEvents().trigger("jetpackOff");
  }

  private int[] addToCheatHistory(int[] keyHistory, int position, int input) {
    if (position > 3) {
        for (int i = 1; i < 3; i ++) {
          keyHistory[i] = keyHistory[i + 1];
        }
        keyHistory[3] = input;
    } else {
      keyHistory[position] = input;
    }


    return keyHistory;
  }

  public int[] getInputHistory() {
    return CHEAT_INPUT_HISTORY;
  }

  public Boolean getIsCheatsOn() {
    return cheatsOn;
  }
  private void enableCheats() {
    if (Arrays.equals(CHEAT_INPUT_HISTORY, new int[]{UP_KEY, UP_KEY, DOWN_KEY, UP_KEY})){
      cheatsOn = !cheatsOn;
      entity.getEvents().trigger("gravityForPlayerOff");
    }
  }
}
