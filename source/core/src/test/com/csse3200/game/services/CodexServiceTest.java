package com.csse3200.game.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.csse3200.game.extensions.GameExtension;
import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    @DisplayName("getEntry() returns null for non-existent entry")
    void getEntryReturnsNull() {
        // Service loads empty string
        setupMockFile("");
        CodexService service = new CodexService();

        // Ensure non-existent entry is null
        CodexEntry entry = service.getEntry("test_id_1");
        assertNull(entry);
    }

    @Test
    @DisplayName("getEntries() returns only unlocked entries when requesting unlocked entries")
    void getEntriesReturnsUnlocked() {
        // Service loads mock file
        setupMockFile("test_id_1\nTest Title 1\nTest Content 1");
        CodexService service = new CodexService();

        // Entries are locked by default. Ensure getEntries() is empty
        ArrayList<CodexEntry> result = service.getEntries(true);
        assertTrue(result.isEmpty());
    }
}
