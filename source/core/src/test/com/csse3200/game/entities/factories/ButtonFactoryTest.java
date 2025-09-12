package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.ButtonComponent;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ButtonFactoryTest {
    @BeforeEach
    void setupGameServices() {
        // Register PhysicsService to initialise a button's physics body during tests
        ServiceLocator.registerPhysicsService(new PhysicsService());

        // Mock ResourceService so Texture assets won't throw exceptions
        ResourceService mockResourceService = mock(ResourceService.class);
        when(mockResourceService.getAsset(anyString(), any())).thenReturn(null);
        ServiceLocator.registerResourceService(mockResourceService);
    }

    @Test
    void createPlatformButton_hasAllComponents() {
        Entity button = ButtonFactory.createButton(false, "platform", "left");

        assertNotNull(button.getComponent(TextureRenderComponent.class),
                "Platform button should have a TextureRenderComponent");
        assertNotNull(button.getComponent(PhysicsComponent.class),
                "Platform button should have a PhysicsComponent");
        assertNotNull(button.getComponent(ColliderComponent.class),
                "Platform button should have a ColliderComponent");
        assertNotNull(button.getComponent(ButtonComponent.class),
                "Platform button should have a ButtonComponent");
    }

    @Test
    void createDoorButton_hasAllComponents() {
        Entity button = ButtonFactory.createButton(true, "door", "left");

        assertNotNull(button.getComponent(TextureRenderComponent.class),
                "Door button should have a TextureRenderComponent");
        assertNotNull(button.getComponent(PhysicsComponent.class),
                "Door button should have a PhysicsComponent");
        assertNotNull(button.getComponent(ColliderComponent.class),
                "Door button should have a ColliderComponent");
        assertNotNull(button.getComponent(ButtonComponent.class),
                "Door button should have a ButtonComponent");
    }

    @Test
    void createStandardButton_hasAllComponents() {
        Entity button = ButtonFactory.createButton(false, "standard", "left");

        assertNotNull(button.getComponent(TextureRenderComponent.class),
                "Standard button should have a TextureRenderComponent");
        assertNotNull(button.getComponent(PhysicsComponent.class),
                "Standard button should have a PhysicsComponent");
        assertNotNull(button.getComponent(ColliderComponent.class),
                "Standard button should have a ColliderComponent");
        assertNotNull(button.getComponent(ButtonComponent.class),
                "Standard button should have a ButtonComponent");
    }

    @Test
    void createButton_hasStaticPhysicsBody() {
        Entity button = ButtonFactory.createButton(false, "platform", "left");

        PhysicsComponent physics = button.getComponent(PhysicsComponent.class);
        assertEquals(BodyDef.BodyType.StaticBody, physics.getBody().getType(),
                "Button PhysicsComponent should be static");
    }

    @Test
    void createButton_colliderIsObstacle() {
        Entity button = ButtonFactory.createButton(false, "door", "left");

        ColliderComponent collider = button.getComponent(ColliderComponent.class);
        assertEquals(PhysicsLayer.OBSTACLE, collider.getLayer(),
                "Button ColliderComponent should be in OBSTACLE layer");
    }
}