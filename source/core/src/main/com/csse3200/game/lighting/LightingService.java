package com.deco2800.game.lighting;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.World;
import com.deco2800.game.components.CameraComponent;

public class LightingService {
    private final LightingEngine engine;

    public LightingService(LightingEngine engine) {
        this.engine = engine;
    }

    public LightingService(CameraComponent camera, World world) {
        this.engine = new LightingEngine(camera, world);
    }

    public LightingEngine getEngine() {
        return this.engine;
    }
}
