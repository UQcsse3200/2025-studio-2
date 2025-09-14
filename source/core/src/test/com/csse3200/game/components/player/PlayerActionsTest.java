package com.csse3200.game.components.player;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.components.CrouchingColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.StandingColliderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        when(mockBody.getLinearVelocity()).thenReturn(new Vector2(0, 0));
        when(mockBody.getMass()).thenReturn(1f);

        ServiceLocator serviceLocator = mock(ServiceLocator.class);
        ServiceLocator.clear();

        ResourceService mockResourceService = mock(ResourceService.class);
        ServiceLocator.registerResourceService(mockResourceService);
        Sound mockSound = mock(Sound.class);
        when(mockResourceService.getAsset(anyString(), eq(Sound.class))).thenReturn(mockSound);


        KeyboardPlayerInputComponent mockInput = mock(KeyboardPlayerInputComponent.class);
        when(mockInput.getIsCheatsOn()).thenReturn(false);

        Fixture standFixture = mock(Fixture.class);
        Fixture crouchFixture = mock(Fixture.class);

        StandingColliderComponent standing = mock(StandingColliderComponent.class);
        when(standing.getFixtureRef()).thenReturn(standFixture);
        CrouchingColliderComponent crouch = mock(CrouchingColliderComponent.class);
        when(crouch.getFixtureRef()).thenReturn(crouchFixture);

        playerEntity = new Entity();
        playerEntity.addComponent(physicsComponent);
        playerEntity.addComponent(standing);
        playerEntity.addComponent(crouch);
        playerEntity.addComponent(mockInput);

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

    @Test
    void testStopWalking() {
        Vector2 dir = new Vector2(1,0);
        playerEntity.getEvents().trigger("walk", dir);
        playerEntity.getEvents().trigger("walkStop");

        assertFalse(playerActions.isMoving());
        assertEquals(0f, playerActions.getXDirection());
        assertEquals(0f, playerActions.getYDirection());
    }

    @Test
    void testJump() {

        playerEntity.getEvents().trigger("jump");

        assertTrue(playerActions.getIsJumping());
        assertFalse(playerActions.getIsDoubleJumping());

        playerEntity.getEvents().trigger("jump");
        assertTrue(playerActions.getIsJumping());
        assertTrue(playerActions.getIsDoubleJumping());
    }

    @Test
    void testOnLandResetsJump() {
        playerEntity.getEvents().trigger("jump");
        playerEntity.getEvents().trigger("landed");

        assertFalse(playerActions.getIsJumping());
        assertFalse(playerActions.getIsDoubleJumping());
    }

    @Test
    void testCrouchTogglesState() {
        playerEntity.getEvents().trigger("crouch");
        assertTrue(playerActions.getIsCrouching());

        playerEntity.getEvents().trigger("crouch");
        assertFalse(playerActions.getIsCrouching());
    }

    @Test
    void testInteractPlaysSound() {
        playerEntity.getEvents().trigger("interact");
        assertTrue(playerActions.hasSoundPlayed());
    }

    @Test
    void testPlayerAdrenaline() {
        assertFalse(playerActions.hasAdrenaline());
        playerEntity.getEvents().trigger("toggleAdrenaline");
        assertTrue(playerActions.hasAdrenaline());
    }

    @Test
    void testAdrenalineCanBeToggled() {
        // On
        playerEntity.getEvents().trigger("toggleAdrenaline");
        assertTrue(playerActions.hasAdrenaline());
        // Off
        playerEntity.getEvents().trigger("toggleAdrenaline");
        assertFalse(playerActions.hasAdrenaline());
        // On
        playerEntity.getEvents().trigger("toggleAdrenaline");
        assertTrue(playerActions.hasAdrenaline());
    }

    @Test
    void testCanDash() {
        assertFalse(playerActions.hasDashed());
        playerEntity.getEvents().trigger("dash");
        assertTrue(playerActions.hasDashed());
    }

    @Test
    void testCantDashWhileCrouching() {
        assertFalse(playerActions.hasDashed());
        playerEntity.getEvents().trigger("crouch");
        playerEntity.getEvents().trigger("dash");
        assertFalse(playerActions.hasDashed());

        playerEntity.getEvents().trigger("crouch");
        playerEntity.getEvents().trigger("dash");
        assertTrue(playerActions.hasDashed());

    }
}
