package com.csse3200.game.components.lighting;

import box2dLight.ConeLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.ComponentPriority;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Cone light component used to store the ConeLight object and all of its parameters.
 * This can be added to an entity to make it product light.
 */
public class ConeLightComponent extends Component {
    private final RayHandler rayHandler;
    private ConeLight coneLight;

    // cone light properties
    private final int rays;
    private final Color color;
    private float distance;
    private float directionDeg;
    private float coneDegree;
    private boolean isActive = true;

    // Movement/rotation
    private Vector2 velocity = new Vector2(0f, 0f);
    private float angularVelocityDeg = 0f;
    private boolean followEntity = true;

    /**
     * The ConeLight must be registered to the same rayHandler that is being rendered.
     * All light objects must be attached to the rayHandler.
     */
    public ConeLightComponent(RayHandler rayHandler,
                              int rays,
                              Color color,
                              float distance,
                              float directionDeg,
                              float coneDegree) {
        this.rayHandler = rayHandler;
        this.rays = rays;
        this.color = new Color(color);
        this.distance = distance;
        this.directionDeg = directionDeg;
        this.coneDegree = coneDegree;
        this.prio = ComponentPriority.HIGH;
    }

    @Override
    public void create() {
        // Initial position uses entities centre
        Vector2 p = entity.getPosition();
        coneLight = new ConeLight(rayHandler, rays, color, distance, p.x, p.y, directionDeg, coneDegree);
        coneLight.setSoftnessLength(1f);
        coneLight.setXray(false);

        short categoryBits = -1;
        short maskBits = (short)~PhysicsLayer.COLLECTABLE;
        short groupIndex = 0;
        coneLight.setContactFilter(categoryBits, groupIndex, maskBits);

        // only here for testing
        entity.getEvents().addListener("walk", this::setVelocity);
        entity.getEvents().addListener("walkStop", this::setVelocityZero);
        entity.getEvents().addListener("rotate", this::setAngularVelocityDeg);
    }

    @Override
    public void update() {
        if (coneLight == null) return;
        if (coneLight.isActive() != isActive) {
            coneLight.setActive(isActive);
        }

        // get the amount of time passed
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        if (dt <= 0f) dt = 0f;

        // rotation
        if (angularVelocityDeg != 0f) {
            // apply angular velocity to the direction degree (d = v * t)
            directionDeg += angularVelocityDeg * dt;
            // sets the new direction
            if (coneLight != null) {
                coneLight.setDirection(directionDeg);
            }
        }

        // kinematic motion
        if (velocity.len2() > 0f) {
            // gets the position vector of the entity
            Vector2 pos = entity.getPosition();
            // applies velocity to the entity
            pos.mulAdd(velocity, dt);
            entity.setPosition(pos);
        }

        // keep light synced to entity position if following
        if (followEntity && coneLight != null) {
            Vector2 c = entity.getCenterPosition();
            coneLight.setPosition(c.x, c.y);
        }
    }

    @Override
    public void dispose() {
        if (coneLight != null) {
            coneLight.remove();
            coneLight = null;
        }
    }

    public ConeLightComponent setVelocityZero() {
        this.velocity = new Vector2(0f, 0f);
        return this;
    }

    public ConeLightComponent setVelocity(Vector2 v) {
        this.velocity = v;
        return this;
    }

    public ConeLightComponent setAngularVelocityDeg(float w) {
        this.angularVelocityDeg = w;
        return this;
    }

    public ConeLightComponent setFollowEntity(boolean f) {
        this.followEntity = f;
        return this;
    }

    public ConeLightComponent setColor(Color c) {
        this.color.set(c);
        if (coneLight != null) coneLight.setColor(c);
        return this;
    }

    public ConeLightComponent setDistance (float d) {
        this.distance = d;
        return this;
    }

    public ConeLightComponent setDirectionDeg (float dirDeg) {
        this.directionDeg = dirDeg;
        if (coneLight != null) coneLight.setDirection(directionDeg);
        return this;
    }

    public ConeLightComponent setConeDegree(float coneDeg) {
        this.coneDegree = coneDeg;
        if (coneLight != null) coneLight.setConeDegree(coneDeg);
        return this;
    }

    public ConeLightComponent setSoftnessLength (float softness) {
        if (coneLight != null) coneLight.setSoftnessLength(softness);
        return this;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public ConeLight getLight() {
        return coneLight;
    }
    public float getConeDegree() {
        return coneDegree;
    }

    public float getDirectionDeg() {
        return directionDeg;
    }

    public float getDistance() {
        return distance;
    }
}