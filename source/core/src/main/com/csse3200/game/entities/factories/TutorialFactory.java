package com.csse3200.game.entities.factories;

import com.badlogic.gdx.Input;
import com.csse3200.game.input.Keymap;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.TutorialComponent;

/**
 * A factory to create tutorial entities.
 */
public class TutorialFactory {

  private TutorialFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }

  private static Entity create(String path, String action) {
    Entity tutorial = new Entity()
        .addComponent(new TutorialComponent(path, Input.Keys.toString(Keymap.getActionKeyCode(action))));
    tutorial.setScale(0.5f, 0.5f);
    return tutorial;
  }

  /**
   * Creates a tutorial entity that displays the jump action image and the corresponding key.
   * @return A new jump tutorial entity.
   */
  public static Entity createJumpTutorial() {
    return create("images/tutorials/jump.png", "PlayerJump");
  }

  /**
   * Creates a tutorial entity that displays the double jump action image and the corresponding key.
   * @return A new double jump tutorial entity.
   */
  public static Entity createDoubleJumpTutorial() {
    return create("images/tutorials/double_jump.png", "PlayerJump");
  }
}
