package com.csse3200.game.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

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
        setupMockFile("test_id_1\nTest Title 1\nTest Content 1\ntest_id_2\nTest Title 2\nTest " +
                "Content 2");
        CodexService service = new CodexService();

        // Unlock exactly one entry
        service.getEntry("test_id_1").setUnlocked();

        // Entries are locked by default. Ensure getEntries() only contains the one unlocked entry
        List<CodexEntry> result = service.getEntries(true);

        assertEquals(1, result.size());
        assertEquals(result.getFirst(), service.getEntry("test_id_1"));
    }

    @Test
    @DisplayName("getEntries() returns all entries when not equesting just unlocked entries")
    void getEntriesReturnsAll() {
        // Service loads mock file
        setupMockFile("test_id_1\nTest Title 1\nTest Content 1\ntest_id_2\nTest Title 2\nTest " +
                "Content 2");
        CodexService service = new CodexService();

        // Unlock exactly one entry
        service.getEntry("test_id_1").setUnlocked();

        // Ensure getEntries() returns all entries
        List<CodexEntry> result = service.getEntries(false);

        assertEquals(2, result.size());
        assertTrue(result.contains(service.getEntry("test_id_1")));
        assertTrue(result.contains(service.getEntry("test_id_2")));
    }

    @Test
    @DisplayName("Disposing the service clears all entries")
    void disposeClearsEntries() {
        // Service loads mock file and is disposed
        setupMockFile("test_id_1\nTest Title 1\nTest Content 1\ntest_id_2\nTest Title 2\nTest " +
                "Content 2");
        CodexService service = new CodexService();
        service.dispose();

        // Ensure all entries are removed
        assertTrue(service.getEntries(false).isEmpty());
    }

    @Test
    @DisplayName("Service ignores entries that are incorrectly formatted")
    void ignoresIncorrectlyFormatted() {
        // Service loads mock file with an entry missing a line
        setupMockFile("test_id_1\nTest Title 1");
        CodexService service = new CodexService();

        // Ensure that no entries have been loaded
        assertTrue(service.getEntries(false).isEmpty());
    }

    @Test
    @DisplayName("Service ignores entries with a missing title/ID")
    void ignoresMissingDetails() {
        // Service loads mock file wth empty strings for an ID and title
        setupMockFile("\nTest Title 1\nTest Content 1\ntest_id_2\n\nTest Content " +
                "2\ntest_id_3\nTest Title 3\nTest Content 3");
        CodexService service = new CodexService();

        // Ensure invalid entries did not load, and valid ones did
        assertEquals(1, service.getEntries(false).size());
        assertNull(service.getEntry("test_id_1"));
        assertNull(service.getEntry("test_id_2"));
        assertNotNull(service.getEntry("test_id_3"));
    }
}
