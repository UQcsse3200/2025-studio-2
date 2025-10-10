package com.csse3200.game.entities.factories;

import box2dLight.ConeLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.lighting.SecurityCamRetrievalService;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class LightFactoryTest {

    @Test
    void createConeLight_buildsAndStoresCorrectArgs() {
        // capture args from ConeLight
        List<List<?>> capturedArgs = new ArrayList<>();

        /*
        * mock the rayHandler and coneLight constructors.
        * this annoyingly had to be done otherwise there were null pointer errors
        * when trying to add the coneLight to the rayHandler which has an internal list of
        * all lights added to it. the rayHandler also has to be mocked as it taps into
        * different rendering services which aren't available when testing.
        **/
        try (MockedConstruction<RayHandler> rhCons = mockConstruction(RayHandler.class);
             MockedConstruction<ConeLight> coneCons = mockConstruction(
                     ConeLight.class,
                     (mock, ctx) -> {
                         capturedArgs.add(ctx.arguments());
                         when(mock.getDistance()).thenReturn((Float) ctx.arguments().get(3));
                         when(mock.getConeDegree()).thenReturn((Float) ctx.arguments().get(7));
                         when(mock.getDirection()).thenReturn((Float) ctx.arguments().get(6));
                         when(mock.getColor()).thenReturn((Color) ctx.arguments().get(2));
                     })) {
            // Uses the production constructor but the "new" is intercepted above
            ServiceLocator.registerLightingService(createLightingService());

            int rays = 64;
            Color color = Color.WHITE;
            float distance = 5f, dir = 45f, cone = 30f;

            Entity e = LightFactory.createConeLight(rays, color, distance, dir, cone);
            e.create();

            // check that the constructor is only called once with the expected args
            assertEquals(1, coneCons.constructed().size());
            List<?> args = capturedArgs.getFirst();
            // ConeLight(rh, rays, color, distance, x, y, directionDeg, coneDeg)
            assertEquals(rays, args.get(1));
            assertEquals(color, args.get(2));
            assertEquals(distance, (Float) args.get(3), 1e-4);
            assertEquals(dir, (Float) args.get(6), 1e-4);
            assertEquals(cone, (Float) args.get(7), 1e-4);

            // Component holds the same mock instance
            ConeLight stored = e.getComponent(ConeLightComponent.class).getLight();
            assertSame(coneCons.constructed().getFirst(), stored);
        }
    }

    @Test
    void createRotatingConeLight_updatesDirectionCorrectly() {
        // mock both constructors again
        try (var rhCons = mockConstruction(RayHandler.class);
             var coneCons = mockConstruction(ConeLight.class)) {
            // register lighting service/engine
            ServiceLocator.registerLightingService(createLightingService());

            // create rotating light
            float dir0 = 10f, cone = 20f, angVel = 90f;
            var e = LightFactory.createRotatingConeLight(32, Color.WHITE, 3f, dir0, cone, angVel);
            e.create();

            // mock game time and make getDeltaTime() return 1 for simple calculations
            var time = mock(GameTime.class);
            when(time.getDeltaTime()).thenReturn(1f); // 1s tick
            ServiceLocator.registerTimeSource(time);

            // update light
            e.getComponent(ConeLightComponent.class).update();

            // check that it updated properly
            ConeLight coneMock = coneCons.constructed().getFirst();
            verify(coneMock).setDirection(dir0 + angVel); // dir = dir0 + angVel * dt (1)
        }
    }

    @Test
    void createSecurityLight_switchesColorsOnEvents() {
        // mock both constructors again
        try (var rhCons = mockConstruction(RayHandler.class);
             var coneCons = mockConstruction(ConeLight.class)) {
            ServiceLocator.registerLightingService(createLightingService());
            // have to mock these as the detector component uses both
            ServiceLocator.registerPhysicsService(mock(PhysicsService.class));
            ServiceLocator.registerRenderService(mock(RenderService.class));
            ServiceLocator.registerSecurityCamRetrievalService(new SecurityCamRetrievalService());

            var target = new Entity();
            var e = LightFactory.createSecurityLight(target, PhysicsLayer.OBSTACLE, 64, Color.GREEN, 5f, 0f, 30f);
            e.create();

            ConeLight coneMock = coneCons.constructed().getFirst();

            // forcefully trigger target detection
            e.getEvents().trigger("targetDetected", target);
            verify(coneMock).setColor(Color.RED);
            // forcefully trigger target lost
            e.getEvents().trigger("targetLost", target);
            verify(coneMock).setColor(Color.GREEN);
        }
    }

    private LightingService createLightingService() {
        return new LightingService(
                new LightingEngine(new CameraComponent(), new World(new Vector2(0, 0), true)));
    }
}