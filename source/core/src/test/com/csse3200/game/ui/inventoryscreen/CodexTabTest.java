package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.csse3200.game.components.pausemenu.PauseMenuDisplay;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.CodexService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

@ExtendWith(GameExtension.class)
public class CodexTabTest {
    /**
     * Reference to mocked display class that renders the codex tab
     */
    @Mock
    private PauseMenuDisplay mockPauseMenuDisplay;

    /**
     * Reference to mocked stage which actor built by codex tab is drawn to
     */
    @Mock
    private Stage mockStage;

    /**
     * Reference to mocked codex service that returns all unlocked entries
     */
    @Mock
    private CodexService mockCodexService;

    /**
     * Codex tab and UI skin
     */
    private CodexTab codexTab;
    private Skin skin;
}
