package com.csse3200.game.lighting;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
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

        rayHandler.setAmbientLight(0.75f);
        rayHandler.setBlur(true);
    }

    public RayHandler getRayHandler() {
        return rayHandler;
    }

    /**
     * Disregard this function for now. Was used to try and test cutting the renderable area of the
     * lighting engine.
     */
    public void renderClipped(int x, int y, int width, int height) {
        rayHandler.setCombinedMatrix(camera.getProjectionMatrix());

        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(x, y, width, height);
        //rayHandler.useCustomViewport(x, y, width, height);
        rayHandler.updateAndRender();
        //rayHandler.useDefaultViewport();
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
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
