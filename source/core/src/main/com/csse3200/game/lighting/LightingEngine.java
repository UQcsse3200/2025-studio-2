package com.csse3200.game.lighting;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.components.CameraComponent;

public class LightingEngine implements Disposable {
    private final RayHandler rayHandler;
    private final CameraComponent camera;

    public LightingEngine(CameraComponent camera, World world) {
        this.camera = camera;
        rayHandler = new RayHandler(world);

        rayHandler.setAmbientLight(0.75f);
        rayHandler.setBlur(true);
    }

    public RayHandler getRayHandler() {
        return rayHandler;
    }

    public void renderClipped(int x, int y, int width, int height) {
        rayHandler.setCombinedMatrix(camera.getProjectionMatrix());

        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(x, y, width, height);
        //rayHandler.useCustomViewport(x, y, width, height);
        rayHandler.updateAndRender();
        //rayHandler.useDefaultViewport();
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
    }

    public void render() {
        rayHandler.setCombinedMatrix(camera.getProjectionMatrix());
        rayHandler.updateAndRender();
    }

    @Override
    public void dispose() {
        rayHandler.dispose();
    }
}
