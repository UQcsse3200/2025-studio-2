package com.deco2800.game.entities.factories;

import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.deco2800.game.components.lighting.ConeLightComponent;
import com.deco2800.game.components.lighting.KeyboardLightingInputTestComponent;
import com.deco2800.game.entities.Entity;
import com.deco2800.game.lighting.LightingService;
import com.deco2800.game.services.ServiceLocator;

public class LightFactory {

    private static RayHandler rayHandler() {
        LightingService lighting = ServiceLocator.getLightingService();
        if (lighting == null || lighting.getEngine() == null) {
            throw new IllegalStateException("LightingService/Engine not initialised.");
        }
        return lighting.getEngine().getRayHandler();
    }

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

    private LightFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}
