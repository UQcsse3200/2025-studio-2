package com.csse3200.game.components;

import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WallComponentTest {

    private Entity wall;
    private PhysicsComponent physics;
    private Body mockBody;

    @BeforeEach
    void setUp() {
        wall = new Entity();

        mockBody = mock(Body.class);
        physics = mock(PhysicsComponent.class);
        when(physics.getBody()).thenReturn(mockBody);

        wall.addComponent(physics);
        wall.addComponent(new WallComponent());
        wall.create();
    }

    @Test
    void testWallComponentIsAttached() {
        assertNotNull(wall.getComponent(WallComponent.class));
    }

    @Test
    void testWallHasPhysicsComponent() {
        assertNotNull(wall.getComponent(PhysicsComponent.class));
    }

    @Test
    void testWallIgnoresEvents() {
        assertDoesNotThrow(() -> wall.getEvents().trigger("activatePlatform"));
        assertDoesNotThrow(() -> wall.getEvents().trigger("deactivatePlatform"));
    }
}
