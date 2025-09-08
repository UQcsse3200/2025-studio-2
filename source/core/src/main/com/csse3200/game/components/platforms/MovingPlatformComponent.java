package com.csse3200.game.components.platforms;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class MovingPlatformComponent extends Component {
    final Vector2 offset; // relative movement from spawn position (world units)
    final float speed;    // units per second
    private final float epsilon = 0.05f;
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

        // After spawnEntityAt has placed the entity, read its actual start position
        Vector2 pos = entity.getPosition().cpy();
        start = pos;
        end = pos.cpy().add(offset);

        lastPos = pos.cpy();

    }

    @Override
    public void update() {
        Body body = physics.getBody();
        Vector2 pos = physics.getBody().getPosition().cpy();
        Vector2 target = forward ? end : start;
        Vector2 dir = target.cpy().sub(pos);
        if (offset.x == 0) dir.x = 0; // vertical-only
        if (offset.y == 0) dir.y = 0; // horizontal-only
        
        if (dir.len() <= epsilon) {
            // Snap to target and reverse
            body.setTransform(target, body.getAngle());
            body.setLinearVelocity(Vector2.Zero);
            forward = !forward;
        } else {
            dir.nor().scl(speed);
            body.setLinearVelocity(dir);
        }

        lastPos.set(pos);
    }

}
