package com.csse3200.game.ui.terminal;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class KeyboardTerminalInputComponentTest {
  @Test
  void shouldToggleTerminalOpenClose() {
    Terminal terminal = spy(Terminal.class);
  }
}
