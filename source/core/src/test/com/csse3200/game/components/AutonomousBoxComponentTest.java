package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.PhysicsComponent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class AutonomousBoxComponentTest {

    private AutonomousBoxComponent component;
    private PhysicsComponent mockPhysics;
    private Body mockBody;

    @BeforeEach
    void setup() {

        component = new AutonomousBoxComponent();
        mockPhysics = mock(PhysicsComponent.class);
        mockBody = mock(Body.class);

        when(mockPhysics.getBody()).thenReturn(mockBody);
        component.entity = mock(Entity.class);
        when(component.entity.getComponent(PhysicsComponent.class)).thenReturn(mockPhysics);

        component.create();
    }

    @Test
    void testPhysics() {
        assertNotNull(component.getPhysics());
        assertEquals(mockPhysics, component.getPhysics());
    }

    @Test
    void testBoundsSettersAndGetters() {
        component.setBounds(1f, 5f, 2f, 6f);
        assertEquals(1f, component.getLeftX());
        assertEquals(5f, component.getRightX());
        assertEquals(2f, component.getBottomY());
        assertEquals(6f, component.getTopY());
    }

    @Test
    void testSpeedSettersAndGetters() {
        component.setSpeed(3f);
        assertEquals(3f, component.getSpeed());
    }

    @Test
    void testUpdateHorizontalMovement() {
        component.setBounds(0f, 2f, 0f, 0f);
        component.setSpeed(1f);

        when(mockBody.getPosition()).thenReturn(new com.badlogic.gdx.math.Vector2(0f, 0f));
        component.update();

        verify(mockBody, atLeastOnce()).setTransform(anyFloat(), anyFloat(), eq(0f));
    }

    @Test
    void testUpdateVerticalMovement() {
        component.setBounds(0f, 0f, 0f, 2f);
        component.setSpeed(1f);

        when(mockBody.getPosition()).thenReturn(new com.badlogic.gdx.math.Vector2(0f, 0f));
        component.update();

        verify(mockBody, atLeastOnce()).setTransform(anyFloat(), anyFloat(), eq(0f));
    }

    @Test
    void testUpdateDiagonalMovement() {
        component.setBounds(0f, 2f, 0f, 2f);
        component.setSpeed(1f);

        when(mockBody.getPosition()).thenReturn(new com.badlogic.gdx.math.Vector2(0f, 0f));
        component.update();

        verify(mockBody, atLeastOnce()).setTransform(anyFloat(), anyFloat(), eq(0f));
    }

    @Test
    void testUpdateNoPhysics() {
        AutonomousBoxComponent component = new AutonomousBoxComponent();
        component.update();  // should do nothing.
    }

    @Test
    void testHorizontalDirectionFlip() {
        component.setBounds(0f, 1f, 0f, 0f);
        component.setSpeed(10f);

        // Simulate being at the right boundary
        when(mockBody.getPosition()).thenReturn(new com.badlogic.gdx.math.Vector2(1f, 0f));
        component.update();
        // After update, directionX should flip to -1
        assertEquals(-1, component.getDirectionX());

        // Simulate being at the left boundary
        when(mockBody.getPosition()).thenReturn(new com.badlogic.gdx.math.Vector2(0f, 0f));
        component.update();
        assertEquals(1, component.getDirectionX());
    }

    @Test
    void testVerticalDirectionFlip() {
        component.setBounds(0f, 0f, 0f, 1f);
        component.setSpeed(10f);

        // At top boundary
        when(mockBody.getPosition()).thenReturn(new com.badlogic.gdx.math.Vector2(0f, 1f));
        component.update();
        assertEquals(-1, component.getDirectionY());

        // At bottom boundary
        when(mockBody.getPosition()).thenReturn(new com.badlogic.gdx.math.Vector2(0f, 0f));
        component.update();
        assertEquals(1, component.getDirectionY());
    }

    @Test
    void testDiagonalDirectionFlip() {
        component.setBounds(0f, 1f, 0f, 1f);
        component.setSpeed(10f);

        // Top-right corner
        when(mockBody.getPosition()).thenReturn(new com.badlogic.gdx.math.Vector2(1f, 1f));
        component.update();
        assertEquals(-1, component.getDirectionX());
        assertEquals(-1, component.getDirectionY());

        // Bottom-left corner
        when(mockBody.getPosition()).thenReturn(new com.badlogic.gdx.math.Vector2(0f, 0f));
        component.update();
        assertEquals(1, component.getDirectionX());
        assertEquals(1, component.getDirectionY());
    }


}
