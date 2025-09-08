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
    private boolean tracking = false;
    private float dirBeforeTrack;

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

            // return to prev pos
            if (tracking) {
                if (Math.abs(dir - dirBeforeTrack) < 0.1f) {
                    tracking = false;
                }
                moveToAngle(dir, dirBeforeTrack, dt);
                return;
            }
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
            float step = angularVelocity * dt;
            if (clockwise) {
                coneComp.setDirectionDeg(dir + step);
            } else {
                coneComp.setDirectionDeg(dir - step);
            }
        } else {
            if (!tracking) {
                dirBeforeTrack = dir;
                tracking = true;
            }
            // TRACKING MODE
            animator.setPaused(true);

            // move towards player increasingly fast
            Vector2 toLight = target.getPosition().cpy().sub(entity.getPosition());
            moveToAngle(dir, toLight.angleDeg(), dt);
        }
        // stayInBounds(dir);
    }

    private void moveToAngle(float dir, float toAngle, float dt) {
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
