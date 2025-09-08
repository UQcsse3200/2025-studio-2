package com.csse3200.game.components.platforms;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.components.PhysicsComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ButtonTriggeredPlatformComponent extends Component {
    private final Vector2 offset;
    private final float speed;
    private final float epsilon = 0.05f;

    private PhysicsComponent physics;

    // Persistent endpoints, fixed once
    private Vector2 start;   // fixed origin captured on create
    private Vector2 end;     // start + offset (axis-locked)

    private boolean active = false;     // moving this press
    private boolean goingOut = false;   // "next direction" flag; toggled on press

    private static final Logger logger = LoggerFactory.getLogger(ButtonTriggeredPlatformComponent.class);

    public ButtonTriggeredPlatformComponent(Vector2 offset, float speed) {
        this.offset = offset.cpy();
        this.speed = speed;
    }

    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);

        // Capture persistent endpoints once
        Body body = physics.getBody();
        start = body.getPosition().cpy();
        end = start.cpy().add(offset);

        // Axis-lock the fixed endpoints so we always move straight
        if (offset.x == 0f) end.x = start.x;
        if (offset.y == 0f) end.y = start.y;

        // Listeners
        entity.getEvents().addListener("activatePlatform", this::onActivate);
        entity.getEvents().addListener("deactivatePlatform", () -> active = false);
    }

    private void onActivate() {
        if (active) return; // ignore if already moving this cycle

        // Toggle intended direction per press:
        // false -> true: start -> end (go up/out)
        // true  -> false: end -> start (come back)
        goingOut = !goingOut;

        active = true;
    }

    @Override
    public void update() {
        if (!active) return;

        Body body = physics.getBody();
        Vector2 pos = body.getPosition();

        // Choose the fixed target based on the current "goingOut" direction
        Vector2 target = goingOut ? end : start;

        // Axis lock during travel to prevent diagonal drift
        if (offset.x == 0f) pos.x = start.x;
        if (offset.y == 0f) pos.y = start.y;

        Vector2 dir = target.cpy().sub(pos);

        // Arrived (or close enough): snap, stop, and wait for the next press
        if (dir.len() <= epsilon) {
            body.setTransform(target, body.getAngle());
            body.setLinearVelocity(Vector2.Zero);
            active = false;
            return;
        }

        // Move toward target with the same physics you liked
        dir.nor().scl(speed);
        body.setLinearVelocity(dir);
    }
}
