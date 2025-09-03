package com.csse3200.game.components.platforms;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovingPlatformComponentTest {

    private Entity platform;
    private MovingPlatformComponent component;
    private PhysicsComponent physics;

    @BeforeEach
    void setUp() {
        platform = new Entity();
        physics = new PhysicsComponent();
        platform.addComponent(physics);

        component = new MovingPlatformComponent(new Vector2(5f, 0f), 2f);
        platform.addComponent(component);
        platform.create();
    }

    @Test
    void testInitialOffsetAndSpeed() {
        assertEquals(new Vector2(5f, 0f), component.offset, "Offset should match constructor");
        assertEquals(2f, component.speed, "Speed should match constructor");
    }

    @Test
    void testPlatformMovesTowardTarget() {
        Body body = physics.getBody();
        Vector2 initialPos = platform.getPosition().cpy();

        component.update();
        Vector2 velocity = body.getLinearVelocity();

        assertNotEquals(0f, velocity.len(), "Platform should be moving");
        assertTrue(velocity.x > 0, "Platform should move right toward offset");
    }

    @Test
    void testPlatformReversesWhenCloseToTarget() {
        Body body = physics.getBody();

        // Manually set position near end
        Vector2 nearEnd = platform.getPosition().cpy().add(new Vector2(4.95f, 0f));
        body.setTransform(nearEnd, body.getAngle());

        component.update();
        Vector2 velocity = body.getLinearVelocity();

        assertEquals(Vector2.Zero, velocity, "Platform should snap and stop at target");
    }
}
