package com.csse3200.game.input;


import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(GameExtension.class)
class InputFactoryTest {

  @Test
  void shouldGiveKeyboardFactoryType() {
    InputFactory keyboardInputFactory = InputFactory.create();
    assertTrue(keyboardInputFactory instanceof KeyboardInputFactory);
  }
}
