package com.csse3200.game.entities.factories;

import box2dLight.RayHandler;
import com.csse3200.game.components.lasers.LaserDetectorComponent;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
class LaserDetectorFactoryTest {

    @BeforeEach
    void setUp() {
        // Register PhysicsService to initialise a box's physics body during tests
        ServiceLocator.registerPhysicsService(new PhysicsService());

        // Mock ResourceService so assets won't throw exceptions
        ResourceService mockResourceService = mock(ResourceService.class);
        when(mockResourceService.getAsset(anyString(), any())).thenReturn(null);
        ServiceLocator.registerResourceService(mockResourceService);

        RenderService renderService = new RenderService();
        ServiceLocator.registerRenderService(renderService);

        RayHandler rayHandler = mock(RayHandler.class);
        LightingEngine lightingEngine = mock(LightingEngine.class);
        when(lightingEngine.getRayHandler()).thenReturn(rayHandler);
        ServiceLocator.registerLightingService(new LightingService(lightingEngine));
    }

    @Test
    void createLaserDetector_hasAllComponents() {
        Entity e = LaserDetectorFactory.createLaserDetector();
        assertNotNull(e);
        assertNotNull(e.getComponent(TextureRenderComponent.class));
        assertNotNull(e.getComponent(PhysicsComponent.class));
        assertNotNull(e.getComponent(ColliderComponent.class));
        assertNotNull(e.getComponent(ConeLightComponent.class));
        assertNotNull(e.getComponent(LaserDetectorComponent.class));
    }

    @Test
    void createLaserDetector_spawnsChildEntity() throws Exception {
        Entity e = LaserDetectorFactory.createLaserDetector();

        LaserDetectorComponent detector = e.getComponent(LaserDetectorComponent.class);
        Field f = LaserDetectorComponent.class.getDeclaredField("child");
        f.setAccessible(true);

        Entity child = (Entity) f.get(detector);
        assertNotNull(child);
    }
}