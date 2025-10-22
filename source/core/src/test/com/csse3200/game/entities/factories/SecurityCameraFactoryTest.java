package com.csse3200.game.entities.factories;

import box2dLight.ConeLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.lighting.ConeDetectorComponent;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.lighting.ConeLightPanningTaskComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.lighting.LightingDefaults;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.lighting.SecurityCamRetrievalService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class SecurityCameraFactoryTest {

    @BeforeEach
    public void before() {
        ResourceService mockResourceService = mock(ResourceService.class);
        when(mockResourceService.getAsset(anyString(), any())).thenReturn(null);
        ServiceLocator.registerResourceService(mockResourceService);

        SecurityCamRetrievalService rs = new SecurityCamRetrievalService();
        ServiceLocator.registerSecurityCamRetrievalService(rs);

        EntityService mockEntityService = mock(EntityService.class);
        ServiceLocator.registerEntityService(mockEntityService);

        RenderService mockRenderService = mock(RenderService.class);
        ServiceLocator.registerRenderService(mockRenderService);

        PhysicsService mockPhysicsService = mock(PhysicsService.class);
        ServiceLocator.registerPhysicsService(mockPhysicsService);
    }

    @Test
    void createSecurityCamera_buildsAndStoresCorrectArgs() {
        List<List<?>> capturedArgs = new ArrayList<>();

        try (MockedConstruction<RayHandler> rhCons = mockConstruction(RayHandler.class);
             MockedConstruction<ConeLight> coneCons = mockConstruction(
                     ConeLight.class,
                     (mock, ctx) -> {
                         capturedArgs.add(ctx.arguments());
                         when(mock.getColor()).thenReturn(     (Color) ctx.arguments().get(2));
                         when(mock.getDistance()).thenReturn(  (Float) ctx.arguments().get(3));
                         when(mock.getDirection()).thenReturn( (Float) ctx.arguments().get(6));
                         when(mock.getConeDegree()).thenReturn((Float) ctx.arguments().get(7));
                     })) {
            ServiceLocator.registerLightingService(createLightingService());

            Entity target = new Entity();

            Entity e = SecurityCameraFactory.createSecurityCamera(target, LightingDefaults.ANGULAR_VEL, "test");
            // force creation of parent and child entity
            e.create();
            Entity lens = e.getComponent(ConeLightPanningTaskComponent.class).getCameraLens();
            lens.create();

            // ensure that both entities have all components
            assertNotNull(e.getComponent(ConeLightPanningTaskComponent.class));
            assertNotNull(e.getComponent(TextureRenderComponent.class));
            assertNotNull(lens.getComponent(TextureRenderComponent.class));
            assertNotNull(lens.getComponent(ConeLightComponent.class));
            assertNotNull(lens.getComponent(ConeDetectorComponent.class));

            // check that only 1 light cone has been made
            assertEquals(1, coneCons.constructed().size());
            List<?> args = capturedArgs.getFirst();
            // ensure that cone light args match that of the defaults
            // ConeLight(rh, rays, color, distance, x, y, directionDeg, coneDeg)
            assertEquals(LightingDefaults.RAYS,              args.get(1));
            assertEquals(LightingDefaults.NORMAL_COLOR,      args.get(2));
            assertEquals(LightingDefaults.DIST,      (Float) args.get(3), 1e-4);
            assertEquals(LightingDefaults.START_DEG, (Float) args.get(6), 1e-4);
            assertEquals(LightingDefaults.CONE_DEG,  (Float) args.get(7), 1e-4);

            // check that the light stored is the same that was constructed
            ConeLight stored = lens.getComponent(ConeLightComponent.class).getLight();
            assertEquals(coneCons.constructed().getFirst(), stored);
        }
    }

    @Test
    void createSecurityCamera_changesColourWhenDetected() {
        try (var rhCons = mockConstruction(RayHandler.class);
             var coneCons = mockConstruction(ConeLight.class)) {
            ServiceLocator.registerLightingService(createLightingService());

            Entity target = new Entity();
            Entity e = SecurityCameraFactory.createSecurityCamera(target, LightingDefaults.ANGULAR_VEL, "test");
            e.create();
            Entity lens = e.getComponent(ConeLightPanningTaskComponent.class).getCameraLens();
            lens.create();

            ConeLight coneMock = coneCons.constructed().getFirst();
            // force detection
            lens.getEvents().trigger("targetDetected", target);
            verify(coneMock).setColor(LightingDefaults.DETECTED_COLOR);
            // forcefully trigger target lost
            lens.getEvents().trigger("targetLost", target);
            verify(coneMock).setColor(LightingDefaults.NORMAL_COLOR);
        }
    }

    private LightingService createLightingService() {
        return new LightingService(
                new LightingEngine(new CameraComponent(), new World(new Vector2(0, 0), true)));
    }

}