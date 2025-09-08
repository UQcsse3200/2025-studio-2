package com.csse3200.game.input;

import com.csse3200.game.components.pausemenu.PauseMenuDisplay;
import com.csse3200.game.screens.MainGameScreen;

/**
 * A class extending InputComponent handling pause menu related key presses.
 */
public class PauseInputComponent extends InputComponent {
    private int lastKeycode = -1;
    private final MainGameScreen gameScreen;

    public PauseInputComponent(MainGameScreen gameScreen) {
        super(10);
        this.gameScreen = gameScreen;
        //this.pauseMenuDisplay = pauseMenuDisplay;
    }

    @Override
    public boolean keyDown(int keycode) {
        // Check each pause action dynamically
        PauseMenuDisplay.Tab tab = null;
        if (keycode == Keymap.getActionKeyCode("PauseSettings")) tab = PauseMenuDisplay.Tab.SETTINGS;
        else if (keycode == Keymap.getActionKeyCode("PauseInventory")) tab = PauseMenuDisplay.Tab.INVENTORY;
        else if (keycode == Keymap.getActionKeyCode("PauseUpgrades")) tab = PauseMenuDisplay.Tab.UPGRADES;
        
        if (tab != null) {
            if (lastKeycode == keycode || !gameScreen.isPaused()) {
                gameScreen.togglePaused();
            }
            gameScreen.togglePauseMenu(tab);
            lastKeycode = keycode;
            return true;
        }
        return false;
    }
}
