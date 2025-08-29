package com.csse3200.game.components.lighting;

import box2dLight.ConeLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class ConeLightComponentTest {

    @Test
    void shouldMoveEntityWithVelocityAndDeltaTime() {
        try (var rhCons = mockConstruction(RayHandler.class);
             var coneCons = mockConstruction(ConeLight.class)) {
            ServiceLocator.registerLightingService(createLightingService());
            RayHandler rh = ServiceLocator.getLightingService().getEngine().getRayHandler();

            // Deterministic dt = 0.5s
            GameTime time = mock(GameTime.class);
            when(time.getDeltaTime()).thenReturn(0.5f);
            ServiceLocator.registerTimeSource(time);

            Entity e = new Entity();
            e.setPosition(new Vector2(0, 0));

            ConeLightComponent comp = new ConeLightComponent(
                    rh, 32, Color.WHITE, 1f, 0f, 30f
            ).setVelocity(new Vector2(2f, -4f)); // units/sec

            e.addComponent(comp);
            e.create();

            comp.update();
            assertEquals(new Vector2(1f, -2f), e.getPosition());
        }
    }

    @Test
    void setVelocityZeroShouldStopMovement() {
        try (var rhCons = mockConstruction(RayHandler.class);
             var coneCons = mockConstruction(ConeLight.class)) {
            ServiceLocator.registerLightingService(createLightingService());
            RayHandler rh = ServiceLocator.getLightingService().getEngine().getRayHandler();

            GameTime time = mock(GameTime.class);
            when(time.getDeltaTime()).thenReturn(1f);
            ServiceLocator.registerTimeSource(time);

            Entity e = new Entity();
            e.setPosition(new Vector2(0, 0));

            ConeLightComponent comp = new ConeLightComponent(
                    rh, 32, Color.WHITE, 1f, 0f, 30f
            ).setVelocity(new Vector2(3f, 0f));

            e.addComponent(comp);
            e.create();

            // move once
            comp.update();
            assertEquals(new Vector2(3f, 0f), e.getPosition());

            // move again
            comp.update();
            assertEquals(new Vector2(6f, 0f), e.getPosition());

            // stop
            comp.setVelocityZero();
            comp.update();
            assertEquals(new Vector2(6f, 0f), e.getPosition());
        }
    }

    @Test
    void disposeWithoutLightShouldNotThrow() throws Exception {
        try (var rhCons = mockConstruction(RayHandler.class);
             var coneCons = mockConstruction(ConeLight.class)) {
            ServiceLocator.registerLightingService(createLightingService());
            RayHandler rh = ServiceLocator.getLightingService().getEngine().getRayHandler();

            Entity e = new Entity();
            ConeLightComponent comp = new ConeLightComponent(
                    rh, 32, Color.WHITE, 1f, 0f, 30f);
            e.addComponent(comp);
            e.create();

            // force "no light" state
            var f = ConeLightComponent.class.getDeclaredField("coneLight");
            f.setAccessible(true);
            f.set(comp, null);

            assertDoesNotThrow(comp::dispose);
        }
    }

    @Test
    void disposeWithLightShouldCallDisposeOnConeLight() {
        try (var rhCons = mockConstruction(RayHandler.class);
             var coneCons = mockConstruction(ConeLight.class)) {
            ServiceLocator.registerLightingService(createLightingService());
            RayHandler rh = ServiceLocator.getLightingService().getEngine().getRayHandler();

            Entity e = new Entity();
            ConeLightComponent comp = new ConeLightComponent(
                    rh, 32, Color.WHITE, 1f, 0f, 30f);
            e.addComponent(comp);
            e.create();

            ConeLight coneMock = coneCons.constructed().getFirst();
            assertDoesNotThrow(comp::dispose);
            verify(coneMock, atLeastOnce()).dispose();
        }
    }

    private LightingService createLightingService() {
        return new LightingService(
                new LightingEngine(new CameraComponent(), new World(new Vector2(0, 0), true)));
    }

}