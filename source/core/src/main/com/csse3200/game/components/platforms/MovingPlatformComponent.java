package com.csse3200.game.components.platforms;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MovingPlatformComponent extends Component {
    final Vector2 offset; // relative movement from spawn position (world units)
    final float speed;    // units per second
    private final float epsilon = 0.1f;
    private final Map<Entity, Vector2> passengerOffsets = new HashMap<>();
    private PhysicsComponent physics;
    private Vector2 start;
    private Vector2 end;
    private boolean forward = true;
    private final Set<Entity> passengers = new HashSet<>();
    private Vector2 lastPos;

    public MovingPlatformComponent(Vector2 offset, float speed) {
        this.offset = offset.cpy();
        this.speed = speed;
    }

    @Override
    public void create() {
        physics = entity.getComponent(PhysicsComponent.class);

        Vector2 pos = entity.getPosition().cpy();
        start = pos.cpy();
        end = pos.cpy().add(offset);

        // Force alignment if offset is purely vertical or horizontal
        if (offset.x == 0) {
            end.x = start.x; // lock X
        }
        if (offset.y == 0) {
            end.y = start.y; // lock Y
        }

        lastPos = pos.cpy();

        entity.getEvents().addListener("start", () -> enabled = true);
        entity.getEvents().addListener("stop", () -> enabled = false);
    }


    @Override
    public void update() {
        Body body = physics.getBody();
        Vector2 currentPos = body.getPosition();
        Vector2 target = forward ? end : start;

        if (!enabled) {
            body.setLinearVelocity(Vector2.Zero);
            return;
        }

        // Check if we're close enough to snap
        if (currentPos.dst2(target) <= epsilon * epsilon) {
            // Snap exactly to target
            body.setTransform(target, body.getAngle());
            // Stop movement
            body.setLinearVelocity(Vector2.Zero);
            // Reverse direction
            forward = !forward;
            return; // Done for this frame
        }

        // Move toward target
        Vector2 direction = target.cpy().sub(currentPos);
        if (Math.abs(direction.x) < 0.0001f) direction.x = 0;
        if (Math.abs(direction.y) < 0.0001f) direction.y = 0;
        direction.nor();
        body.setLinearVelocity(direction.scl(speed));

    }


}
