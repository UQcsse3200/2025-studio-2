package com.csse3200.game.services;

import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing all codex entries in the game
 */
public class CodexService implements Disposable {
    /**
     * Map that maps an entry ID to an entry record
     */
    private final Map<String, CodexEntry> entries;

    public CodexService() {
        entries = new HashMap<>();
    }

    public loadEntries

    @Override
    public void dispose() {
        entries.clear();
    }
}

