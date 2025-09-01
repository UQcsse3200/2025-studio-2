package com.csse3200.game.input;

import com.badlogic.gdx.Input.Keys;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.components.pausemenu.PauseMenuDisplay;

/**
 * A class extending InputComponent handling pause menu related key presses.
 */
public class PauseInputComponent extends InputComponent {
    private MainGameScreen gameScreen;
    //private PauseMenuDisplay pauseMenuDisplay;

    public PauseInputComponent(MainGameScreen gameScreen) {
        super(10);
        this.gameScreen = gameScreen;
        //this.pauseMenuDisplay = pauseMenuDisplay;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keymap.getActionKeyCode("PauseGame")) {
            gameScreen.togglePaused();
            gameScreen.togglePauseMenu(PauseMenuDisplay.Tab.SETTINGS);
            //pauseMenuDisplay.setVisible(true);
            //pauseMenuDisplay.setTab(PauseMenuDisplay.Tab.SETTINGS);
            return true;
        }
        return false;
    }
}
