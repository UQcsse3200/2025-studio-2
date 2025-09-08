package com.csse3200.game.components.platforms;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovingPlatformComponentTest {

    private Entity platform;
    private MovingPlatformComponent component;
    private PhysicsComponent physics;
    private Body mockBody;

    @BeforeEach
    void setUp() {
        platform = spy(new Entity());

        mockBody = mock(Body.class);
        when(mockBody.getAngle()).thenReturn(0f);

        physics = mock(PhysicsComponent.class);
        when(physics.getBody()).thenReturn(mockBody);

        platform.addComponent(physics);

        // Use vertical or horizontal offset depending on what you want to test
        component = new MovingPlatformComponent(new Vector2(5f, 0f), 2f);
        platform.addComponent(component);
    }

    @Test
    void testInitialOffsetAndSpeed() {
        assertEquals(new Vector2(5f, 0f), component.offset);
        assertEquals(2f, component.speed);
    }

    @Test
    void testPlatformMovesTowardTarget() {
        // Simulate starting at origin
        when(mockBody.getPosition()).thenReturn(new Vector2(0f, 0f));

        component.create();
        component.update();

        verify(mockBody, atLeastOnce()).setLinearVelocity(any(Vector2.class));
    }

    @Test
    void testPlatformReversesWhenCloseToTarget() {
        // First call to getPosition() during create() sets origin
        when(mockBody.getPosition()).thenReturn(new Vector2(0f, 0f));
        component.create();

        // Now simulate being exactly at the target
        when(mockBody.getPosition()).thenReturn(new Vector2(5f, 0f));

        component.update();

        verify(mockBody).setTransform(any(Vector2.class), anyFloat());
        verify(mockBody).setLinearVelocity(argThat(v -> v.epsilonEquals(Vector2.Zero, 0.0001f)));
    }
}
