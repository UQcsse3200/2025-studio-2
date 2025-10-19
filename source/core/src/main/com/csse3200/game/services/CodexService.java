package com.csse3200.game.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Service for managing all codex entries in the game
 */
public class CodexService implements Disposable {
    /**
     * Logger for error handling
     */
    private static final Logger logger = LoggerFactory.getLogger(CodexService.class);
    /**
     * Map that maps an entry title to an entry content's (text, unlock status)
     */
    private final Map<String, CodexEntry> entries = new LinkedHashMap<>();
    /**
     * A counter for the number of entries that have been unlocked
     */
    private int numUnlocked = 0;

    /**
     * Constructor loads all codex entries from a special file.
     */
    public CodexService() {
        loadEntries();
    }

    /**
     * Returns an entry held by the service using the key (or id) of the entry.
     *
     * @param id The id of the entry.
     * @return The entry with the matching id, or null if it does not exist
     */
    public CodexEntry getEntry(String id) {
        CodexEntry entry = entries.get(id);

        // Create error if entry with that title does not exist.
        if (entry == null) {
            logger.error("No entry with id '{}' in codex entries", id);
        }

        return entry;
    }

    public void incUnlockCount() {
        numUnlocked++;
    }

    public int getUnlockedCount() {
        return numUnlocked;
    }

    /**
     * Returns all unlocked entries currently stored by the service as an array list.
     *
     * @param unlockedOnly Flag for filtering any codex entries which have not been unlocked.
     * @return All unlocked entries stored by service as an array list.
     */
    public List<CodexEntry> getEntries(boolean unlockedOnly) {
        // Turn values in map into stream.
        Stream<CodexEntry> codexEntryStream = entries.values().stream();

        // Filter locked entries if flag is set
        if (unlockedOnly) {
            codexEntryStream = codexEntryStream.filter(CodexEntry::isUnlocked);
        }

        // Sort by the order in which entries became unlocked
        codexEntryStream =
            codexEntryStream.sorted(Comparator.comparing(CodexEntry::getUnlockedIndex));

        // Return stream as array list
        return codexEntryStream.collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Reads the contents of 'codex-entries.txt' and interprets it as ID, title, and text for
     * entries.
     */
    private void loadEntries() {
        // Read the contents of the file
        String fileContents = Gdx.files.internal("codex-entries.txt").readString();
        String[] fileLines = fileContents.split("\\r?\\n");

        // Iterate through file by three lines each
        for (int i = 0; i < fileLines.length; i += 3) {
            // Only parse if there's enough lines left
            if (i + 2 < fileLines.length) {
                String id = fileLines[i];
                String title = fileLines[i + 1];
                String text = fileLines[i + 2];

                // Skip invalid codex entries
                if (id != null && !id.trim().isEmpty() && title != null && !title.trim().isEmpty()) {
                    entries.put(id, new CodexEntry(title, text));
                } else {
                    logger.warn("Skipping malformed codex entry at line {}", i + 1);
                }
            } else {
                logger.warn("Incomplete codex entry found starting at line {}", i + 1);
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

