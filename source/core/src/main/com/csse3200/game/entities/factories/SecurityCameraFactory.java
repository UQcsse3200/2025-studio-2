package com.csse3200.game.entities.factories;

import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.lighting.ConeDetectorComponent;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.lighting.ConeLightPanningTaskComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * The security camera factory is used to spawn in new security camera entities. It consists of
 * static methods that initialise the various components based off of set default values.
 * This can ensure that all cameras stay uniform throughout the game.
 */
public class SecurityCameraFactory {

    // default values (can be changed but for now they work well)
    private static final float START_DEG = -135;
    private static final float END_DEG = -45;
    private static final int DEFAULT_RAYS = 128;
    private static final float DEFAULT_DIST = 5f;
    private static final float DEFAULT_CONE_DEG = 35f;
    private static final float DOWN = -90f;
    private static final Color NORMAL_COLOR = new Color(230f/255f, 210f/255f, 140f/255f, 70f/100f);
    private static final Color DETECTED_COLOR = Color.RED;
    private static final short DEFAULT_OCCLUDER = PhysicsLayer.OBSTACLE;

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
        ConeLightPanningTaskComponent pan = new ConeLightPanningTaskComponent(START_DEG, END_DEG, angularVel);
        e.addComponent(pan);
        e.addComponent(new TextureRenderComponent("images/camera-body.png"));

        // panning component creates a child entity for the lens
        // the light component is added to the lens allowing it to move when the lens does
        Entity lens = pan.getCameraLens();
        lens.addComponent(new ConeLightComponent(rayHandler(),
                DEFAULT_RAYS,
                NORMAL_COLOR,
                DEFAULT_DIST,
                DOWN,
                DEFAULT_CONE_DEG));
        lens.addComponent(new ConeDetectorComponent(target, DEFAULT_OCCLUDER, id));
        // ensures the lens is rendered on top of the body (layer 1 by default)
        lens.addComponent(new TextureRenderComponent("images/camera-lens.png").setLayer(2));
        // change the color of the lens based off of the detection status
        lens.getEvents().addListener("targetDetected", (Entity p) ->
                lens.getComponent(ConeLightComponent.class).setColor(DETECTED_COLOR));
        lens.getEvents().addListener("targetLost", (Entity p) ->
                lens.getComponent(ConeLightComponent.class).setColor(NORMAL_COLOR));

        return e;
    }
}
