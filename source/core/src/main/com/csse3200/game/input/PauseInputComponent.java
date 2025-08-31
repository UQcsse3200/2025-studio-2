package com.csse3200.game.input;

import com.badlogic.gdx.Input.Keys;
import com.csse3200.game.screens.MainGameScreen;

/**
 * A class extending InputComponent handling pause menu related key presses.
 */
public class PauseInputComponent extends InputComponent {
    private MainGameScreen gameScreen;

    public PauseInputComponent(MainGameScreen gameScreen) {
        super(10);
        this.gameScreen = gameScreen;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keys.ESCAPE) {
            gameScreen.togglePaused();
            return true;
        }
        return false;
    }
}
