package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TiledPlatformComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class PlatformFactoryTest {
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
    void createStaticPlatform_hasAllComponents() {
        Entity staticPlatform = PlatformFactory.createStaticPlatform();

        assertNotNull(staticPlatform.getComponent(TiledPlatformComponent.class),
                "Static platform should have a TextureRendererComponent");
        assertNotNull(staticPlatform.getComponent(PhysicsComponent.class),
                "Static platform should have a PhysicsComponent");
        assertNotNull(staticPlatform.getComponent(ColliderComponent.class),
                "Static platform should have a Collider");
    }

    @Test
    void createStaticPlatform_isStatic() {
        Entity staticPlatform = PlatformFactory.createStaticPlatform();

        PhysicsComponent physics = staticPlatform.getComponent(PhysicsComponent.class);
        assertEquals(BodyDef.BodyType.StaticBody, physics.getBody().getType(),
                "Static Platform PhysicsComponent should have a static body type");
    }
}
