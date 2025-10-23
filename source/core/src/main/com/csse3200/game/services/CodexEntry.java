package com.csse3200.game.services;

import com.badlogic.gdx.Gdx;
/**
 * Class representing a codex entry - storing all relevant data for an entry.
 */
public class CodexEntry {
    /**
     * Text contents of codex entry.
     */
    private final String text;
    /**
     * Title of the codex entry.
     */
    private final String title;
    /**
     * Number representing how many other entries had been unlocked before this one.
     */
    private int unlockedIndex;
    /**
     * Flag determining if codex entry has been unlocked.
     */
    private boolean unlocked = false;

    /**
     * Constructor that sets the text contents of the codex.
     * @param text A string (with formatting) for the codex entry.
     */
    public CodexEntry(String title, String text) {
        this.text = text;
        this.title = title;
        this.unlockedIndex = -1; // -1 implies no unlock
    }

    /**
     * Set the unlocked flag to be true - use when player has unlocked entry;
     */
    public void setUnlocked() {
        unlocked = true;

        // Update service unlock count & set unlock index
        unlockedIndex = ServiceLocator.getCodexService().getUnlockedCount();
        ServiceLocator.getCodexService().incUnlockCount();

        Gdx.app.log("CodexEntry", "Unlocked '" + title + "'");
    }

    public int getUnlockedIndex() {
        return unlockedIndex;
    }

    /**
     * Get the state of the flag determining unlocked status.
     * @return The flag determining unlocked status.
     */
    public boolean isUnlocked() {
        return unlocked;
    }

    /**
     * Get the text contents of the entry.
     * @return The text contents of the entry.
     */
    public String getText() {
        return text;
    }

    /**
     * Get the title of the entry.
     * @return The title of the entry.
     */
    public String getTitle() { return title; }
}
