package com.csse3200.game.components.lighting;

import box2dLight.ConeLight;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.services.ServiceLocator;

public class ConeDetectorComponent extends Component {
    private final Entity target;
    private short occluderMask = PhysicsLayer.OBSTACLE;

    private PhysicsEngine physicsEngine;
    private DebugRenderer debug;
    private ConeLightComponent coneComp;
    private final RaycastHit hit = new RaycastHit();

    private boolean detected = false;
    private boolean debugLines = false;

    public ConeDetectorComponent(Entity target) {
        this.target = target;
    }

    public ConeDetectorComponent(Entity target, short occluderMask) {
        this.target = target;
        this.occluderMask = occluderMask;
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

    private boolean computeDetection() {
        if (target == null) return false;
        ConeLight light = coneComp.getLight();
        if (light == null) return false;

        // Positions
        Vector2 lightPos = entity.getCenterPosition();
        Vector2 targetPos = target.getCenterPosition();

        // Quick cone test
        Vector2 toTarget = targetPos.cpy().sub(lightPos);
        float dist = toTarget.len();
        if (dist > light.getDistance()) {
            return false;
        }

        float toAngle = toTarget.angleDeg();
        float dir = light.getDirection();
        float diff = angleDiffDeg(toAngle, dir);
        if (diff > light.getConeDegree()) {
            return false;
        }

        // Line-of-sight test: if an occluder is between the light and the player, player is not detected
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

    private static float angleDiffDeg(float a, float b) {
        float d = (a - b) % 360f;
        if (d < -180f) d += 360f;
        else if (d > 180f) d -= 360f;
        return Math.abs(d);
    }

}
