package com.csse3200.game.services;

/**
 * Class representing a codex entry - storing all relevant data for an entry.
 */
public class CodexEntry {
    /**
     * Text contents of codex entry.
     */
    private final String text;
    /**
     * Flag determining if codex entry has been unlocked.
     */
    private boolean unlocked = false;

    /**
     * Constructor that sets the text contents of the codex.
     * @param text A string (with formatting) for the codex entry.
     */
    public CodexEntry(String text) {
        this.text = text;
    }

    /**
     * Set the unlocked flag to be true - use when player has unlocked entry;
     */
    public void setUnlocked() {
        unlocked = true;
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
}
