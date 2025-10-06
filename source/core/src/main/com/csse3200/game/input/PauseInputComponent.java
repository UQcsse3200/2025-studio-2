package com.csse3200.game.input;

import com.csse3200.game.components.pausemenu.PauseMenuDisplay;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.ui.cutscene.CutsceneArea;

/**
 * A class extending InputComponent handling pause menu related key presses.
 */
public class PauseInputComponent extends InputComponent {
    private final MainGameScreen gameScreen;
    private int lastKeycode = -1;

    public PauseInputComponent(MainGameScreen gameScreen) {
        super(10);
        this.gameScreen = gameScreen;
        //this.pauseMenuDisplay = pauseMenuDisplay;
    }

    // Set last keycode for inventory Tabs
    public void setLastKeycodeForTab(PauseMenuDisplay.Tab tab) {
        this.lastKeycode = switch (tab) {
            case SETTINGS -> Keymap.getActionKeyCode("PauseSettings");
            case INVENTORY -> Keymap.getActionKeyCode("PauseInventory");
            case UPGRADES -> Keymap.getActionKeyCode("PauseUpgrades");
            case OBJECTIVES -> Keymap.getActionKeyCode("PauseObjectives");
            case CODEX -> Keymap.getActionKeyCode("PauseCodex");
        };
    }

    @Override
    public boolean keyDown(int keycode) {
        // Check each pause action dynamically
        PauseMenuDisplay.Tab tab = null;

        if (keycode == Keymap.getActionKeyCode("PauseSettings"))
            tab = PauseMenuDisplay.Tab.SETTINGS;
        else if (keycode == Keymap.getActionKeyCode("PauseInventory"))
            tab = PauseMenuDisplay.Tab.INVENTORY;
        else if (keycode == Keymap.getActionKeyCode("PauseUpgrades"))
            tab = PauseMenuDisplay.Tab.UPGRADES;
        else if (keycode == Keymap.getActionKeyCode("PauseObjectives"))
            tab = PauseMenuDisplay.Tab.OBJECTIVES;
        else if (keycode == Keymap.getActionKeyCode("PauseCodex"))
            tab = PauseMenuDisplay.Tab.CODEX;


        if (tab != PauseMenuDisplay.Tab.SETTINGS && gameScreen.getGameArea() instanceof CutsceneArea) {
            return false;
        }

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
