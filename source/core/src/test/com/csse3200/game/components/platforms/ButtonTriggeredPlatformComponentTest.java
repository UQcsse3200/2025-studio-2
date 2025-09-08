package com.csse3200.game.components.platforms;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ButtonTriggeredPlatformComponentTest {

    private Entity platform;
    private ButtonTriggeredPlatformComponent component;
    private PhysicsComponent physics;
    private Body mockBody;

    @BeforeEach
    void setUp() {
        platform = new Entity();

        mockBody = mock(Body.class);
        when(mockBody.getPosition()).thenReturn(new Vector2(0, 0));

        physics = mock(PhysicsComponent.class);
        when(physics.getBody()).thenReturn(mockBody);

        platform.addComponent(physics);

        component = new ButtonTriggeredPlatformComponent(new Vector2(0f, 3f), 2f);
        platform.addComponent(component);
        platform.create();
    }

    @Test
    void testStartsDisabled() {
        assertFalse(componentIsActive());
    }

    @Test
    void testActivatePlatformEnablesMovement() {
        platform.getEvents().trigger("activatePlatform");
        assertTrue(componentIsActive());
    }

    @Test
    void testPlatformMovesWhenEnabled() {
        platform.getEvents().trigger("activatePlatform");
        component.update();
        verify(mockBody, atLeastOnce()).setLinearVelocity(any(Vector2.class));
    }

    // Helper to check active state
    private boolean componentIsActive() {
        try {
            var field = ButtonTriggeredPlatformComponent.class.getDeclaredField("active");
            field.setAccessible(true);
            return field.getBoolean(component);
        } catch (Exception e) {
            fail("Could not access 'active' field: " + e.getMessage());
            return false;
        }
    }
}
