package com.csse3200.game.input;

import com.csse3200.game.components.pausemenu.PauseMenuDisplay;
import com.csse3200.game.screens.MainGameScreen;

import java.util.HashMap;
import java.util.Map;

/**
 * A class extending InputComponent handling pause menu related key presses.
 */
public class PauseInputComponent extends InputComponent {
    /**
     * A hash map containing all keys that can open the pause menu related to the tab it opens with
     */
    private final Map<Integer, PauseMenuDisplay.Tab> pauseKeys = new HashMap<>();

    private int lastKeycode = -1;

    //private PauseMenuDisplay pauseMenuDisplay;
    private MainGameScreen gameScreen;

    public PauseInputComponent(MainGameScreen gameScreen) {
        super(10);
        this.gameScreen = gameScreen;
        //this.pauseMenuDisplay = pauseMenuDisplay;

        // Get all key codes that open pause screen and relate with tab
        pauseKeys.put(Keymap.getActionKeyCode("PauseSettings"), PauseMenuDisplay.Tab.SETTINGS);
        pauseKeys.put(Keymap.getActionKeyCode("PauseInventory"), PauseMenuDisplay.Tab.INVENTORY);
        pauseKeys.put(Keymap.getActionKeyCode("PauseMap"), PauseMenuDisplay.Tab.MAP);
        pauseKeys.put(Keymap.getActionKeyCode("PauseUpgrades"), PauseMenuDisplay.Tab.UPGRADES);
    }

    @Override
    public boolean keyDown(int keycode) {
        // Keycode given opens the pause menu
        if (pauseKeys.containsKey(keycode)) {
            // Only pause if the pause menu is not open, or if the key pressed was same as last
            if (lastKeycode == keycode || !gameScreen.isPaused())  {
                gameScreen.togglePaused();
            }

            // Open tab depending on key code
            gameScreen.togglePauseMenu(pauseKeys.get(keycode));

            // Update last keycode
            lastKeycode = keycode;
            return true;
        }

        return false;
    }
}
