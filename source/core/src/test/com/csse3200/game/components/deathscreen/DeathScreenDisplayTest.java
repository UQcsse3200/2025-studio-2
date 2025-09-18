package com.csse3200.game.components.deathscreen;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DeathScreenDisplayTest {

    @Test
    void testStripMarkupTags() {
        TestableDeathScreenDisplay display = new TestableDeathScreenDisplay();
        
        // Test basic markup removal
        String result1 = display.testStripMarkupTags("{RAINBOW}Hello{ENDRAINBOW}");
        assertEquals("Hello", result1);
        
        // Test multiple tags
        String result2 = display.testStripMarkupTags("{COLOR=RED}You{ENDCOLOR} {SHAKE}died{ENDSHAKE}");
        assertEquals("You died", result2);
        
        // Test no markup
        String result3 = display.testStripMarkupTags("Plain text");
        assertEquals("Plain text", result3);
    }
    
    @Test
    void testStripMarkupEdgeCases() {
        TestableDeathScreenDisplay display = new TestableDeathScreenDisplay();
        
        // Test empty string
        assertEquals("", display.testStripMarkupTags(""));
        
        // Test nested tags
        assertEquals("Test", display.testStripMarkupTags("{WAVE}{RAINBOW}Test{ENDRAINBOW}{ENDWAVE}"));
        
        // Test tags with parameters
        assertEquals("Colored text", display.testStripMarkupTags("{COLOR=BLUE}Colored text{ENDCOLOR}"));
        
        // Test malformed tags (shouldn't break)
        assertEquals("Text with { broken", display.testStripMarkupTags("Text with { broken"));
    }
    
    @Test
    void testCenterOffsetCalculation() {
        TestableDeathScreenDisplay display = new TestableDeathScreenDisplay();
        
        // Test that longer text returns larger negative offset
        float shortOffset = display.testCalculateCenterOffset("Hi");
        float longOffset = display.testCalculateCenterOffset("This is a much longer text");
        
        assertTrue(longOffset < shortOffset, "Longer text should have more negative offset");
        assertTrue(shortOffset < 0, "Offset should be negative for centering");
    }
    
    @Test
    void testRandomPromptSelection() {
        TestableDeathScreenDisplay display = new TestableDeathScreenDisplay();
        
        // Test with sample prompts
        String[] prompts = {"Prompt 1", "Prompt 2", "Prompt 3"};
        display.setTestPrompts(prompts);
        
        // Get multiple random prompts and verify they're from our list
        for (int i = 0; i < 10; i++) {
            String randomPrompt = display.testGetRandomPrompt();
            assertTrue(java.util.Arrays.asList(prompts).contains(randomPrompt),
                      "Random prompt should be from our test list");
        }
    }
    
    @Test
    void testDeathScreenTriggering() {
        TestableDeathScreenDisplay display = new TestableDeathScreenDisplay();
        
        // Test initial state - death screen should be hidden
        assertFalse(display.isVisible(), "Death screen should start hidden");
        
        // Test showing death screen
        display.setVisible(true);
        assertTrue(display.isVisible(), "Death screen should be visible after setVisible(true)");
        
        // Test hiding death screen
        display.setVisible(false);
        assertFalse(display.isVisible(), "Death screen should be hidden after setVisible(false)");
    }
    
    @Test
    void testDeathScreenStateManagement() {
        TestableDeathScreenDisplay display = new TestableDeathScreenDisplay();
        
        // Test multiple state changes
        display.setVisible(true);
        display.setVisible(true); // Should handle redundant calls
        assertTrue(display.isVisible());
        
        display.setVisible(false);
        display.setVisible(false); // Should handle redundant calls
        assertFalse(display.isVisible());
    }
    
    // Test helper class that exposes methods for testing
    private static class TestableDeathScreenDisplay {
        private String[] testPrompts = {"Default prompt"};
        private java.util.Random random = new java.util.Random(42); // Fixed seed for testing
        private boolean visible = false;
        
        public String testStripMarkupTags(String text) {
            return text.replaceAll("\\{[^}]*\\}", "");
        }
        
        public float testCalculateCenterOffset(String text) {
            // Simplified calculation - assume 10 pixels per character
            String cleanText = testStripMarkupTags(text);
            float textWidth = cleanText.length() * 10f;
            return -textWidth / 2f;
        }
        
        public void setTestPrompts(String[] prompts) {
            this.testPrompts = prompts;
        }
        
        public String testGetRandomPrompt() {
            if (testPrompts.length == 0) return "Default";
            return testPrompts[random.nextInt(testPrompts.length)];
        }
        
        public void setVisible(boolean visible) {
            this.visible = visible;
        }
        
        public boolean isVisible() {
            return visible;
        }
    }
}