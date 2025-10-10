package com.csse3200.game.input;

import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@ExtendWith(GameExtension.class)
class KeyboardInputFactoryTest {
  @Test
  void shouldReturnKeyboardPlayerInput() {
    KeyboardInputFactory keyboardInputFactory = new KeyboardInputFactory();
    assertInstanceOf(KeyboardPlayerInputComponent.class, keyboardInputFactory.createForPlayer());
  }
}
