package com.csse3200.game.components;

import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

public class ButtonManagerComponent extends Component {
    private List<ButtonComponent> buttons = new ArrayList<>();
    private float puzzleTimer = 0f;
    private boolean puzzleActive = false;
    private static final float PUZZLE_TIME_LIMIT = 15f;

    public void addButton(ButtonComponent button) {
        buttons.add(button);
    }

    public void onButtonPressed() {
        if(!puzzleActive) {
            puzzleActive = true;
            puzzleTimer = PUZZLE_TIME_LIMIT;
        }
    }

    @Override
    public void update() {
        if (!puzzleActive) return;

        puzzleTimer -= ServiceLocator.getTimeSource().getDeltaTime();

        boolean allPressed = true;
        for (ButtonComponent button : buttons) {
            if (!button.isPushed()) {
                allPressed = false;
                break;
            }
        }

        if (allPressed) {
            System.out.println("Puzzle completed!");
            puzzleActive = false;
            entity.getEvents().trigger("puzzleCompleted");
        } else if (puzzleTimer <= 0f) {
            System.out.println("Puzzle failed. Resetting buttons.");
            puzzleActive = false;
            for (ButtonComponent button : buttons) {
                button.setPushed(false);
            }
        }
    }
}
