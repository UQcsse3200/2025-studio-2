package com.csse3200.game.components.lighting;

import box2dLight.ConeLight;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.services.ServiceLocator;

/**
 * Component that is responsible for detecting if a target entity is within the light cone.
 * It also detects whether the target entity is hidden behind another entity with a specified
 * physics layer.
 */
public class ConeDetectorComponent extends Component {
    private final Entity target;
    private short occluderMask = PhysicsLayer.OBSTACLE;

    private PhysicsEngine physicsEngine;
    private DebugRenderer debug;
    private ConeLightComponent coneComp;
    private final RaycastHit hit = new RaycastHit();
    private final String id;

    private boolean detected = false;
    private boolean debugLines = false;

    /**
     * Constructor for the ConeDetector.
     * The occluder mask is set to "PhysicsLayer.OBSTACLE" by default.
     *
     * @param target the target entity to be detected
     */
    public ConeDetectorComponent(Entity target, String id) {
        this.target = target;
        this.id = id;
    }

    public ConeDetectorComponent(Entity target, short occluderMask, String id) {
        this.target = target;
        this.occluderMask = occluderMask;
        this.id = id;
    }

    public ConeDetectorComponent setDebug(boolean enabled) {
        debugLines = true;
        return this;
    }

    public ConeDetectorComponent setOccluderMask(short mask) {
        occluderMask = mask;
        return this;
    }

    @Override
    public void create() {
        physicsEngine = ServiceLocator.getPhysicsService().getPhysics();
        debug = ServiceLocator.getRenderService().getDebug();
        coneComp = entity.getComponent(ConeLightComponent.class);
        // if there is no ConeLightComponent throw an exception
        if (coneComp == null) {
            throw new IllegalStateException("ConeDetectorComponent requires a ConeLightComponent on the same entity.");
        }
    }

    @Override
    public void update() {
        boolean nowDetected = computeDetection();
        if (nowDetected != detected) {
            detected = nowDetected;
            if (detected) {
                entity.getEvents().trigger("targetDetected", target);
            } else {
                entity.getEvents().trigger("targetLost", target);
            }
        }
    }

    public boolean isDetected() {
        return detected;
    }

    /**
     * A private helper class that does most of the heavy lifting of calculations.
     * I've commented more details of how each section works within the code but might put
     * more details on the wiki (if i feel like it).
     *
     * @return true iff the target entity is within the cone light
     */
    private boolean computeDetection() {
        // initial error checking tests
        if (target == null) return false;
        ConeLight light = coneComp.getLight();
        if (light == null) return false;

        // Positions
        Vector2 lightPos = entity.getCenterPosition();
        Vector2 targetPos = target.getCenterPosition();

        /*
        * Quick cone test
        * This checks to see if the target entity is within the cone lights specified distance.
        * It essentially checks if the target is within the correct radius of the light.
        **/
        Vector2 toTarget = targetPos.cpy().sub(lightPos);
        float dist = toTarget.len();
        if (dist > light.getDistance()) {
            return false;
        }

        /*
        * Angle test
        * This part checks to see if the target entity is within the right angle of the cone.
        * Still without regards for collisions however, but it confines out search for the target
        * entity from a circular radius to now a wedge (cone) of that radius.
        **/
        float toAngle = toTarget.angleDeg();
        float dir = light.getDirection();
        float diff = angleDiffDeg(toAngle, dir);
        if (diff > light.getConeDegree()) {
            return false;
        }

        // Line-of-sight test: if an occluder is between the light and the player, player is not detected
        /*
        * Finally we check if an entity with the specified occluder mask physics layer is in the way.
        * It uses the already defined raycast method within the physics engine (this is also used for the
        * enemy line of sight detection).
        * Also draws the lines to the debug renderer to help debug.
        **/
        boolean blocked = physicsEngine.raycast(lightPos, targetPos, occluderMask, hit);
        if (debugLines && debug != null) {
            if (blocked) {
                debug.drawLine(lightPos, hit.point); // will be clipped to first hit
            } else {
                debug.drawLine(lightPos, targetPos);
            }
        }
        return !blocked;
    }

    /**
     * Helper method to calculate the difference of 2 angles.
     *
     * @param a angle 1
     * @param b angle 2
     * @return angle difference
     */
    private static float angleDiffDeg(float a, float b) {
        float d = (a - b) % 360f;
        // keep the range between (-180, 180)
        if (d < -180f) d += 360f;
        else if (d > 180f) d -= 360f;
        return Math.abs(d);
    }

}
