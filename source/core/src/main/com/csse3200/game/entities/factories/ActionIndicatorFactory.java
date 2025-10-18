package com.csse3200.game.entities.factories;

import com.badlogic.gdx.Input;
import com.csse3200.game.components.ActionIndicatorComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.Keymap;

/**
 * A factory to create tutorial entities.
 */
public class ActionIndicatorFactory {

  private ActionIndicatorFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }

  private static Entity create(String path, String action) {
    return new Entity().addComponent(new ActionIndicatorComponent(path, Input.Keys.toString(Keymap.getActionKeyCode(action))));
  }

  /**
   * Creates a tutorial entity that displays the jump action image and the corresponding key.
   * @return A new jump tutorial entity.
   */
  public static Entity createJumpTutorial() {
    Entity tutorial = create("images/tutorials/jump.png", "PlayerJump");
    tutorial.setScale(0.5f, 0.5f);
    return tutorial;
  }

  /**
   * Creates a tutorial entity that displays the double jump action image and the corresponding key.
   * @return A new double jump tutorial entity.
   */
  public static Entity createDoubleJumpTutorial() {
    Entity tutorial = create("images/tutorials/double_jump.png", "PlayerJump");
    tutorial.setScale(0.5f, 0.7f);
    return tutorial;
  }

  /**
   * Creates a tutorial entity that displays the dash action image and the corresponding key.
   * @return A new dash tutorial entity.
   */
  public static Entity createDashTutorial() {
    Entity tutorial = create("images/tutorials/dash.png", "PlayerDash");
    tutorial.setScale(1f, 0.5f);
    return tutorial;
  }
}
