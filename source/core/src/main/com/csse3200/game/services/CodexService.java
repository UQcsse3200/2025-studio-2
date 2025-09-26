package com.csse3200.game.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.ui.cutscene.CutsceneReaderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Logger object for creating errors if any occur
     */
    private static final Logger logger = LoggerFactory.getLogger(CodexService.class);
    /**
     * Constructor for the codex service. Initialises a hash map for all entries
     */
    public CodexService() {
        entries = new HashMap<>();
    }
    /**
     * Returns all entries loaded at this point.
     *
     * @return All entries.
     */
    public CodexEntry getEntry(String title) {
        CodexEntry entry = entries.get(title);

        // Create error if entry with that title does not exist.
        if (entry != null) {
            logger.error("Entry with title {} does not exist.", title);
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
     * Clears the map of all entries.
     */
    @Override
    public void dispose() {
        entries.clear();
    }
}

