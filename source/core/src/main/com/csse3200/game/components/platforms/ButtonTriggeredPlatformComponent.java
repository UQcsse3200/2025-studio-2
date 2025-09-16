package com.csse3200.game.components.platforms;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.components.PhysicsComponent;


/**
 * Button Triggered Platform Component for platforms with buttons to trigger movement
 */
public class ButtonTriggeredPlatformComponent extends Component {
    private enum State {
        IDLE_AT_START,
        MOVING_TO_END,
        IDLE_AT_END,
        MOVING_TO_START
    }

    private final Vector2 offset;
    private final float speed;
    private final float epsilon = 0.05f; // makes the platform snap to the target

    private PhysicsComponent physics;

    // Recomputed per activation to avoid drift
    private Vector2 start;
    private Vector2 end;

    private State state = State.IDLE_AT_START;

    /**
     * Sets the offset (direction) and speed of the platform to be moved
     * @param offset
     * @param speed
     */
    public ButtonTriggeredPlatformComponent(Vector2 offset, float speed) {
        this.offset = offset.cpy();
        this.speed = speed;
    }

    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);

        // Only one input we honor; no deactivate listener that can interrupt mid-trip
        entity.getEvents().addListener("activatePlatform", this::onActivate);
        entity.getEvents().addListener("deactivatePlatform", this::onDeactivate);
        // If you truly need an emergency stop, replace with a guarded handler that sets a flag
        // instead of immediately mutating state mid-physics step.
    }

    private void onDeactivate() {
        // Ignore while moving
        if (state == State.MOVING_TO_END || state == State.MOVING_TO_START) {
            return;
        }

        Body body = physics.getBody();
        Vector2 currentPos = body.getPosition().cpy();

        if (state == State.IDLE_AT_END) {
            // At end move to start
            end = currentPos;
            start = end.cpy().sub(offset);
            if (offset.x == 0f) start.x = end.x;
            if (offset.y == 0f) start.y = end.y;

            state = State.MOVING_TO_START;
        } else if (state == State.IDLE_AT_START) {
            // At start move to end
            start = currentPos;
            end = start.cpy().add(offset);
            if (offset.x == 0f) end.x = start.x;
            if (offset.y == 0f) end.y = start.y;

            state = State.MOVING_TO_END;
        }
    }

    private void onActivate() {
        // Ignore input while moving
        if (state == State.MOVING_TO_END || state == State.MOVING_TO_START) {
            return;
        }

        Body body = physics.getBody();
        Vector2 currentPos = body.getPosition().cpy();

        // Decide direction from current idle state, then compute endpoints accordingly
        if (state == State.IDLE_AT_START) {
            // We are at (or considered at) the "start". Move to end from here.
            start = currentPos;
            end = start.cpy().add(offset);
            // Axis lock
            if (offset.x == 0f) end.x = start.x;
            if (offset.y == 0f) end.y = start.y;

            state = State.MOVING_TO_END;
        } else { // IDLE_AT_END
            // We are at (or considered at) the "end". Move back to start from here.
            end = currentPos;
            start = end.cpy().sub(offset);
            // Axis lock
            if (offset.x == 0f) start.x = end.x;
            if (offset.y == 0f) start.y = end.y;

            state = State.MOVING_TO_START;
        }
    }

    @Override
    public void update() {
        if (state != State.MOVING_TO_END && state != State.MOVING_TO_START) {
            return; // only move while in moving states
        }

        Body body = physics.getBody();
        Vector2 pos = body.getPosition();
        Vector2 target = (state == State.MOVING_TO_END) ? end : start;

        Vector2 dir = target.cpy().sub(pos);

        // Arrived (or close enough): snap and switch to the correct idle state
        if (dir.len() <= epsilon) {
            body.setTransform(target, body.getAngle());
            body.setLinearVelocity(Vector2.Zero);
            state = (state == State.MOVING_TO_END) ? State.IDLE_AT_END : State.IDLE_AT_START;
            return;
        }

        // Move toward target with fixed speed
        dir.nor().scl(speed);
        body.setLinearVelocity(dir);
    }
}
