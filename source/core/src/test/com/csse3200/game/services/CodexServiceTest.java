package com.csse3200.game.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
public class CodexServiceTest {
    private void setupMockFile(String content) {
        Gdx.files = mock(com.badlogic.gdx.Files.class);
        FileHandle fileHandle = mock(FileHandle.class);

        // Reading string from file handle returns 'content' instead
        when(fileHandle.readString()).thenReturn(content);
        // Reading a file returns mocked file handle
        when(Gdx.files.internal("codex-entries.txt")).thenReturn(fileHandle);
    }

    @Test
    @DisplayName("getEntry() returns the correct entry")
    void getEntryReturnsCorrectEntry() {
        // Service loads string and creates entries
        setupMockFile("test_id_1\nTest Title 1\nTest Content 1");
        CodexService service = new CodexService();

        // Test whether entry is valid
        CodexEntry entry = service.getEntry("test_id_1");
        assertNotNull(entry);
        assertEquals("Test Title 1", entry.getTitle());
        assertEquals("Test Content 1", entry.getText());
    }
}
