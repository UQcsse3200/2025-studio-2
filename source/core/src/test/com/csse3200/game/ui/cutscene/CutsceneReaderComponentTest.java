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
}
