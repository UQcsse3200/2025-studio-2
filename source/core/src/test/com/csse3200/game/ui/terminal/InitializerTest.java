package com.csse3200.game.ui.terminal;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class InitializerTest {
  @Test
  void initializes() {
    Shell shell = com.csse3200.game.ui.terminal.Initializer.getInitializedShell();
  }
}