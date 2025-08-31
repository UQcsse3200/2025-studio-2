package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GateFactoryTest {
    @BeforeEach
    void setupGameServices() {

        ServiceLocator.registerPhysicsService(new PhysicsService());
        ResourceService mockResourceService = mock(ResourceService.class);
        Texture mockTexture = mock(Texture.class);
        when(mockTexture.getHeight()).thenReturn(100);
        when(mockTexture.getWidth()).thenReturn(100);
        when(mockResourceService.getAsset(anyString(), any())).thenReturn(mockTexture);
        ServiceLocator.registerResourceService(mockResourceService);
    }

    @Test
    void createGate_hasAllComponents() {
        Entity gate = GateFactory.createGate();

        assertNotNull(gate.getComponent(TextureRenderComponent.class),
                "Gate should have a TextureRendererComponent");
        assertNotNull(gate.getComponent(PhysicsComponent.class),
                "Gate should have a PhysicsComponent");
        assertNotNull(gate.getComponent(ColliderComponent.class),
                "Gate should have a Collider");
    }

    @Test
    void createGate_isStatic() {
        Entity gate = GateFactory.createGate();

        PhysicsComponent physics = gate.getComponent(PhysicsComponent.class);
        assertEquals(BodyDef.BodyType.StaticBody, physics.getBody().getType(),
                "Gate PhysicsComponent should have a static body type");
    }
}
