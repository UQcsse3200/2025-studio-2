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
    private Vector2 start;
    private Vector2 end;
    private boolean active = false;
    private boolean goingOut = true; // phase control

    public ButtonTriggeredPlatformComponent(Vector2 offset, float speed) {
        this.offset = offset.cpy();
        this.speed = speed;
    }

    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);

        entity.getEvents().addListener("activatePlatform", this::onActivate);
        entity.getEvents().addListener("deactivatePlatform", () -> active = false);
    }

    private void onActivate() {
        if (active) return; // ignore if already moving

        Body body = physics.getBody();
        start = body.getPosition().cpy();
        end = start.cpy().add(offset);

        // Lock axis
        if (offset.x == 0) end.x = start.x;
        if (offset.y == 0) end.y = start.y;

        goingOut = !goingOut;
        active = true;
    }

    @Override
    public void update() {
        if (!active) return;

        Body body = physics.getBody();
        Vector2 pos = body.getPosition();
        Vector2 target = goingOut ? end : start;

        Vector2 dir = target.cpy().sub(pos);

        if (dir.len() <= epsilon) {
            body.setTransform(target, body.getAngle());
            body.setLinearVelocity(Vector2.Zero);
            active = false;
            return;
        }

        dir.nor().scl(speed);
        body.setLinearVelocity(dir);
    }
}