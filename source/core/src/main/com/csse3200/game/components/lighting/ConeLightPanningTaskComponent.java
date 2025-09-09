package com.csse3200.game.components.lighting;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;


public class ConeLightPanningTaskComponent extends Component {
    private ConeLightComponent coneComp;
    private ConeDetectorComponent detectorComp;
    private float degreeStart;
    private float degreeEnd;
    private float angularVelocity;
    private boolean clockwise = true;
    private Entity target;
    private static final float angularAccel = 0.225f;
    private float currentVel = 0;
    private final float maxSpeed;
    private boolean tracking = false;
    private float movSign = 1f;

    private final Entity cameraLens;
    private static float maxLensMov = 0.3f;

    public ConeLightPanningTaskComponent(float degreeStart, float degreeEnd, float angularVelocity) {
        if (degreeStart < degreeEnd) {
            this.degreeStart = wrapDeg(degreeStart);
            this.degreeEnd = wrapDeg(degreeEnd);
        } else {
            this.degreeStart = wrapDeg(degreeEnd);
            this.degreeEnd = wrapDeg(degreeStart);
        }

        this.angularVelocity = angularVelocity;
        maxSpeed = angularVelocity * 3f;

        cameraLens = new Entity();
    }

    public Entity getCameraLens() {
        return cameraLens;
    }

    @Override
    public void create() {
        cameraLens.setPosition(entity.getPosition());
        ServiceLocator.getEntityService().register(cameraLens);

        this.coneComp = cameraLens.getComponent(ConeLightComponent.class);
        if (coneComp == null) {
            throw new IllegalStateException("ConeLightComponent must be attached to host entity before panning task");
        }
        this.detectorComp = cameraLens.getComponent(ConeDetectorComponent.class);
        if (detectorComp == null) {
            throw new IllegalStateException("ConeDetectorComponent must be attached to host entity before panning task");
        }

        coneComp.setDirectionDeg(degreeStart);
        target = detectorComp.getTarget();
        maxLensMov *= entity.getScale().x;
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        float dir = wrapDeg(coneComp.getLight().getDirection());
        boolean detected = detectorComp.isDetected();
        float lensCentre = entity.getPosition().x;

        if (!detected) {
            // PANNING MODE
            // keep going in the same direction
            if (tracking) {
                clockwise = (movSign < 0f);
            }
            currentVel = angularVelocity;

            // change clockwise based off dir
            if (dir >= degreeEnd) {
                clockwise = true;
                // move left
            } else if (dir <= degreeStart) {
                clockwise = false;
                // move right
            }

            // move angle
            float step = angularVelocity * dt;
            coneComp.setDirectionDeg(clockwise ? dir - step : dir + step);

        } else {
            // TRACKING MODE

            // move towards player increasingly fast
            Vector2 toLight = target.getPosition().cpy().sub(cameraLens.getPosition());
            float targetAng = wrapDeg(toLight.angleDeg());
            float aimAng = clampToRange(targetAng, degreeStart, degreeEnd);

            float delta = wrapDeg(aimAng - dir);
            currentVel = Math.min(currentVel + angularVelocity * dt, maxSpeed);
            float step = currentVel * dt;

            if (Math.abs(delta) <= step) {
                coneComp.setDirectionDeg(aimAng);
                // bleed speed when locked on
                currentVel = Math.max(currentVel * 0.95f, angularVelocity);
            } else {
                // move along shortest distance and remember the sign
                movSign = Math.signum(delta);
                coneComp.setDirectionDeg(dir + movSign * step);
            }
        }

        // rescale lens x pos based off of the cone degree bounds and current dir
        float lensX = lensCentre + (maxLensMov / (degreeEnd - degreeStart)) * (dir + 90);
        cameraLens.setPosition(lensX, entity.getPosition().y);
        // remember tracking state
        tracking = detected;
    }

    private static float wrapDeg(float a) {
        a = a % 360f;
        if (a >= 180) a -= 360f;
        if (a < -180) a += 360f;
        return a;
    }

    private static float clampToRange(float a, float start, float end) {
        a = wrapDeg(a);
        if (a < start) return start;
        if (a > end) return end;
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

    @Override
    public void dispose() {
        cameraLens.dispose();
        ServiceLocator.getEntityService().unregister(cameraLens);
    }
}
