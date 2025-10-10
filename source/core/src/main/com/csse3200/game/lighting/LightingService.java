package com.csse3200.game.lighting;

import com.badlogic.gdx.physics.box2d.World;
import com.csse3200.game.components.CameraComponent;

/**
 * Provides a global access point to the lighting engine. This is necessary for the lighting based
 * entities to register themselves with the engine.
 */
public record LightingService(LightingEngine engine) {

  public LightingService(CameraComponent camera, World world) {
    this(new LightingEngine(camera, world));
  }
}
