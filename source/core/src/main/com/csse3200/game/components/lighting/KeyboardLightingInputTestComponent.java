package com.csse3200.game.components.lighting;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.utils.math.Vector2Utils;

/**
 * This class was lazily copied from the player input handler (as you can probably tell) just to test the
 * dynamic movement and rotation of the lights.
 */
public class KeyboardLightingInputTestComponent  extends InputComponent {
    private final Vector2 walkDirection = Vector2.Zero.cpy();

    public KeyboardLightingInputTestComponent() { super(5); }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.UP:
                walkDirection.add(Vector2Utils.UP);
                triggerWalkEvent();
                return true;
            case Input.Keys.LEFT:
                walkDirection.add(Vector2Utils.LEFT);
                triggerWalkEvent();
                return true;
            case Input.Keys.DOWN:
                walkDirection.add(Vector2Utils.DOWN);
                triggerWalkEvent();
                return true;
            case Input.Keys.RIGHT:
                walkDirection.add(Vector2Utils.RIGHT);
                triggerWalkEvent();
                return true;
            case Input.Keys.E:
                entity.getEvents().trigger("rotate", 30f);
                return true;
            case Input.Keys.Q:
                entity.getEvents().trigger("rotate", -30f);
                return true;
            default:
                return false;
        }
    }

    /**
     * Triggers events on specific keycodes.
     *
     * @return whether the input was processed
     * @see InputProcessor#keyUp(int)
     */
    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.UP:
                walkDirection.sub(Vector2Utils.UP);
                triggerWalkEvent();
                return true;
            case Input.Keys.LEFT:
                walkDirection.sub(Vector2Utils.LEFT);
                triggerWalkEvent();
                return true;
            case Input.Keys.DOWN:
                walkDirection.sub(Vector2Utils.DOWN);
                triggerWalkEvent();
                return true;
            case Input.Keys.RIGHT:
                walkDirection.sub(Vector2Utils.RIGHT);
                triggerWalkEvent();
                return true;
            case Input.Keys.E:
                entity.getEvents().trigger("rotate", 0f);
                return true;
            case Input.Keys.Q:
                entity.getEvents().trigger("rotate", 0f);
                return true;
            default:
                return false;
        }
    }

    private void triggerWalkEvent() {
        if (walkDirection.epsilonEquals(Vector2.Zero)) {
            entity.getEvents().trigger("walkStop");
        } else {
            entity.getEvents().trigger("walk", walkDirection);
        }
    }
}
