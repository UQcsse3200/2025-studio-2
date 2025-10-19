package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import java.util.List;

public class HoverEffectHelper {
    private HoverEffectHelper() {
        throw new IllegalStateException("Instantiating static util class");
    }

    /**
     * Adds hover enlarge/shrink behaviour to a group of buttons.
     */
    public static void applyHoverEffects(List<TextButton> menuButtons) {
        for (TextButton button : menuButtons) {
            button.addListener(new InputListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    // Shrink all first
                    for (TextButton b : menuButtons) {
                        b.addAction(Actions.scaleTo(0.95f, 0.95f, 0.15f));
                    }
                    // Enlarge hovered
                    button.addAction(Actions.scaleTo(1.2f, 1.2f, 0.15f));
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    // Reset all to normal
                    for (TextButton b : menuButtons) {
                        b.addAction(Actions.scaleTo(1f, 1f, 0.15f));
                    }
                }
            });
        }

    }
    /**
     * Apply an idle pulsing color effect (good for death screen buttons).
     */
    public static void applyIdlePulse(TextButton button, Color c1, Color c2, float duration) {
        button.addAction(Actions.forever(
                Actions.sequence(
                        Actions.color(c1, duration),
                        Actions.color(c2, duration)
                )
        ));
    }
    /**
     * Apply a fade-in effect (useful for delayed reveal).
     */
    public static void applyFadeIn(TextButton button, float delay, float duration) {
        button.getColor().a = 0f; // start transparent
        button.addAction(Actions.sequence(
                Actions.delay(delay),
                Actions.fadeIn(duration)
        ));
    }
    /**
     * Apply a jitter/shake effect (adds unease).
     */
    public static void applyJitter(TextButton button, float amount, float speed) {
        button.addAction(Actions.forever(
                Actions.sequence(
                        Actions.moveBy(amount, 0, speed),
                        Actions.moveBy(-2 * amount, 0, speed),
                        Actions.moveBy(amount, 0, speed)
                )
        ));
    }
    /**
     * Apply a subtle pulsing scale effect (gentle grow/shrink loop).
     */
    public static void applySubtlePulse(TextButton button, float scaleAmount, float duration) {
        button.addAction(Actions.forever(
                Actions.sequence(
                        Actions.scaleTo(scaleAmount, scaleAmount, duration),
                        Actions.scaleTo(1f, 1f, duration)
                )
        ));
    }
    /**
     * Apply a slinky wave effect across multiple buttons.
     * Each button enlarges in sequence, then shrinks back as the next enlarges.
     */
    public static void applySlinkyEffect(List<TextButton> buttons, float scaleUp, float duration, float delayBetween) {
        for (int i = 0; i < buttons.size(); i++) {
            TextButton button = buttons.get(i);

            // Delay each button's animation based on its index
            float delay = i * delayBetween;

            button.addAction(Actions.forever(
                    Actions.sequence(
                            Actions.delay(delay),
                            Actions.scaleTo(scaleUp, scaleUp, duration),
                            Actions.scaleTo(1f, 1f, duration),
                            Actions.delay((buttons.size() - 1 - i) * delayBetween) // wait for others to finish
                    )
            ));
        }
    }
    public static void applyHoverInterruptiblePulse(TextButton button, float scaleAmount, float duration) {
        // Start pulsing immediately
        button.addAction(makePulse(scaleAmount, duration));

        button.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                // Stop pulsing when hovered
                button.clearActions();
                // Enlarge slightly for hover feedback
                button.addAction(Actions.scaleTo(1.2f, 1.2f, 0.15f));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                // Reset to normal size
                button.addAction(Actions.scaleTo(1f, 1f, 0.15f));
                // Re‑apply a *new* pulse action
                button.addAction(makePulse(scaleAmount, duration));
            }
        });
    }

    // Helper to create a fresh pulse action each time
    private static Action makePulse(float scaleAmount, float duration) {
        return Actions.forever(
                Actions.sequence(
                        Actions.scaleTo(scaleAmount, scaleAmount, duration),
                        Actions.scaleTo(1f, 1f, duration)
                )
        );
    }


}
