package com.csse3200.game.lighting;

import box2dLight.RayHandler;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.components.CameraComponent;

/**
 * Processes lighting components using the Box2DLight library. Sets up the ray handler that is responsible
 * for rendering all lights to the screen.
 */
public class LightingEngine implements Disposable {
    private final RayHandler rayHandler;
    private final CameraComponent camera;

    private float ambientLight = LightingDefaults.AMBIENT_LIGHT;

    /**
     * Constructor method for the lighting engine. This is where some of the rayHandler's
     * global variables are set and can be changed.
     *
     * @param camera The camera associated with the current renderer for the screen.
     * @param world The same world registered with the physics engine.
     */
    public LightingEngine(CameraComponent camera, World world) {
        this.camera = camera;
        rayHandler = new RayHandler(world);

        rayHandler.setAmbientLight(ambientLight);
        rayHandler.setBlur(true);
        rayHandler.setBlurNum(LightingDefaults.BLUR_NUM);
    }

    /**
     * Injectable constructor method used for junit tests.
     *
     * @param rayHandler mock rayHandler
     * @param camera camera component
     */
    LightingEngine(RayHandler rayHandler, CameraComponent camera) {
        this.rayHandler = rayHandler;
        this.camera = camera;
    }

    public RayHandler getRayHandler() {
        return rayHandler;
    }

    public void setAmbientLight(float ambientLight) {
        this.ambientLight = ambientLight;
        rayHandler.setAmbientLight(ambientLight);
    }

    /**
     * Render all lights registered with the ray handler to the screen. This also renders
     * a "shadow" over the entire screen which can be adjusted using the setAmbientLight function.
     */
    public void render() {
        rayHandler.setCombinedMatrix(camera.getProjectionMatrix());
        rayHandler.updateAndRender();
    }

    @Override
    public void dispose() {
        rayHandler.dispose();
    }
}
