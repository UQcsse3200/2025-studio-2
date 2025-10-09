package com.csse3200.game.input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InputFactory creates inputType-specific inputFactories which can handle various types of input.
 * Currently only keyboard input is implemented, but InputFactory can be expanded to
 * include more, e.g. touch gestures.
 *
 * <p>Methods to get new input handlers should be defined here.
 */
public abstract class InputFactory {
  private static final Logger logger = LoggerFactory.getLogger(InputFactory.class);

  /**
   * @return an KeyboardInputFactory
   */
  public static InputFactory create() {
      return new KeyboardInputFactory();
  }

  /**
   * Creates an input handler for the player
   *
   * @return Player input handler
   */
  public abstract InputComponent createForPlayer();
}
