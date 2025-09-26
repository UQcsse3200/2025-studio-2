package com.csse3200.game.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
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
     * Constructor for the codex service. Initialises a hash map for all entries
     */
    public CodexService() {
        entries = new HashMap<>();
    }

    /**
     * Reads the contents of a file and interprets it as title/text for entries. Even numbered lines
     * are the title of the entry, subsequent line is considered text description.
     * @param filePath The path to the file to read, with respect to the resources root.
     */
    public void loadEntries(String filePath) {
        // Read the contents of the file
        String fileContents = Gdx.files.internal(filePath).readString();
        String[] fileLines = fileContents.split("\\r?\\n");

        // Keep track of line index, and last recorded title in file
        int lineIndex = 0;
        String titleBuffer = null;

        // Iterate through file line by line
        for (String line : fileLines) {
            if (lineIndex % 2 == 0) {
                // Even line numbers are titles
                titleBuffer = line;
            } else {
                // Odd line numbers are text
                // Enough information for entry - add to codex map
                entries.put(titleBuffer, new CodexEntry(line));
                titleBuffer = null;
            }

            lineIndex++;
        }
    }

    /**
     * Clears the map of
     */
    @Override
    public void dispose() {
        entries.clear();
    }
}

