package com.csse3200.game.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Service for managing all codex entries in the game
 */
public class CodexService implements Disposable {
    /**
     * Map that maps an entry title to an entry content's (text, unlock status)
     */
    private final Map<String, CodexEntry> entries;
    /**
     * Logger object for creating errors if any occur
     */
    private static final Logger logger = LoggerFactory.getLogger(CodexService.class);
    /**
     * Constructor for the codex service. Initialises a hash map for all entries
     */
    public CodexService() {
        entries = new LinkedHashMap<>();
    }
    /**
     * Returns an entry held by the service using the key (or id) of the entry.
     * @param id The id of the entry.
     * @return All entries.
     */
    public CodexEntry getEntry(String id) {
        CodexEntry entry = entries.get(id);

        // Create error if entry with that title does not exist.
        if (entry != null) {
            logger.error("Entry with id {} does not exist.", id);
        }

        return entry;
    }

    /**
     * Reads the contents of a file and interprets it as title/text for entries. Even numbered lines
     * are the title of the entry, subsequent line is considered text description.
     *
     * @param filePath The path to the file to read, with respect to the resources root.
     */
    public void loadEntries(String filePath) {
        // Read the contents of the file
        String fileContents = Gdx.files.internal(filePath).readString();
        String[] fileLines = fileContents.split("\\r?\\n");

        // Keep track of line index, and last recorded title in file
        int lineIndex = 0;
        String titleBuffer = null;
        String idBuffer = null;

        // Iterate through file line by line
        for (String line : fileLines) {
            if (lineIndex % 3 == 0) {
                idBuffer = line;
            } else if (lineIndex % 3 == 1) {
                titleBuffer = line;
            } else {
                // Enough information for entry - add to codex map
                entries.put(idBuffer, new CodexEntry(titleBuffer, line));
                titleBuffer = null;
                idBuffer = null;
            }

            lineIndex++;
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

