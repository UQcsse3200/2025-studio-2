package com.csse3200.game.components;

import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

public class ButtonManagerComponent extends Component {
    private List<ButtonComponent> buttons = new ArrayList<>();
    private float puzzleTimer = 0f;
    private boolean puzzleActive = false;
    private static final float PUZZLE_TIME_LIMIT = 5f;
    private boolean puzzleCompleted = false;

    public void addButton(ButtonComponent button) {
        buttons.add(button);
    }

    public void onButtonPressed() {
        if (!puzzleActive) {
            puzzleActive = true;
            puzzleTimer = PUZZLE_TIME_LIMIT;
        }
    }

    @Override
    public void update() {
        if (!puzzleActive) return;

        puzzleTimer -= ServiceLocator.getTimeSource().getDeltaTime();

        boolean allPressedNow = true;
        for (ButtonComponent button : buttons) {
            if (!button.isPushed()) {
                allPressedNow = false;
                break;
            }
        }

        if (allPressedNow) {
            if (!puzzleCompleted) {
                puzzleCompleted = true;
                puzzleActive = false;
                entity.getEvents().trigger("puzzleCompleted");
            }
        } else if (puzzleTimer <= 0f) {
            puzzleActive = false;
            for (ButtonComponent button : buttons) {
                button.forceUnpress();
            }
        }
    }

    public boolean isPuzzleCompleted() {
        return !puzzleCompleted;
    }

    public void resetPuzzle() {
        puzzleActive = false;
        puzzleCompleted = false;
        puzzleTimer = 0f;
        for (ButtonComponent button : buttons) {
            button.forceUnpress();
        }
    }

    public float getTimeLeft() {
        if(!puzzleActive) return 0f;
        return Math.max(0, puzzleTimer);
    }
}
