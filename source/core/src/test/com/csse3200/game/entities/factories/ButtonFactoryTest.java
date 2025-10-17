package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.ButtonComponent;
import com.csse3200.game.components.ButtonManagerComponent;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ButtonFactoryTest {
    private ResourceService mockResourceService;
    @BeforeEach
    void setupGameServices() {
        // Register PhysicsService to initialise a button's physics body during tests
        ServiceLocator.registerPhysicsService(new PhysicsService());

        // Mock ResourceService so Texture assets won't throw exceptions
        mockResourceService = mock(ResourceService.class);
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

    @Test
    void privateConstructor_throwsException() throws Exception {
        Constructor<ButtonFactory> constructor = ButtonFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException e = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(e.getCause() instanceof IllegalStateException,
                "Constructor should throw IllegalStateException");
    }

    @Test
    void createButton_setsCorrectTextureForDoorType() {
        ButtonFactory.createButton(true, "door", "left");
        verify(mockResourceService).getAsset("images/red_button_pushed.png", Texture.class);
    }

    @Test
    void createButton_setsRotationCorrectly_basedOnDirection() {
        float epsilon = 0.001f;

        Entity up = ButtonFactory.createButton(false, "standard", "up");
        assertEquals(270f, up.getComponent(TextureRenderComponent.class).getRotation(), epsilon);

        Entity down = ButtonFactory.createButton(false, "standard", "down");
        assertEquals(90f, down.getComponent(TextureRenderComponent.class).getRotation(), epsilon);

        Entity right = ButtonFactory.createButton(false, "standard", "right");
        assertEquals(180f, right.getComponent(TextureRenderComponent.class).getRotation(), epsilon);

        Entity left = ButtonFactory.createButton(false, "standard", "left");
        assertEquals(0f, left.getComponent(TextureRenderComponent.class).getRotation(), epsilon);

        Entity nullDirection = ButtonFactory.createButton(false, "standard", null);
        assertEquals(0f, nullDirection.getComponent(TextureRenderComponent.class).getRotation(), epsilon);
    }

    @Test
    void createButton_setsCorrectScale() {
        Entity button = ButtonFactory.createButton(false, "door", "left");
        Vector2 scale = button.getScale();

        assertEquals(0.5f, scale.x, 0.001f, "Button X scale should be 0.5");
        assertEquals(0.5f, scale.y, 0.001f, "Button Y scale should be 0.5");
    }

    @Test
    void createPuzzleButton_registersToManager() {
        ButtonManagerComponent manager = new ButtonManagerComponent();
        Entity button = ButtonFactory.createPuzzleButton(false, "door", "left", manager);
        ButtonComponent buttonComp = button.getComponent(ButtonComponent.class);

        assertTrue(manager.getButtons().contains(buttonComp),
                "Puzzle button should be added to manager's list");
    }

}