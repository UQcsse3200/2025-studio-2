package com.csse3200.game.input;

import com.badlogic.gdx.Input;
import com.csse3200.game.components.pausemenu.PauseMenuDisplay;


public class PauseMenuNavigationComponent extends InputComponent {
    private final PauseMenuDisplay pauseMenuDisplay;

    public PauseMenuNavigationComponent(PauseMenuDisplay pauseMenuDisplay) {
        this.pauseMenuDisplay = pauseMenuDisplay;
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.A:
                PauseMenuDisplay.Tab prev = pauseMenuDisplay.getPrevTab();
                pauseMenuDisplay.setTab(prev);
                return true;

            case Input.Keys.D:
                PauseMenuDisplay.Tab next = pauseMenuDisplay.getNextTab();
                pauseMenuDisplay.setTab(next);
                return true;
        }
        return false;
    }
}
