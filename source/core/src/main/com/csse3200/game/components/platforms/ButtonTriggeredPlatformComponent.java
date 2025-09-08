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
    private Vector2 origin; // starting position when spawned
    private boolean active = false;
    private boolean forward = true; // true = going to target, false = returning
    private final Logger platformLogger = LoggerFactory.getLogger(ButtonTriggeredPlatformComponent.class);

    public ButtonTriggeredPlatformComponent(Vector2 offset, float speed) {
        this.offset = offset.cpy();
        this.speed = speed;
    }
    private Vector2 start;
    private Vector2 end;
    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);
        origin = physics.getBody().getPosition().cpy();
        Body body = physics.getBody();
        start = body.getPosition().cpy();
        end = start.cpy().add(offset);

        // Lock axis
        if (offset.x == 0) end.x = start.x;
        if (offset.y == 0) end.y = start.y;
        active = true;
        // Listen for button activation
        entity.getEvents().addListener("activatePlatform", this::onToggle);
        entity.getEvents().addListener("deactivatePlatform", () -> active = false);
    }

    private void onToggle() {
        if (active) return; // ignore presses while moving
        active = true;
        // Flip direction each time we start moving
        forward = !forward;
    }

    @Override
    public void update() {
        if (!active) return;

        Body body = physics.getBody();
        Vector2 pos = body.getPosition();
        Vector2 target = forward ? origin.cpy().add(offset) : origin.cpy();

        // Lock axis to prevent diagonal drift
        if (offset.x == 0) pos.x = origin.x;
        if (offset.y == 0) pos.y = origin.y;

        Vector2 dir = target.cpy().sub(pos);

        if (dir.len() <= epsilon) {
            body.setTransform(target, body.getAngle());
            body.setLinearVelocity(Vector2.Zero);
            active = false; // stop until next press
        } else {
            dir.nor().scl(speed);
            body.setLinearVelocity(dir);
        }
    }
}
