package com.csse3200.game.entities.factories;

import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.components.lighting.ConeDetectorComponent;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.lighting.KeyboardLightingInputTestComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.services.ServiceLocator;

/**
 * The light factory is used to create the various light entities and apply the corresponding components to them.
 * The implementation is currently up for change as it has been configured for debugging.
 */
public class LightFactory {

    /**
     * Helper class to get the rayHandler from the ServiceLocator. Checks to see that the lighting
     * engine as well as the lighting service has been initialised.
     *
     * @return the rayHandler if located, otherwise throws an exception.
     */
    private static RayHandler rayHandler() {
        LightingService lighting = ServiceLocator.getLightingService();
        if (lighting == null || lighting.getEngine() == null) {
            throw new IllegalStateException("LightingService/Engine not initialised.");
        }
        return lighting.getEngine().getRayHandler();
    }

    /**
     * Creates a new static cone light entity.
     *
     * @param rays the number of rays (resolution) of the light (I found that 128 works well)
     * @param color the colour of the light
     * @param distance the distance the light travels
     * @param directionDeg the direction the light points in
     * @param coneDeg the angle width of the cone light
     * @return the cone light entity created
     */
    public static Entity createConeLight(int rays,
                                         Color color,
                                         float distance,
                                         float directionDeg,
                                         float coneDeg) {
        Entity e = new Entity();
        e.addComponent(new ConeLightComponent(rayHandler(), rays, color, distance, directionDeg, coneDeg));
        e.addComponent(new KeyboardLightingInputTestComponent());
        return e;
    }

    /**
     * Creates a new rotating cone light entity with a fixed angular velocity.
     * The light would be fixed in place and pivot on the spot.
     *
     * @param rays the number of rays (resolution) of the light
     * @param color the colour of the light
     * @param distance the distance the light travels
     * @param directionDeg the direction the light points in
     * @param coneDeg the angle width of the cone light
     * @param angularVelDeg the angular velocity of the rotating light.
     * @return the cone light entity created
     */
    public static Entity createRotatingConeLight(int rays,
                                                 Color color,
                                                 float distance,
                                                 float directionDeg,
                                                 float coneDeg,
                                                 float angularVelDeg) {
        Entity e = createConeLight(rays, color, distance, directionDeg, coneDeg);
        e.getComponent(ConeLightComponent.class).setAngularVelocityDeg(angularVelDeg);
        return e;
    }

    /**
     * Creates a detecting light that can detect a target entity within the light. The target entity
     * can also hide behind any objects with the same physics layer as the occluderMask.
     * The implementation currently just changed the colour of the light from green to red if detected.
     *
     *
     * @param target the target entity to be detected
     * @param occluderMask the physics layer to block the light
     * @param rays the number of rays (resolution) of the light
     * @param color the colour of the light
     * @param distance the distance the light travels
     * @param directionDeg the direction the light points in
     * @param coneDeg the angle width of the cone light
     * @return the cone light entity created
     */
    public static Entity createSecurityLight(Entity target,
                                             short occluderMask,
                                             int rays,
                                             Color color,
                                             float distance,
                                             float directionDeg,
                                             float coneDeg) {
        Entity e = createConeLight(rays, color, distance, directionDeg, coneDeg);
        e.addComponent(new ConeDetectorComponent(target, occluderMask).setDebug(true));
        e.getEvents().addListener("targetDetected", (Entity p) ->
                e.getComponent(ConeLightComponent.class).setColor(Color.RED));
        e.getEvents().addListener("targetLost", (Entity p) ->
                e.getComponent(ConeLightComponent.class).setColor(Color.GREEN));
        return e;
    }

    private LightFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}
