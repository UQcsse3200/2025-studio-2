package com.csse3200.game.ui.cutscene;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
public class CutsceneReaderComponentTest {
    // Dummy path to cutscene script
    private static final String DUMMY_PATH = "cutscene-scripts/test.txt";
    // Component instance being tested
    CutsceneReaderComponent cutsceneReader;
    // Mocks for LibGDX dependencies
    @Mock
    private Files mockFiles;
    @Mock
    private FileHandle mockFileHandle;

    @BeforeEach
    void setUp() {
        // Prevents null pointer exception for mockFiles
        MockitoAnnotations.openMocks(this);

        // Mock static Gdx.files field
        Gdx.files = mockFiles;
        // Make internal() return mock file handle
        when(mockFiles.internal(DUMMY_PATH)).thenReturn(mockFileHandle);
    }

    @AfterEach
    void reset() {
        // Revert static Gdx.files field
        Gdx.files = null;
    }

    /**
     * Mocks the content that is read from a script file.
     *
     * @param content A string containing contents of read file.
     */
    private void mockScriptContent(String content) {
        when(mockFileHandle.readString()).thenReturn(content);
    }

    @Test
    @DisplayName("Valid script file is correctly parsed")
    void validScriptParsesCorrectly() {
        // Content to be tested
        String script = String.join(System.lineSeparator(),
                "#images/background1.png",
                "Hello, world!",
                "#images/background2.png",
                "Second text box.",
                "Third text box."
        );
        mockScriptContent(script);

        // Create reader with dummy path and parse
        cutsceneReader = new CutsceneReaderComponent(DUMMY_PATH);
        cutsceneReader.create();
        List<CutsceneReaderComponent.TextBox> textBoxList = cutsceneReader.getTextBoxes();

        // There should be items inside text box list
        assertNotNull(textBoxList);
        // There should be three items inside text box list
        assertEquals(3, textBoxList.size());

        // First item in list
        assertEquals("Hello, world!", textBoxList.getFirst().text());
        assertEquals("images/background1.png", textBoxList.getFirst().background());

        // Second item in list
        assertEquals("Second text box.", textBoxList.get(1).text());
        assertEquals("images/background2.png", textBoxList.get(1).background());

        // Third item in list
        assertEquals("Third text box.", textBoxList.get(2).text());
        assertNull(textBoxList.get(2).background());
    }

    @Test
    @DisplayName("Empty script file is handled correctly")
    void emptyFileHandled() {
        // Content being tested
        when(mockFileHandle.readString()).thenReturn("");

        // Create reader with dummy path
        cutsceneReader = new CutsceneReaderComponent(DUMMY_PATH);

        // Ensure exception was caught when created and text box list empty
        assertDoesNotThrow(() -> cutsceneReader.create());
        assertTrue(cutsceneReader.getTextBoxes().isEmpty());
    }

    @Test
    @DisplayName("First line not command is handled correctly")
    void firstLineNotCommandHandled() {
        // Content being tested
        String script = "Not a command.";
        mockScriptContent(script);

        // Create reader with dummy path
        cutsceneReader = new CutsceneReaderComponent(DUMMY_PATH);

        // Ensure exception was caught when created and text box list empty
        assertDoesNotThrow(() -> cutsceneReader.create());
        assertTrue(cutsceneReader.getTextBoxes().isEmpty());
    }

    @Test
    @DisplayName("Text box uses most recently set background")
    void textBoxUsesMostRecentBackground() {
        // Content to be tested
        String script = String.join(System.lineSeparator(),
                "#images/background1.png",
                "#images/background2.png",
                "Text box."
        );
        mockScriptContent(script);

        // Create reader with dummy path and parse
        cutsceneReader = new CutsceneReaderComponent(DUMMY_PATH);
        cutsceneReader.create();
        List<CutsceneReaderComponent.TextBox> textBoxList = cutsceneReader.getTextBoxes();

        // Ensure text box list contains items, and only contains one item
        assertNotNull(textBoxList);
        assertEquals(1, textBoxList.size());

        // Ensure item uses 'images/background2.png'
        assertEquals("images/background2.png", textBoxList.getFirst().background());
    }

    @Test
    @DisplayName("Script file only containing commands results in empty text box list")
    void emptyTextBoxListForOnlyCommands() {
        // Content to be tested
        String script = String.join(System.lineSeparator(),
                "#images/background1.png",
                "#images/background2.png",
                "#images/background3.png",
                "#images/background4.png"
        );
        mockScriptContent(script);

        // Create reader with dummy path and parse
        cutsceneReader = new CutsceneReaderComponent(DUMMY_PATH);
        cutsceneReader.create();

        // Ensure text box list is empty
        assertTrue(cutsceneReader.getTextBoxes().isEmpty());
    }
}
