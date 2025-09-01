package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.PhysicsComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
public class PlayerActionsTest {

    private Entity playerEntity;
    private PlayerActions playerActions;
    private PhysicsComponent physicsComponent;
    private Body mockBody;

    /**
     * setUp() was authored with assistance from ChatGPT-5
     * Date: 1/09/25
     */
    @BeforeEach
    void setUp() {
        physicsComponent = mock(PhysicsComponent.class);
        mockBody = mock(Body.class);
        when(physicsComponent.getBody()).thenReturn(mockBody);

        playerEntity = new Entity();
        playerEntity.addComponent(physicsComponent);

        playerActions = new PlayerActions();
        playerEntity.addComponent(playerActions);
        playerEntity.create();
    }

    @Test
    void testWalking() {
        Vector2 dir = new Vector2(1,0);
        playerEntity.getEvents().trigger("walk", dir);

        assertTrue(playerActions.isMoving());
        assertEquals(1.0, playerActions.getXDirection());
        assertEquals(0.0, playerActions.getYDirection());
    }
}
