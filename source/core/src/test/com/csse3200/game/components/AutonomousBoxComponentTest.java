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
        component.setBounds(0f, 10f, 2f, 2f);
        component.setSpeed(2f);
    }

    @Test
    void create_assignsPhysics() {
        assertNotNull(component.getPhysics());
    }

    @Test
    void update_movesBoxRightAndLeft() {
        // Arrange: starting position
        when(mockBody.getPosition()).thenReturn(new Vector2(1.0f, 5.0f));

        // Mock deltaTime to be fixed (~60 FPS)
        Gdx.graphics = mock(Gdx.graphics.getClass());
        when(Gdx.graphics.getDeltaTime()).thenReturn(0.016f);

        // Act
        component.update();

        // Expected X after one frame: 1.0 + (2.0 * 0.016) = 1.032
        float expectedX = 1.0f + (2.0f * 0.016f);

        // Assert with tolerance for floating-point drift
        verify(mockBody).setTransform(
                floatThat(val -> Math.abs(val - expectedX) < 0.001f),
                eq(5.0f),
                eq(0.0f)
        );
    }

}
