package com.csse3200.game.components.platforms;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ButtonTriggeredPlatformComponentTest {

    private Entity platform;
    private ButtonTriggeredPlatformComponent component;
    private PhysicsComponent physics;

    @BeforeEach
    void setUp() {
        platform = new Entity();
        physics = new PhysicsComponent();
        platform.addComponent(physics);

        component = new ButtonTriggeredPlatformComponent(new Vector2(0f, 3f), 2f);
        platform.addComponent(component);
        platform.create();
    }

    @Test
    void testPlatformStartsDisabled() {
        platform.getEvents().trigger("activatePlatform"); // toggle once
        platform.getEvents().trigger("activatePlatform"); // toggle back
        platform.getEvents().trigger("activatePlatform"); // toggle on

        assertTrue(getEnabledState(), "Platform should be enabled after odd number of toggles");
    }

    @Test
    void testPlatformMovesWhenEnabled() {
        platform.getEvents().trigger("activatePlatform"); // enable
        component.update();

        Body body = physics.getBody();
        Vector2 velocity = body.getLinearVelocity();

        assertNotEquals(0f, velocity.len(), "Platform should be moving when enabled");
    }

    @Test
    void testPlatformDoesNotMoveWhenDisabled() {
        component.update();

        Body body = physics.getBody();
        Vector2 velocity = body.getLinearVelocity();

        assertEquals(0f, velocity.len(), "Platform should not move when disabled");
    }

    @Test
    void testPlatformReversesDirection() {
        platform.getEvents().trigger("activatePlatform"); // enable

        // Move platform near target
        Body body = physics.getBody();
        Vector2 nearTarget = body.getPosition().cpy().add(new Vector2(0f, 2.95f));
        body.setTransform(nearTarget, body.getAngle());

        component.update();

        Vector2 velocity = body.getLinearVelocity();
        assertEquals(Vector2.Zero, velocity, "Platform should snap and stop at target");
    }

    // Helper method to access private 'enabled' field via reflection
    private boolean getEnabledState() {
        try {
            var field = ButtonTriggeredPlatformComponent.class.getDeclaredField("enabled");
            field.setAccessible(true);
            return field.getBoolean(component);
        } catch (Exception e) {
            fail("Could not access 'enabled' field: " + e.getMessage());
            return false;
        }
    }
}
