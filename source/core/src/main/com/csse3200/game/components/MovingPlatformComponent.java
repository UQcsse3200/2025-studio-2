package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.components.PhysicsComponent;

import java.util.HashSet;
import java.util.Set;

public class MovingPlatformComponent extends Component {
    private final Vector2 offset; // relative movement from spawn position (world units)
    private final float speed;    // units per second
    private final float epsilon = 0.05f;

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
        Vector2 pos = physics.getBody().getPosition().cpy();
        start = pos;
        end = pos.cpy().add(offset);

        lastPos = pos.cpy();

        // Listen for collision events from PhysicsContactListener
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        entity.getEvents().addListener("collisionEnd", this::onCollisionEnd);
    }

    @Override
    public void update() {
        Body body = physics.getBody();
        Vector2 pos = body.getPosition();
        Vector2 target = forward ? end : start;
        Vector2 dir = target.cpy().sub(pos);

        if (dir.len() <= epsilon) {
            // Snap to target and reverse
            body.setTransform(target, body.getAngle());
            body.setLinearVelocity(Vector2.Zero);
            forward = !forward;
        } else {
            dir.nor().scl(speed);
            body.setLinearVelocity(dir);
        }

        // Move passengers by the platform's delta
        Vector2 delta = pos.cpy().sub(lastPos);
        for (Entity passenger : passengers) {
            PhysicsComponent pc = passenger.getComponent(PhysicsComponent.class);
            if (pc != null) {
                pc.getBody().setTransform(pc.getBody().getPosition().add(delta), 0);
            }
        }

        lastPos.set(pos);
    }

    private void onCollisionStart(Fixture thisFixture, Fixture otherFixture) {
        Entity other = ((BodyUserData) otherFixture.getBody().getUserData()).entity;
        if (other != null && other.getComponent(PlayerActions.class) != null) {
            passengers.add(other);
        }
    }

    private void onCollisionEnd(Fixture thisFixture, Fixture otherFixture) {
        Entity other = ((BodyUserData) otherFixture.getBody().getUserData()).entity;
        passengers.remove(other);
    }
}
