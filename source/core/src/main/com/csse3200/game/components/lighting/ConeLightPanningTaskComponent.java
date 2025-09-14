package com.csse3200.game.components.lighting;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.lighting.LightingDefaults;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * This component is responsible for the movement of the security camera light. It has two main
 * tasks, panning mode when the target isn't detected and tracking mode when the target is.
 * In panning mode it moves at a constant angular velocity defined at creation and within the bounds
 * defined. Once in tracking mode it moves towards the target gaining speed based on a defined constant
 * acceleration.
 * <p>
 * This component also spawns in a child entity for the camera lens. This allows the lens to move dynamically
 * and not be defined to a set animation. The component also handles the disposal of the child lens.
 */
public class ConeLightPanningTaskComponent extends Component {
    private ConeLightComponent coneComp;
    private ConeDetectorComponent detectorComp;
    private float degreeStart;
    private float degreeEnd;
    private float angularVelocity;
    private boolean clockwise = false;
    private Entity target;

    private float angularAccel = LightingDefaults.ANGULAR_ACC;
    private float currentVel   = LightingDefaults.ANGULAR_VEL;
    private float maxSpeed;


    private float movSign = 1f;
    private boolean tracking = false;

    // camera lens stuff
    private final Entity cameraLens;
    private static float maxLensMov = 0.3f;
    private float rotation = 0f;
    private Vector2 lensInitPos;
    private Vector2 uBodyX;
    private Vector2 lensNeutralCenter;

    private static final float bodyW = 1f;
    private static final float bodyH = 22f / 28f;
    private static final float lensW = 9f / 28f * bodyW;
    private static final float lensH = 9f / 22f * bodyH;

    public ConeLightPanningTaskComponent(float degreeStart, float degreeEnd, float angularVelocity) {
        // just to make calculations easier, normalise all values
        if (degreeStart < degreeEnd) {
            this.degreeStart = wrapDeg(degreeStart);
            this.degreeEnd = wrapDeg(degreeEnd);
        } else {
            this.degreeStart = wrapDeg(degreeEnd);
            this.degreeEnd = wrapDeg(degreeStart);
        }

        // set max speed based on angular vel
        this.angularVelocity = angularVelocity;
        maxSpeed = angularVelocity * 3f;

        // spawn new child entity
        cameraLens = new Entity();
    }

    /**
     * Gets the created child lens entity
     *
     * @return The lens entity
     */
    public Entity getCameraLens() {
        return cameraLens;
    }

    @Override
    public void create() {
        // centre lens and register it with entity service
        centreLens();
        lensInitPos = cameraLens.getPosition().cpy();
        ServiceLocator.getEntityService().register(cameraLens);

        // compute rotation + local x axis once
        rotation = (float) entity.getComponent(TextureRenderComponent.class).getRotation();
        uBodyX = new Vector2(1f, 0f).rotateDeg(rotation);

        // use the centered position as the neutral centre
        lensNeutralCenter = lensInitPos.cpy().add(lensW / 2f, lensH / 2f);

        this.coneComp = cameraLens.getComponent(ConeLightComponent.class);
        if (coneComp == null) {
            throw new IllegalStateException("ConeLightComponent must be attached to lens entity");
        }
        this.detectorComp = cameraLens.getComponent(ConeDetectorComponent.class);
        if (detectorComp == null) {
            throw new IllegalStateException("ConeDetectorComponent must be attached to lens entity");
        }

        // set cone to bounds (could be omitted tho)
        coneComp.setDirectionDeg(degreeStart);
        target = detectorComp.getTarget();
        // scale lens movement bounds with x scale
        maxLensMov *= entity.getScale().x;
    }

    private void centreLens() {
        cameraLens.setScale(lensW, lensH);

        float anchorX = 0.5f;
        float anchorY = 0.3f;

        Vector2 p = entity.getPosition();

        float rotDeg = 0f;
        TextureRenderComponent tex = entity.getComponent(TextureRenderComponent.class);
        if (tex != null) {
            rotDeg = (float) tex.getRotation();
        }
        Vector2 bodyCenter = new Vector2(p.x + bodyW / 2f, p.y + bodyH / 2f);
        Vector2 localFromCenter = new Vector2(
                (anchorX - 0.5f) * bodyW,
                (anchorY - 0.5f) * bodyH
        );
        localFromCenter.rotateDeg(rotDeg);
        Vector2 center = bodyCenter.cpy().add(localFromCenter);

        cameraLens.setPosition(center.x - lensW / 2f, center.y - lensH / 2f);
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        float dir = wrapDeg(coneComp.getLight().getDirection());
        boolean detected = detectorComp.isDetected();

        if (!detected) {
            // PANNING MODE
            // keep going in the same direction after returning to panning mode
            if (tracking) {
                clockwise = (movSign < 0f);
            }
            // reset velocity (removes effects from acceleration)
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

            // get vector from the target to the light
            Vector2 toLight = target.getPosition().cpy().sub(cameraLens.getPosition());
            float targetAng = wrapDeg(toLight.angleDeg());
            // clamp the target angle so it stays in the bounds
            float aimAng = clampToRange(targetAng, degreeStart, degreeEnd);

            // get different in angles
            float delta = wrapDeg(aimAng - dir);
            // change velocity based on acceleration, clamp to max speed
            currentVel = Math.min(currentVel + angularAccel * dt, maxSpeed);
            float step = currentVel * dt;

            // if angle is close enough to target then set angle = target (locked on)
            if (Math.abs(delta) <= step) {
                coneComp.setDirectionDeg(aimAng);
                // bleed speed when locked on
                currentVel = Math.max(currentVel * 0.98f, angularVelocity);
            } else {
                // move along shortest distance and remember the sign
                movSign = Math.signum(delta);
                coneComp.setDirectionDeg(dir + movSign * step);
            }
        }

        // rescale lens x pos based off of the cone degree bounds and current dir
        // map current dir int [0..1] across the panning span, then to [-max..+max]
        float span = degreeEnd - degreeStart;
        float t = (dir - degreeStart) / span;
        t = Math.max(0f, Math.min(1f, t)); // clamp to [0..1]

        // slide amount along the body's local x axis
        float slideAmount = (t - 0.5f) * maxLensMov;

        // project along local x
        Vector2 center = lensNeutralCenter.cpy().add(uBodyX.cpy().scl(slideAmount));
        cameraLens.setPosition(center.x - lensW / 2f, center.y - lensH / 2f);
        // remember tracking state
        tracking = detected;
    }

    /**
     * Helper method to wrap an angle to be between (-180, 180)
     *
     * @param a Angle
     * @return Wrapped angle within range
     */
    private static float wrapDeg(float a) {
        a = a % 360f;
        if (a >= 180) a -= 360f;
        if (a < -180) a += 360f;
        return a;
    }

    /**
     * Helper method to clamp angle between a range.
     *
     * @param a Angle
     * @param start Lower bound
     * @param end Upper bound
     * @return Clamped angle within range
     */
    private static float clampToRange(float a, float start, float end) {
        a = wrapDeg(a);
        if (a < start) return start;
        if (a > end) return end;
        return a;
    }

    /**
     * Set the angular velocity and change max speed based upon new value.
     *
     * @param angularVelocity New angular velocity
     */
    public void setAngularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
        this.maxSpeed = angularVelocity * 3f;
    }

    public void setAngularAccel(float angularAccel) {
        this.angularAccel = angularAccel;
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
