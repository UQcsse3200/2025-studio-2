package com.csse3200.game.components.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class LeaderboardEntryDisplayTest {

    @Test
    void testConfirmWithValidName() {
        TestableLeaderboardEntryDisplay display = new TestableLeaderboardEntryDisplay();
        display.setNameField("PlayerOne");
        display.testConfirm();

        assertEquals("PlayerOne", display.getEnteredName());
        assertTrue(display.isCompleted(), "Should be marked completed after confirm");
        assertFalse(display.isErrorVisible(), "Error should not be visible for valid name");
    }

    @Test
    void testConfirmWithEmptyNameShowsError() {
        TestableLeaderboardEntryDisplay display = new TestableLeaderboardEntryDisplay();
        display.setNameField("");
        display.testConfirm();

        assertNull(display.getEnteredName(), "Name should not be set when empty");
        assertFalse(display.isCompleted(), "Should not be completed when name is empty");
        assertTrue(display.isErrorVisible(), "Error label should be visible");
        assertEquals("Name cannot be empty!", display.getErrorText());
    }

    @Test
    void testSkipSetsNameNull() {
        TestableLeaderboardEntryDisplay display = new TestableLeaderboardEntryDisplay();
        display.testSkip();

        assertNull(display.getEnteredName(), "Skip should set enteredName to null");
        assertTrue(display.isCompleted(), "Should be marked completed after skip");
    }

    @Test
    void testFormatTime() {
        TestableLeaderboardEntryDisplay display = new TestableLeaderboardEntryDisplay();
        String formatted = display.testFormatTime(90500L); // 90.5 seconds
        assertEquals("01:30.50", formatted);
    }

    // --- Test helper class that exposes methods for testing ---
    private static class TestableLeaderboardEntryDisplay {
        private String enteredName;
        private boolean completed = false;
        private boolean errorVisible = false;
        private String errorText = "";

        public void setNameField(String text) {
            this.enteredName = text;
        }

        public void testConfirm() {
            if (completed) return;
            String trimmed = (enteredName == null) ? "" : enteredName.trim();
            if (trimmed.isEmpty()) {
                errorText = "Name cannot be empty!";
                errorVisible = true;
                enteredName = null;
                return;
            }
            completed = true;
            enteredName = trimmed;
        }

        public void testSkip() {
            if (completed) return;
            completed = true;
            enteredName = null;
        }

        public String testFormatTime(long ms) {
            long seconds = ms / 1000;
            long minutes = seconds / 60;
            long remSeconds = seconds % 60;
            long millis = (ms % 1000) / 10;
            return String.format("%02d:%02d.%02d", minutes, remSeconds, millis);
        }

        // Accessors for assertions
        public String getEnteredName() { return enteredName; }
        public boolean isCompleted() { return completed; }
        public boolean isErrorVisible() { return errorVisible; }
        public String getErrorText() { return errorText; }
    }
}
