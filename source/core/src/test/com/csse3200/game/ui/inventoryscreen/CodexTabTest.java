package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.csse3200.game.components.pausemenu.PauseMenuDisplay;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.CodexService;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
public class CodexTabTest {
    /**
     * Reference to mocked display class that renders the codex tab
     */
    private PauseMenuDisplay mockPauseMenuDisplay;
    /**
     * Reference to mocked stage which actor built by codex tab is drawn to
     */
    private Stage mockStage;
    /**
     * Reference to mocked codex service that returns all unlocked entries
     */
    private CodexService mockCodexService;
    /**
     * Codex tab and UI skin
     */
    private CodexTab codexTab;
    private Skin skin;

    @BeforeEach
    void setUp() {
        // Mock all mocked classes
        mockPauseMenuDisplay = mock(PauseMenuDisplay.class);
        mockStage = mock(Stage.class);
        mockCodexService = mock(CodexService.class);

        // Return mocked stage when requesting stage from mocked display
        when(mockPauseMenuDisplay.getStage()).thenReturn(mockStage);

        // Create minimal skin for widgets
        skin = new Skin();
        skin.add("default", new Label.LabelStyle());
        skin.add("title", new Label.LabelStyle());

        // Instantiate codex tab
        codexTab = new CodexTab(mockPauseMenuDisplay);
    }
}
