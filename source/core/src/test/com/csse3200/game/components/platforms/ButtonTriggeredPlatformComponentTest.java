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
    void testStartsIdleAtStart() {
        assertEquals(getState(), "IDLE_AT_START");
    }

    @Test
    void testFirstActivationMovesToEnd() {
        platform.getEvents().trigger("activatePlatform");
        assertEquals(getState(), "MOVING_TO_END");
    }

    @Test
    void testPlatformMovesWhenEnabled() {
        platform.getEvents().trigger("activatePlatform");
        component.update();
        verify(mockBody, atLeastOnce()).setLinearVelocity(any(Vector2.class));
    }

    @Test
    void testArrivalAtEndStopsMovement() {
        platform.getEvents().trigger("activatePlatform");
        // Simulate being at target
        when(mockBody.getPosition()).thenReturn(new Vector2(0, 3f));
        component.update();
        assertEquals(getState(), "IDLE_AT_END");
    }

    @Test
    void testSecondActivationMovesToStart() {
        // First trip to end
        platform.getEvents().trigger("activatePlatform");
        when(mockBody.getPosition()).thenReturn(new Vector2(0, 3f));
        component.update();

        // Second trip back
        platform.getEvents().trigger("activatePlatform");
        assertEquals(getState(), "MOVING_TO_START");
    }

    @Test
    void testButtonIgnoredWhileMoving() {
        platform.getEvents().trigger("activatePlatform");
        String stateBefore = getState();
        platform.getEvents().trigger("activatePlatform"); // press again mid-trip
        assertEquals(stateBefore, getState()); // state unchanged
    }

    // Helper to read private 'state' enum as string
    private String getState() {
        try {
            var field = ButtonTriggeredPlatformComponent.class.getDeclaredField("state");
            field.setAccessible(true);
            Object stateValue = field.get(component);
            return stateValue.toString();
        } catch (Exception e) {
            fail("Could not access 'state' field: " + e.getMessage());
            return null;
        }
    }
}
