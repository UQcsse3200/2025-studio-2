package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class BoxFactoryTest {

    @BeforeEach
    void setupGameServices() {
        // Register PhysicsService to initialise a box's physics body during tests
        ServiceLocator.registerPhysicsService(new PhysicsService());

        // Mock ResourceService so Pixmap and Texture assets won't throw exceptions
        ResourceService mockResourceService = mock(ResourceService.class);
        when(mockResourceService.getAsset(anyString(), any())).thenReturn(null);
        ServiceLocator.registerResourceService(mockResourceService);
    }

    @Test
    void createStaticBox_hasAllComponents() {
        Entity box = BoxFactory.createStaticBox();
        assertNotNull(box.getComponent(TextureRenderComponent.class),
                "Box should have a TextureRendererComponent");
        assertNotNull(box.getComponent(PhysicsComponent.class),
                "Box should have a PhysicsComponent");
        assertNotNull(box.getComponent(ColliderComponent.class),
                "Box should have a ColliderComponent");
    }

    @Test
    void createStaticBox_isStatic() {
        Entity box = BoxFactory.createStaticBox();

        PhysicsComponent physics = box.getComponent(PhysicsComponent.class);
        assertEquals(BodyDef.BodyType.StaticBody, physics.getBody().getType(),
                "Box PhysicsComponent should have a static BodyType");
    }
}
