package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.pausemenu.PauseMenuDisplay;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.CodexEntry;
import com.csse3200.game.services.CodexService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        skin.add("default", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        skin.add("title", new Label.LabelStyle(new BitmapFont(), Color.WHITE));

        // Instantiate codex tab
        codexTab = new CodexTab(mockPauseMenuDisplay);
    }

    private Table mockBuildAndVerify(List<CodexEntry> unlocked, List<CodexEntry> all) {
        // Build the codex tab and ensure not null and is table
        Actor rootActor = codexTab.build(skin);
        assertNotNull(rootActor);
        assertInstanceOf(Table.class, rootActor);

        // Ensure scroll focus set to be on codex tab's scroll pane
        verify(mockStage).setScrollFocus(any(ScrollPane.class));

        // Traverse table and verify widgets
        Table rootTable = (Table) rootActor;
        Table tableHolder = (Table) rootTable.getChildren().get(0);

        // Check counter label
        Table titleHolder = (Table) tableHolder.getChildren().get(0);
        Label counterLabel = (Label) titleHolder.getChildren().get(1);
        assertEquals(unlocked.size() + "/" + all.size(), counterLabel.getText().toString());

        // Check scroll pane holds correct number of entries
        ScrollPane scrollPane = (ScrollPane) tableHolder.getChildren().get(1);
        Table logicalTable = (Table) scrollPane.getActor();
        assertEquals(unlocked.size(), logicalTable.getChildren().size);

        return logicalTable;
    }

    private void redirectCodexService(List<CodexEntry> unlocked, List<CodexEntry> all) {
        // Mock getting unlocked entries from codex
        when(mockCodexService.getEntries(true)).thenReturn(unlocked);
        // Mock getting all entries from codex (contents don't matter, just size)
        // Assume there a five entries overall
        when(mockCodexService.getEntries(false)).thenReturn(all);
        // Mock getting number of unlocked entries
        when(mockCodexService.getUnlockedCount()).thenReturn(unlocked.size());
    }

    @Test
    @DisplayName("Build the Codex Tab with at least one unlocked codex entry")
    void buildWithUnlocked() {
        try (MockedStatic<ServiceLocator> mockLocator = mockStatic(ServiceLocator.class)) {
            // Get mocked codex service when accessing from service locator
            mockLocator.when(ServiceLocator::getCodexService).thenReturn(mockCodexService);

            // Mock UNLOCKED entries for codex
            List<CodexEntry> unlocked = new ArrayList<>();
            unlocked.add(new CodexEntry("Test 1", "this is test 1"));
            unlocked.add(new CodexEntry("Test 2", "this is test 2"));

            // Mock ALL entries for codex
            List<CodexEntry> all = new ArrayList<>(unlocked);
            all.add(new CodexEntry("Test 3", "this is test 3"));

            // Redirect Codex Service methods to use mocked entries
            redirectCodexService(unlocked, all);

            // Mock table building process and verify shared widgets between tests
            Table logicalTable = mockBuildAndVerify(unlocked, all);

            // Check labels representing entries match entry data
            for (int i = 0; i < unlocked.size(); i++) {
                // Ensure title matches
                Table entryTable = (Table) logicalTable.getChildren().get(i);
                Table titleTable = (Table) entryTable.getChildren().get(0);
                Label titleLabel = (Label) titleTable.getChildren().get(0);
                assertEquals(unlocked.get(i).getTitle(), titleLabel.getText().toString());

                // Ensure text body matches
                Label textLabel = (Label) entryTable.getChildren().get(1);
                assertEquals(unlocked.get(i).getText(), textLabel.getText().toString());
            }
        }
    }

    @Test
    @DisplayName("Build the Codex Tab with zero unlocked codex entries")
    void buildNoUnlocked() {

    }
}
