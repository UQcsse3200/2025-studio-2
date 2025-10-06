package com.csse3200.game.components.obstacles;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;

public class MovingTrapComponent extends Component {
    /**
     * Default fallback surface speed if the host body's velocity is not available.
     */
    private static final float SURFACE_SPEED = 0.75f;

    /** The entity this component should follow. */
    private final Entity host;

    /** Initial offset from the host entity, captured on the first update. */
    private final Vector2 offset = new Vector2();

    /** Stores the last known position of the host. */
    private final Vector2 lastHostPos = new Vector2();

    /** Whether the initial offset has been captured. */
    private boolean captured = false;

    /**
     * Creates a new {@code PositionSyncComponent} that will follow the given host entity.
     *
     * @param host the entity to synchronize position with
     */

    public MovingTrapComponent(Entity host) {
        this.host = host;
    }
    @Override
    public void create() {
        // Ensure the entity is treated as a kinematic body
        entity.getComponent(PhysicsComponent.class).setBodyType(BodyDef.BodyType.KinematicBody);

        // Apply friction and restitution to prevent sliding/bouncing
        ColliderComponent col = entity.getComponent(ColliderComponent.class);
        if (col != null) {
            col.setFriction(0.9f).setRestitution(0f);
        }
    }

    @Override
    public void update() {
        PhysicsComponent hostPhys = host.getComponent(PhysicsComponent.class);
        PhysicsComponent selfPhys = entity.getComponent(PhysicsComponent.class);
        Vector2 hostPos = host.getPosition();

        // Capture the initial offset on the first update
        if (!captured) {
            offset.set(entity.getPosition()).sub(hostPos);
            lastHostPos.set(hostPos);
            captured = true;
            return;
        }

        // Calculate the new target position relative to the host
        Vector2 target = new Vector2(hostPos).add(offset);

        // Move and synchronize velocity with the host
        selfPhys.getBody().setTransform(target, 0f);
        selfPhys.getBody().setLinearVelocity(hostPhys.getBody().getLinearVelocity());

        lastHostPos.set(hostPos);
    }
}
