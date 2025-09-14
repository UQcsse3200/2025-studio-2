package com.csse3200.game.entities.factories;

import box2dLight.RayHandler;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.lighting.ConeDetectorComponent;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.lighting.ConeLightPanningTaskComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.lighting.LightingDefaults;

/**
 * The security camera factory is used to spawn in new security camera entities. It consists of
 * static methods that initialise the various components based off of set default values.
 * This can ensure that all cameras stay uniform throughout the game.
 */
public class SecurityCameraFactory {

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
     * Creates a new security camera entity.
     * The id string can be looked up using the SecurityCamRetrievalService which will
     * return this created entity.
     *
     * @param target The target entity to be detected by the camera
     * @param angularVel The angular velocity at which the camera rotates at
     * @param id The id of the camera which will be registered with the retrieval service
     * @return The newly created security camera entity
     */
    public static Entity createSecurityCamera(Entity target, float angularVel, String id) {
        // create main entity
        Entity e = new Entity();
        ConeLightPanningTaskComponent pan = new ConeLightPanningTaskComponent(
                LightingDefaults.START_DEG, LightingDefaults.END_DEG, angularVel);
        e.addComponent(pan);
        TextureRenderComponent tex = new TextureRenderComponent("images/camera-body.png");
        tex.setLayer(2);
        e.addComponent(tex);
        e.setScale(1f, 22f / 28f);


        // panning component creates a child entity for the lens
        // the light component is added to the lens allowing it to move when the lens does
        Entity lens = pan.getCameraLens();
        lens.addComponent(new ConeLightComponent(rayHandler(),
                LightingDefaults.RAYS,
                LightingDefaults.NORMAL_COLOR,
                LightingDefaults.DIST,
                LightingDefaults.DOWN,
                LightingDefaults.CONE_DEG));
        lens.addComponent(new ConeDetectorComponent(target, LightingDefaults.OCCLUDER, id));
        // ensures the lens is rendered on top of the body (layer 1 by default)
        lens.addComponent(new TextureRenderComponent("images/camera-lens.png").setLayer(3));
        // change the color of the lens based off of the detection status
        lens.getEvents().addListener("targetDetected", (Entity p) ->
                lens.getComponent(ConeLightComponent.class).setColor(LightingDefaults.DETECTED_COLOR));
        lens.getEvents().addListener("targetLost", (Entity p) ->
                lens.getComponent(ConeLightComponent.class).setColor(LightingDefaults.NORMAL_COLOR));

        return e;
    }

    public static Entity createSecurityCamera(Entity target, float angularVel, float rotation, String id)  {
        // create main entity
        Entity e = new Entity();
        ConeLightPanningTaskComponent pan = new ConeLightPanningTaskComponent(
                LightingDefaults.START_DEG + rotation, LightingDefaults.END_DEG + rotation, angularVel);
        e.addComponent(pan);
        TextureRenderComponent tex = new TextureRenderComponent("images/camera-body.png");
        tex.setRotation(rotation);
        tex.setLayer(2);
        e.addComponent(tex);
        e.setScale(1f, 22f / 28f);

        // panning component creates a child entity for the lens
        // the light component is added to the lens allowing it to move when the lens does
        Entity lens = pan.getCameraLens();
        lens.addComponent(new ConeLightComponent(rayHandler(),
                LightingDefaults.RAYS,
                LightingDefaults.NORMAL_COLOR,
                LightingDefaults.DIST,
                LightingDefaults.DOWN,
                LightingDefaults.CONE_DEG));
        lens.addComponent(new ConeDetectorComponent(target, LightingDefaults.OCCLUDER, id));
        // ensures the lens is rendered on top of the body (layer 1 by default)
        TextureRenderComponent lensTex = new TextureRenderComponent("images/camera-lens.png");
        lensTex.setLayer(3);
        lens.addComponent(lensTex);

        // change the color of the lens based off of the detection status
        lens.getEvents().addListener("targetDetected", (Entity p) ->
                lens.getComponent(ConeLightComponent.class).setColor(LightingDefaults.DETECTED_COLOR));
        lens.getEvents().addListener("targetLost", (Entity p) ->
                lens.getComponent(ConeLightComponent.class).setColor(LightingDefaults.NORMAL_COLOR));


        return e;
    }
}
