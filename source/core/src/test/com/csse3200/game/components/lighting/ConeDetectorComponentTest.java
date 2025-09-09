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
import com.csse3200.game.lighting.SecurityCamRetrievalService;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class ConeDetectorComponentTest {
    private PhysicsEngine physics;

    @BeforeEach
    void setUp() {
        physics = mock(PhysicsEngine.class);
        ServiceLocator.registerPhysicsService(new PhysicsService(physics));

        RenderService renderService = new RenderService();
        DebugRenderer debug = mock(DebugRenderer.class);
        renderService.setDebug(debug);
        ServiceLocator.registerRenderService(renderService);
        ServiceLocator.registerSecurityCamRetrievalService(new SecurityCamRetrievalService());
    }

    @Test
    void shouldRequireConeLightComponentOnSameEntity() {
        Entity e = new Entity();
        Entity target = new Entity();
        ConeDetectorComponent detector = new ConeDetectorComponent(target, "none");

        e.addComponent(detector);
        assertThrows(IllegalStateException.class, e::create);
    }

    @Test
    void detectsTargetWhenWithinConeAndUnblocked() {
        try (var rhCons = mockConstruction(RayHandler.class);
             var coneCons = mockConstruction(ConeLight.class)) {
            ServiceLocator.registerLightingService(createLightingService());
            RayHandler rh = ServiceLocator.getLightingService().getEngine().getRayHandler();

            // light at (0,0) centre at (0.5, 0.5). Target centre at (1.5, 0.5) -> angle 0 deg, distance 1.
            Entity lightEntity = new Entity();
            lightEntity.setPosition(new Vector2(0f, 0f));

            Entity target = new Entity();
            target.setPosition(new Vector2(1f, 0f));

            float dis = 5f, dir = 0f, coneDeg = 45f;
            ConeLightComponent cl = new ConeLightComponent(rh,32, Color.WHITE, dis, dir, coneDeg);
            ConeDetectorComponent detector = new ConeDetectorComponent(target, "none");
            lightEntity.addComponent(cl);
            lightEntity.addComponent(detector);

            // Unblocked LOS
            when(physics.raycast(any(Vector2.class), any(Vector2.class), eq(PhysicsLayer.OBSTACLE), any())).thenReturn(false);

            final boolean[] detectedEvent = {false};
            final boolean[] lostEvent = {false};

            lightEntity.getEvents().addListener("targetDetected", (Entity e) -> detectedEvent[0] = true);
            lightEntity.getEvents().addListener("targetLost", (Entity e) -> lostEvent[0] = true);

            lightEntity.create();

            ConeLight lightMock = coneCons.constructed().getFirst();
            when(lightMock.getDirection()).thenReturn(dir);
            when(lightMock.getDistance()).thenReturn(dis);
            when(lightMock.getConeDegree()).thenReturn(coneDeg);

            // initial update causes detection to flip from false -> true
            detector.update();

            assertTrue(detector.isDetected());
            assertTrue(detectedEvent[0]);
            assertFalse(lostEvent[0]);
        }
    }

    @Test
    void doesNotDetectOutsideAngleOrDistance() {
        try (var rhCons = mockConstruction(RayHandler.class);
             var coneCons = mockConstruction(ConeLight.class)) {
            ServiceLocator.registerLightingService(createLightingService());
            RayHandler rh = ServiceLocator.getLightingService().getEngine().getRayHandler();

            Entity lightEntity = new Entity();
            lightEntity.setPosition(new Vector2(0f, 0f));

            Entity target = new Entity();
            target.setPosition(new Vector2(1f, 1f)); // ~45 degrees from lightEntity pos

            ConeLightComponent cl = new ConeLightComponent(rh, 32, Color.WHITE, 5f, 0f, 45f);
            lightEntity.addComponent(cl);
            ConeDetectorComponent detector = new ConeDetectorComponent(target, "none");
            lightEntity.addComponent(detector);

            when(physics.raycast(any(Vector2.class), any(Vector2.class), eq(PhysicsLayer.OBSTACLE), any())).thenReturn(false);

            lightEntity.create();

            ConeLight lightMock = coneCons.constructed().getFirst();

            // Too short distance
            // pointing in the right direction but the distance is too short to detect
            when(lightMock.getDirection()).thenReturn(0f);
            when(lightMock.getDistance()).thenReturn(0.25f);
            when(lightMock.getConeDegree()).thenReturn(45f);

            detector.update();
            assertFalse(detector.isDetected());

            // Large distance, but narrow cone excluding ~45deg
            when(lightMock.getDistance()).thenReturn(10f);
            when(lightMock.getConeDegree()).thenReturn(10f);

            detector.update();
            assertFalse(detector.isDetected());
        }
    }

    @Test
    void noLightMeansNoDetection() throws Exception{
        try (var rhCons = mockConstruction(RayHandler.class);
             var coneCons = mockConstruction(ConeLight.class)) {
            ServiceLocator.registerLightingService(createLightingService());
            RayHandler rh = ServiceLocator.getLightingService().getEngine().getRayHandler();

            Entity lightEntity = new Entity();
            Entity target = new Entity();

            ConeLightComponent cl = new ConeLightComponent(rh, 32, Color.WHITE, 5f, 0f, 45f);
            ConeDetectorComponent detector = new ConeDetectorComponent(target, "none");

            lightEntity.addComponent(cl);
            lightEntity.addComponent(detector);
            lightEntity.create();

            // force the component into a "no light" state
            var f = ConeLightComponent.class.getDeclaredField("coneLight");
            f.setAccessible(true);
            f.set(cl, null);

            detector.update();
            assertFalse(detector.isDetected());
        }
    }

    private LightingService createLightingService() {
        return new LightingService(
                new LightingEngine(new CameraComponent(), new World(new Vector2(0, 0), true)));
    }
}