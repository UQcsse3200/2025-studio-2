package com.csse3200.game.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Service for managing all codex entries in the game
 */
public class CodexService implements Disposable {
    /**
     * Map that maps an entry title to an entry content's (text, unlock status)
     */
    private final Map<String, CodexEntry> entries = new LinkedHashMap<>();
    /**
     * Logger object for creating errors if any occur
     */
    private static final Logger logger = LoggerFactory.getLogger(CodexService.class);

    /**
     * Returns an entry held by the service using the key (or id) of the entry.
     * @param id The id of the entry.
     * @return All entries.
     */
    public CodexEntry getEntry(String id) throws IOException {
        CodexEntry entry = entries.get(id);

        // Create error if entry with that title does not exist.
        if (entry == null) {
            throw new IOException("No entry with id '" + id + "' in codex entries");
        }

        return entry;
    }

    /**
     * Reads the contents of a file and interprets it as ID, title, and text for entries.
     *
     * @param filePath The path to the file to read, with respect to the resources root.
     */
    public void loadEntries(String filePath) {
        // Read the contents of the file
        String fileContents = Gdx.files.internal(filePath).readString();
        String[] fileLines = fileContents.split("\\r?\\n");

        // Iterate through file by three lines each
        for (int i = 0; i + 2 < fileLines.length; i += 3) {
            String id = fileLines[i];
            String title = fileLines[i + 1];
            String text = fileLines[i + 2];

            // Prevent adding entries with no ID or title
            if (id != null && !id.trim().isEmpty() && title != null) {
                entries.put(id, new CodexEntry(title, text));
            }
        }
    }

    /**
     * Clears the map of all entries.
     */
    @Override
    public void dispose() {
        entries.clear();
    }
}

