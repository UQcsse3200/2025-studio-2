package com.csse3200.game.components.lighting;

import com.csse3200.game.components.Component;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

import java.security.Provider;

public class ConeLightPanningTaskComponent extends Component {
    private ConeLightComponent coneComp;
    private ConeDetectorComponent detectorComp;
    private float degreeStart;
    private float degreeEnd;
    private float angularVelocity;
    private boolean clockwise = true;
    private AnimationRenderComponent animator;

    public ConeLightPanningTaskComponent(float degreeStart, float degreeEnd, float angularVelocity) {
        if (degreeStart < degreeEnd) {
            this.degreeStart = degreeStart;
            this.degreeEnd = degreeEnd;
        } else {
            this.degreeStart = degreeEnd;
            this.degreeEnd = degreeStart;
        }

        this.angularVelocity = angularVelocity;

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
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        float dir = coneComp.getLight().getDirection();

        // change clockwise based off dir
        if (dir >= degreeEnd) {
            clockwise = false;
            animator.startAnimation("right-left");
        } else if (dir <= degreeStart) {
            clockwise = true;
            animator.startAnimation("left-right");
        }

        // move if not detected
        if (!detectorComp.isDetected()) {
            animator.setPaused(false);
            if (clockwise) {
                coneComp.setDirectionDeg(dir + angularVelocity * dt);
            } else {
                coneComp.setDirectionDeg(dir - angularVelocity * dt);
            }
        } else {
            animator.setPaused(true);
        }
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
