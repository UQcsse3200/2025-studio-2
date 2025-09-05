package com.csse3200.game.components.lighting;

import com.csse3200.game.components.Component;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.services.ServiceLocator;

public class ConeLightPanningTaskComponent extends Component {
    private ConeLightComponent coneComp;
    private float degreeStart;
    private float degreeEnd;
    private float angularVelocity;
    private boolean clockwise = true;

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
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        float dir = coneComp.getLight().getDirection();

        // change clockwise based off dir
        if (dir >= degreeEnd) {
            clockwise = false;
        } else if (dir <= degreeStart) {
            clockwise = true;
        }

        // move
        if (clockwise) {
            coneComp.setDirectionDeg(dir + angularVelocity * dt);
        } else {
            coneComp.setDirectionDeg(dir - angularVelocity * dt);
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
