package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.collectables.CollectableComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
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

public class CollectableFactoryTest {
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
    public void testCreateCollectable() {
        Entity key = CollectableFactory.createKey("pink-key");
        assertNotNull(key.getComponent(TextureRenderComponent.class),
                "Key should have a TextureRendererComponent");
        assertNotNull(key.getComponent(PhysicsComponent.class),
                "Key should have a PhysicsComponent");
        assertNotNull(key.getComponent(ColliderComponent.class),
                "Key should have a Collider");
        assertNotNull(key.getComponent(ColliderComponent.class),
                "Key should have a Collider");
    }

    @Test
    public void testCreateCollectable_hasCorrectPhysicsBody() {
        Entity key = CollectableFactory.createKey("pink-key");
        PhysicsComponent physics = key.getComponent(PhysicsComponent.class);
        ColliderComponent collider = key.getComponent(ColliderComponent.class);

        assertEquals(BodyDef.BodyType.StaticBody, physics.getBody().getType(),
                "Key PhysicsBody should be static");
        assertEquals(PhysicsLayer.OBSTACLE, collider.getLayer(),
                "Button ColliderComponent should be in OBSTACLE layer");
    }
}
