package com.csse3200.game.components.lighting;

import box2dLight.ConeLight;
import box2dLight.RayHandler;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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

        EntityService mockEntityService = mock(EntityService.class);
        ServiceLocator.registerEntityService(mockEntityService);

        RenderService mockRenderService = mock(RenderService.class);
        ServiceLocator.registerRenderService(mockRenderService);

        PhysicsService mockPhysicsService = mock(PhysicsService.class);
        ServiceLocator.registerPhysicsService(mockPhysicsService);
    }

    @Disabled // working on it...
    @Test
    void update_shouldPanAsExpected() {
        try (var rhCons = mockConstruction(RayHandler.class);
             var coneCons = mockConstruction(ConeLight.class)) {
            ServiceLocator.registerLightingService(createLightingService());

            Entity target = new Entity();
            Entity cam = SecurityCameraFactory.createSecurityCamera(target, LightingDefaults.ANGULAR_VEL, "test");
            cam.create();

            Entity lens = cam.getComponent(ConeLightPanningTaskComponent.class).getCameraLens();
            lens.create();

            ConeLightComponent coneComp = lens.getComponent(ConeLightComponent.class);
            float dir = coneComp.getLight().getDirection();
            // check dir is equal to start deg after creation
            assertEquals(LightingDefaults.START_DEG, dir);
            // progress time one update
            cam.update();
            // check that cone has moved according to vel (should be moving anti-clockwise to start)
            float newDir = dir - LightingDefaults.ANGULAR_VEL;
            assertEquals(newDir, coneComp.getLight().getDirection());
        }
    }

    private LightingService createLightingService() {
        return new LightingService(
                new LightingEngine(new CameraComponent(), new World(new Vector2(0, 0), true)));
    }

}