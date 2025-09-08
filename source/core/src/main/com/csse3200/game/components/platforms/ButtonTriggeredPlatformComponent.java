package com.csse3200.game.components.platforms;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.components.PhysicsComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ButtonTriggeredPlatformComponent extends Component {
    private final Vector2 offset;    // units per second
    private final float speed;
    private final float epsilon = 0.05f;
    private PhysicsComponent physics;
    private Vector2 origin; //This is the position where the platform will spawn at
    private boolean active = false;
    private boolean forward = true;
    private final Logger platformLogger = LoggerFactory.getLogger(ButtonTriggeredPlatformComponent.class);
    public ButtonTriggeredPlatformComponent(Vector2 offset, float speed) {
        this.offset = offset.cpy();
        this.speed = speed;
    }
    private boolean enabled = false;
    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);
        origin = physics.getBody().getPosition().cpy(); // true spawn position
        entity.getEvents().addListener("activatePlatform", this::onToggle);
        entity.getEvents().addListener("deactivatePlatform", () -> {
            enabled = false;
            active = false;
        });
    }

    private void onToggle() {
        enabled = !enabled;
        if (enabled) {
            active = true;
            // Ensure no phantom contact
            physics.getBody().setAwake(true);
        }
    }

    @Override
    public void update() {
        if (!enabled || !active) return;

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
            forward = !forward;
            active = false;
        } else {
            dir.nor().scl(speed);
            body.setLinearVelocity(dir);
        }
    }


}
