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
        platform = new Entity();

        // Mock Body and PhysicsComponent
        mockBody = mock(Body.class);
        when(mockBody.getPosition()).thenReturn(new Vector2(0, 0));

        physics = mock(PhysicsComponent.class);
        when(physics.getBody()).thenReturn(mockBody);

        platform.addComponent(physics);

        component = new MovingPlatformComponent(new Vector2(5f, 0f), 2f);
        platform.addComponent(component);
        platform.create();
    }

    @Test
    void testInitialOffsetAndSpeed() {
        assertEquals(new Vector2(5f, 0f), component.offset);
        assertEquals(2f, component.speed);
    }

    @Test
    void testPlatformMovesTowardTarget() {
        component.update();
        verify(mockBody, atLeastOnce()).setLinearVelocity(any(Vector2.class));
    }

    @Test
    void testPlatformReversesWhenCloseToTarget() {
        when(mockBody.getPosition()).thenReturn(new Vector2(4.96f, 0f));
        component.update();
        verify(mockBody).setTransform(any(Vector2.class), anyFloat());
        verify(mockBody).setLinearVelocity(argThat(v -> v.epsilonEquals(new Vector2(0,0), 0.0001f)));
    }
}
