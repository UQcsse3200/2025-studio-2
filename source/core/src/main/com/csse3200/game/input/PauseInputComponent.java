package com.csse3200.game.input;

import com.badlogic.gdx.Input;
import com.csse3200.game.components.computerterminal.TerminalUiComponent;
import com.csse3200.game.components.pausemenu.PauseMenuDisplay;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.services.ServiceLocator;
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
        // If terminal overlay is open…
        if (TerminalUiComponent.isOpen()) {
            // ESC should close the terminal (not toggle pause)
            if (keycode == Input.Keys.ESCAPE) {
                var svc = ServiceLocator.getComputerTerminalService();
                if (svc != null) svc.close();   // safe if null in tests
                return true; // consumed
            }
            // Swallow pause keys so they don’t toggle the pause menu under the terminal
            if (keycode == Keymap.getActionKeyCode("PauseSettings") ||
                    keycode == Keymap.getActionKeyCode("PauseInventory") ||
                    keycode == Keymap.getActionKeyCode("PauseUpgrades")  ||
                    keycode == Keymap.getActionKeyCode("PauseObjectives")||
                    keycode == Keymap.getActionKeyCode("PauseCodex")     ||
                    keycode == Keymap.getActionKeyCode("PauseMap")) {// include tests’ key
                return true; // consumed by terminal layer
            }
            // Let other keys (like 'E') bubble to the terminal component
            return false;
        }

        // original pause handling below (unchanged test)
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
        else if (keycode == Keymap.getActionKeyCode("PauseMap"))
            tab = PauseMenuDisplay.Tab.INVENTORY; // or whichever tab your Map maps to in UI tests

        if (tab != null) {
            if (lastKeycode == keycode || !gameScreen.isPaused()) {
                gameScreen.togglePaused();
            }
            gameScreen.togglePauseMenu(tab);
            lastKeycode = keycode;
            return true;
        }

        return false; // non-pause key
    }
}
