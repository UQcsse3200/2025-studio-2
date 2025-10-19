package com.csse3200.game.entities.factories;

import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.LaserRenderComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LaserFactoryTest {
    @BeforeEach
    void setUp() {
        // Register PhysicsService to initialise a box's physics body during tests
        ServiceLocator.registerPhysicsService(new PhysicsService());

        // Mock ResourceService so assets won't throw exceptions
        ResourceService mockResourceService = mock(ResourceService.class);
        when(mockResourceService.getAsset(anyString(), any())).thenReturn(new TextureAtlas());
        ServiceLocator.registerResourceService(mockResourceService);

        // mock time source
        GameTime time = mock(GameTime.class);
        ServiceLocator.registerTimeSource(time);

        RenderService renderService = new RenderService();
        ServiceLocator.registerRenderService(renderService);

        RayHandler rayHandler = mock(RayHandler.class);
        LightingEngine lightingEngine = mock(LightingEngine.class);
        when(lightingEngine.getRayHandler()).thenReturn(rayHandler);
        ServiceLocator.registerLightingService(new LightingService(lightingEngine));
    }

    @Test
    void createLaserEmitter_hasAllComponents() {
        Entity e = LaserFactory.createLaserEmitter(0f);
        assertNotNull(e);
        assertNotNull(e.getComponent(AnimationRenderComponent.class));
        assertNotNull(e.getComponent(ConeLightComponent.class));
        assertNotNull(e.getComponent(CombatStatsComponent.class));
        //assertNotNull(e.getComponent(LaserShowerComponent.class));
        assertNotNull(e.getComponent(LaserRenderComponent.class));
    }
}