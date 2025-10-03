package com.csse3200.game.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import java.util.List;

public class HoverEffectHelper {

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
                        b.addAction(Actions.scaleTo(0.9f, 0.9f, 0.15f));
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
}
