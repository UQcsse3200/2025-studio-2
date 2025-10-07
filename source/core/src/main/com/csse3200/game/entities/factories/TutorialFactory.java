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

  /**
   * Creates a tutorial entity that displays the jump action image and the corresponding key.
   * @return A new jump tutorial entity.
   */
  public static Entity createJumpTutorial() {

    Entity tutorial = new Entity()
        .addComponent(new TutorialComponent("images/tutorials/jump.png", Input.Keys.toString(Keymap.getActionKeyCode("PlayerJump"))));
    tutorial.setScale(0.5f, 0.5f);

    return tutorial;
  }
}
