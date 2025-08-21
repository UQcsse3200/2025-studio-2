package com.deco2800.game.components.lighting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.deco2800.game.components.Component;
import box2dLight.RayHandler;
import box2dLight.ConeLight;
import com.deco2800.game.services.ServiceLocator;

public class ConeLightComponent extends Component {
    private final RayHandler rayHandler;
    private ConeLight coneLight;

    private int rays;
    private Color color;
    private float distance;
    private float directionDeg;
    private float coneDegree;

    // Movement/rotation
    private Vector2 velocity = new Vector2(0f, 0f);
    private float angularVelocityDeg = 0f;
    private boolean followEntity = true;

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
    }

    @Override
    public void create() {
        // Initial position uses entities centre
        Vector2 p = entity.getPosition();
        coneLight = new ConeLight(rayHandler, rays, color, distance, p.x, p.y, directionDeg, coneDegree);
        coneLight.setSoftnessLength(1f);
        coneLight.setXray(false);
        entity.getEvents().addListener("walk", this::setVelocity);
        entity.getEvents().addListener("walkStop", this::setVelocityZero);
        entity.getEvents().addListener("rotate", this::setAngularVelocityDeg);
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        if (dt <= 0f) dt = 0f;

        // rotation
        if (angularVelocityDeg != 0f) {
            directionDeg += angularVelocityDeg * dt;
            if (coneLight != null) {
                coneLight.setDirection(directionDeg);
            }
        }

        // kinematic motion
        if (velocity.len2() > 0f) {
            Vector2 pos = entity.getPosition();
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
            coneLight.dispose();
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

    public ConeLight getLight() {
        return coneLight;
    }
}