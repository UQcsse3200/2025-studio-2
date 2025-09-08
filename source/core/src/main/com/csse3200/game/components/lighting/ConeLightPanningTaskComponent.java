package com.csse3200.game.components.lighting;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;


public class ConeLightPanningTaskComponent extends Component {
    private ConeLightComponent coneComp;
    private ConeDetectorComponent detectorComp;
    private float degreeStart;
    private float degreeEnd;
    private float angularVelocity;
    private boolean clockwise = true;
    private AnimationRenderComponent animator;
    private Entity target;
    private static final float angularAccel = 0.2f;
    private float currentVel = 0;
    private final float maxSpeed;

    public ConeLightPanningTaskComponent(float degreeStart, float degreeEnd, float angularVelocity) {
        if (degreeStart < degreeEnd) {
            this.degreeStart = degreeStart;
            this.degreeEnd = degreeEnd;
        } else {
            this.degreeStart = degreeEnd;
            this.degreeEnd = degreeStart;
        }

        this.angularVelocity = angularVelocity;
        maxSpeed = angularVelocity * 2.5f;

    }

    @Override
    public void create() {
        this.coneComp = entity.getComponent(ConeLightComponent.class);
        if (coneComp == null) {
            throw new IllegalStateException("ConeLightComponent must be attached to host entity before panning task");
        }
        this.animator = entity.getComponent(AnimationRenderComponent.class);
        if (animator == null) {
            throw new IllegalStateException("AnimationRenderComponent must be attached to host entity before panning task");
        }
        this.detectorComp = entity.getComponent(ConeDetectorComponent.class);
        if (detectorComp == null) {
            throw new IllegalStateException("ConeDetectorComponent must be attached to host entity before panning task");
        }

        coneComp.setDirectionDeg(degreeStart);
        animator.startAnimation("left-right");
        target = detectorComp.getTarget();
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        float dir = coneComp.getLight().getDirection();

        if (!detectorComp.isDetected()) {
            // PANNING MODE

            animator.setPaused(false);
            currentVel = angularVelocity;
            // change clockwise based off dir
            if (dir >= degreeEnd) {
                clockwise = false;
                animator.startAnimation("right-left");
            } else if (dir <= degreeStart) {
                clockwise = true;
                animator.startAnimation("left-right");
            }

            // move angle
            if (clockwise) {
                coneComp.setDirectionDeg(dir + angularVelocity * dt);
            } else {
                coneComp.setDirectionDeg(dir - angularVelocity * dt);
            }
        } else {

            // TRACKING MODE
            animator.setPaused(true);

            // move towards player increasingly fast
            Vector2 toLight = target.getPosition().cpy().sub(entity.getPosition());
            float toAngle = toLight.angleDeg();
            float delta = wrapDeg(toAngle - dir);
            currentVel = Math.min(currentVel + angularVelocity * dt, maxSpeed);
            float step = currentVel * dt;
            if (Math.abs(delta) <= step) {
                coneComp.setDirectionDeg(toAngle);
                // bleed speed when locked on
                currentVel = Math.max(currentVel * 0.9f, angularVelocity);
            } else if (delta > 0){
                coneComp.setDirectionDeg(dir + step);
            } else {
                coneComp.setDirectionDeg(dir - step);
            }
        }
        // stayInBounds(dir);
    }

    private void stayInBounds(float dir) {
        if (dir > degreeEnd) {
            coneComp.setDirectionDeg(degreeEnd);
        } else if (dir < degreeStart) {
            coneComp.setDirectionDeg(degreeStart);
        }
    }

    private static float wrapDeg(float a) {
        a = a % 360f;
        if (a >= 180) a -= 360f;
        if (a < -180) a += 360f;
        return a;
    }

    public void setAngularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public void setDegreeStart (float degreeStart) {
        this.degreeStart = degreeStart;
    }

    public void setDegreeEnd (float degreeEnd) {
        this.degreeEnd = degreeEnd;
    }
}
