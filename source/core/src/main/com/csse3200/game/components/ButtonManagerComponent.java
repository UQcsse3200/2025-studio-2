package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.TimerSignFactory;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages a group of buttons that form one full button puzzle in which all buttons in manager
 *  must be pressed in a set amount of time
 * Tracks button states, handles puzzle timing, triggers even on success and resets all buttons on failure
 */
public class ButtonManagerComponent extends Component {
    private List<ButtonComponent> buttons = new ArrayList<>();
    private float puzzleTimer = 0f;
    private boolean puzzleActive = false;
    private static final float PUZZLE_TIME_LIMIT = 15f;
    private boolean puzzleCompleted = false;

    private Entity sign = null;
    private AnimationRenderComponent signAnimator;
    private final String[] atlas = {"images/timer.atlas"};

    /**
     * Adds a button to the list of buttons managed by this manager
     *
     * @param button the ButtonComponent to add
     */
    public void addButton(ButtonComponent button) {
        buttons.add(button);
    }

    /**
     * Called when any button is pressed, starts the timer if the puzzle isn't already active
     */
    public void onButtonPressed() {
        if (!puzzleActive) {
            puzzleActive = true;
            puzzleTimer = PUZZLE_TIME_LIMIT;

            // find pushed button position
            Vector2 buttonPos = null;
            for (ButtonComponent button : buttons) {
                if (button.isPushed()) {
                    buttonPos = button.getEntity().getPosition().cpy();
                    break;
                }
            }
            if (buttonPos == null) return;

            // create sign
            if (sign == null) {
                sign = TimerSignFactory.createTimer(PUZZLE_TIME_LIMIT, buttonPos);
                signAnimator = sign.getComponent(AnimationRenderComponent.class);
                ServiceLocator.getEntityService().register(sign);
            } else  {
                // if it already exists change the pos and start the animation
                sign.setPosition(buttonPos.x, buttonPos.y + 0.5f);
                signAnimator.startAnimation("timer");
            }
        }
    }

    /**
     * Updates the puzzle timer and checks button state
     *  If all buttons pressed before timer expires, puzzle completed and event triggered
     *  If time runs out before buttons all marked as pressed, all buttons are reset by forceUnpress()
     */
    @Override
    public void update() {
        if (!puzzleActive) {
            return;
        }

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
                if (sign != null) {
                    signAnimator.setPaused(true);
                }
                entity.getEvents().trigger("puzzleCompleted");
            }
        } else if (puzzleTimer <= 0f) {
            puzzleActive = false;
            for (ButtonComponent button : buttons) {
                button.forceUnpress();
            }
            if (sign != null) {
                signAnimator.stopAnimation();
            }
        }
    }

    /**
     * Checks if puzzle successfully completed
     *
     * @return true of puzzle completed, false otherwise
     */
    public boolean isPuzzleCompleted() {
        return puzzleCompleted;
    }

    /**
     * Resets puzzle state by clearing progress, deactivating puzzle and unpressing all buttons being
     *  controlled by the manager
     */
    public void resetPuzzle() {
        puzzleActive = false;
        puzzleCompleted = false;
        puzzleTimer = 0f;
        for (ButtonComponent button : buttons) {
            button.forceUnpress();
        }
    }

    /**
     * Gets time remaining before puzzle fails i.e. time left to complete
     *
     * @return time left if puzzle active, 0 otherwise
     */
    public float getTimeLeft() {
        if(!puzzleActive) {
            return 0f;
        }
        return Math.max(0, puzzleTimer);
    }

    /**
     * Returns list of buttons being managed by this puzzle button manager
     *
     * @return list of ButtonComponents being managed
     */
    public List<ButtonComponent> getButtons() {
        return buttons;
    }

    @Override
    public void dispose() {
        if (sign != null) sign.dispose();
    }
}
