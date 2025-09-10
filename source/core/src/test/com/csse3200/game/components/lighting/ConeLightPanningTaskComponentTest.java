package com.csse3200.game.components.lighting;

import box2dLight.ConeLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.SecurityCameraFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.lighting.LightingDefaults;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.lighting.SecurityCamRetrievalService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class ConeLightPanningTaskComponentTest {

    @BeforeEach
    public void before() {
        // set constant delta time to 1f for easy calculations
        GameTime time = mock(GameTime.class);
        when(time.getDeltaTime()).thenReturn(1f);
        ServiceLocator.registerTimeSource(time);

        ResourceService mockResourceService = mock(ResourceService.class);
        when(mockResourceService.getAsset(anyString(), any())).thenReturn(null);
        ServiceLocator.registerResourceService(mockResourceService);

        SecurityCamRetrievalService rs = new SecurityCamRetrievalService();
        ServiceLocator.registerSecurityCamRetrievalService(rs);

        ServiceLocator.registerEntityService(new EntityService());

        RenderService mockRenderService = mock(RenderService.class);
        ServiceLocator.registerRenderService(mockRenderService);

        PhysicsService mockPhysicsService = mock(PhysicsService.class);
        ServiceLocator.registerPhysicsService(mockPhysicsService);
    }

    private static MockedConstruction<ConeLight> statefulConeLight() {
        return mockConstruction(ConeLight.class, (mock, ctx) -> {
            final AtomicReference<Float> dir = new AtomicReference<>(0f);

            when(mock.getDirection()).then(inv -> dir.get());
            doAnswer(inv -> {
                float d = (Float) inv.getArgument(0);
                dir.set(d);
                return null;
            }).when(mock).setDirection(anyFloat());

            // no-ops for methods invoked during construction/update
            doNothing().when(mock).setActive(anyBoolean());
            doNothing().when(mock).setConeDegree(anyFloat());
            doNothing().when(mock).setPosition(anyFloat(), anyFloat());
            doNothing().when(mock).setColor(any(Color.class));
            doNothing().when(mock).setDistance(anyFloat());
        });
    }

    @Test
    void flipsDirectionAtBounds_andMovesBackInsideRange() {
        try (var rhCons = mockConstruction(RayHandler.class);
             var coneCons = statefulConeLight()) {
            ServiceLocator.registerLightingService(createLightingService());

            // create camera
            Entity target = new Entity();
            Entity cam = SecurityCameraFactory.createSecurityCamera(target, LightingDefaults.ANGULAR_VEL, "test");
            cam.create();

            Entity lens = cam.getComponent(ConeLightPanningTaskComponent.class).getCameraLens();
            ConeLightComponent cone = lens.getComponent(ConeLightComponent.class);

            // start at END -> should flip to clockwise
            cone.setDirectionDeg(LightingDefaults.END_DEG);
            float before = cone.getLight().getDirection();
            cam.update();
            float after = cone.getLight().getDirection();
            assertTrue(after < before);

            // jump to START -> should flip to anticlockwise
            cone.setDirectionDeg(LightingDefaults.START_DEG);
            before = cone.getLight().getDirection();
            cam.update();
            after = cone.getLight().getDirection();
            assertTrue(after > before);
        }
    }

    @Test
    void update_shouldPanAsExpected() {
        try (var rhCons = mockConstruction(RayHandler.class);
             var coneCons = statefulConeLight()) {
            ServiceLocator.registerLightingService(createLightingService());

            Entity target = new Entity();
            Entity cam = SecurityCameraFactory.createSecurityCamera(target, LightingDefaults.ANGULAR_VEL, "test");
            cam.create();

            Entity lens = cam.getComponent(ConeLightPanningTaskComponent.class).getCameraLens();

            ConeLightComponent cone = lens.getComponent(ConeLightComponent.class);
            cone.setDirectionDeg(LightingDefaults.START_DEG);
            float dir = cone.getLight().getDirection();

            // check dir is equal to start deg after creation
            assertEquals(LightingDefaults.START_DEG, dir);
            // progress time one update
            cam.update();
            // check that cone has moved according to vel (should be moving anti-clockwise to start)
            float newDir = dir + LightingDefaults.ANGULAR_VEL;
            dir = cone.getLight().getDirection();
            assertEquals(newDir, dir);

            // works going the clockwise also
            cone.setDirectionDeg(LightingDefaults.END_DEG);
            dir = cone.getLight().getDirection();
            cam.update();
            // make sure light didnt go over bounds
            newDir = dir - LightingDefaults.ANGULAR_VEL;
            dir = cone.getLight().getDirection();
            assertEquals(newDir, dir);
        }
    }

    @Test
    void dispose_unregistersLensChildEntity() {
        try (var rhCons = mockConstruction(RayHandler.class);
             var coneCons = statefulConeLight()) {
            ServiceLocator.registerLightingService(createLightingService());

            Entity target = new Entity();
            Entity cam = SecurityCameraFactory.createSecurityCamera(target, LightingDefaults.ANGULAR_VEL, "test");
            cam.create();

            int before = ServiceLocator.getEntityService().get_entities().size;
            cam.dispose();
            int after  = ServiceLocator.getEntityService().get_entities().size;

            assertTrue(after <= before - 1);
        }
    }

    private LightingService createLightingService() {
        return new LightingService(
                new LightingEngine(new CameraComponent(), new World(new Vector2(0, 0), true)));
    }

}