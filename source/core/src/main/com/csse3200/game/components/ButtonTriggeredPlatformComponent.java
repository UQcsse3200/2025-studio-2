package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.physics.components.PhysicsComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ButtonTriggeredPlatformComponent extends Component{
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

    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);

        // After spawnEntityAt has placed the entity, read its origin/spawning coordinates
        origin = physics.getBody().getPosition().cpy();

        //Listen for button activation
        entity.getEvents().addListener("activatePlatform", this::onActivate);
    }

    private void onActivate() {
        active = true;
        forward = !forward;//To alter the direction each time
        platformLogger.info("Platform activated! Moving {}", forward ? "forward" : "backward");
    }
    @Override
    public void update() {
        if (!active) return;
        platformLogger.info("Platform update running");
        Body body = physics.getBody();
        Vector2 pos = body.getPosition();
        // Calculate target dynamically from origin + offset
        Vector2 target = forward ? origin.cpy().add(offset) : origin.cpy();
        Vector2 dir = target.cpy().sub(pos);

        if (dir.len()<=epsilon) {
            body.setTransform(target, body.getAngle());
            body.setLinearVelocity(Vector2.Zero);
            active = false;
        } else {
            dir.nor().scl(speed);
            body.setLinearVelocity(dir);
        }
    }
}
