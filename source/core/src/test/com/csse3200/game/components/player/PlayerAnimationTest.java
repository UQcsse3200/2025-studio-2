package com.csse3200.game.components.player;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class PlayerAnimationTest {
    private PlayerAnimationController controller;
    private AnimationRenderComponent animator;
    private PlayerActions actions;
    private Entity player;

    /**
     * setUp() was authored with assistance from ChatGPT-5
     * Date: 16/09/25
     */
    @BeforeEach
    void setUp() {
        animator = Mockito.mock(AnimationRenderComponent.class);
        actions = Mockito.mock(PlayerActions.class);

        ResourceService rs = mock(ResourceService.class);
        when(rs.getAsset(anyString(), eq(Sound.class))).thenReturn(mock(Sound.class));

        ServiceLocator.registerResourceService(rs);

        player = new Entity()
                .addComponent(animator)
                .addComponent(actions);

        controller = new PlayerAnimationController();
        player.addComponent(controller);

        controller.create();

        controller.scheduleTask = (runnable, delay) -> { /* do nothing */ };

    }

    @Test
    void testAnimateStop() {
        when(actions.getIsCrouching()).thenReturn(false);
        controller.animateWalk(new Vector2(1f, 0f));
        verify(animator).startAnimation("RIGHT");
        clearInvocations(animator);
        player.getEvents().trigger("walkStop");
        verify(animator).startAnimation("IDLE");
    }

    @Test
    void testAnimateJumpRight() {
        controller.animateWalk(new Vector2(1f, 0f));
        player.getEvents().trigger("jump");
        verify(animator).startAnimation("JUMP");
        // double jump
        player.getEvents().trigger("jump");
        verify(animator, times(2)).startAnimation("JUMP");
        player.getEvents().trigger("landed");
        verify(animator).startAnimation("IDLE");
    }

    @Test
    void testAnimateJumpLeft() {
        controller.animateWalk(new Vector2(-1f, 0f));
        player.getEvents().trigger("jump");
        verify(animator).startAnimation("JUMPLEFT");
        // double jump
        player.getEvents().trigger("jump");
        verify(animator, times(2)).startAnimation("JUMPLEFT");
        // land
        player.getEvents().trigger("landed");
        verify(animator).startAnimation("IDLELEFT");
    }

    @Test
    void testAnimateWalkRight() {
        when(actions.getIsCrouching()).thenReturn(false);
        controller.animateWalk(new Vector2(1f, 0f));
        verify(animator).startAnimation("RIGHT");
    }

    @Test
    void testAnimateWalkLeft() {
        when(actions.getIsCrouching()).thenReturn(false);
        controller.animateWalk(new Vector2(-1f, 0f));
        verify(animator).startAnimation("LEFT");
    }

    @Test
    void testAnimateCrouchingToIdle() {
        when(actions.getIsCrouching()).thenReturn(true);
        // crouch
        player.getEvents().trigger("crouch");
        verify(animator).startAnimation("CROUCH");
        reset(animator);
        controller.animateWalk(new Vector2(1f, 0f));
        verify(animator).startAnimation("CROUCHMOVE");
        reset(animator);
        // walk left
        controller.animateWalk(new Vector2(-1f, 0f));
        verify(animator).startAnimation("CROUCHMOVELEFT");
        reset(animator);
        when(actions.getIsCrouching()).thenReturn(false);
        // uncrouch
        player.getEvents().trigger("crouch");
        player.getEvents().trigger("walkStop");
        verify(animator, atLeastOnce()).startAnimation("IDLELEFT");
    }

    @Test
    void testAnimateCrouchingToCrouch() {
        // crouch
        when(actions.getIsCrouching()).thenReturn(true);
        player.getEvents().trigger("crouch");
        verify(animator).startAnimation("CROUCH");
        reset(animator);
        // uncrouch
        when(actions.getIsCrouching()).thenReturn(false);
        player.getEvents().trigger("crouch");
        player.getEvents().trigger("walkStop");
        verify(animator, atLeastOnce()).startAnimation("IDLE");
        reset(animator);
        // crouch again
        when(actions.getIsCrouching()).thenReturn(true);
        player.getEvents().trigger("crouch");
        player.getEvents().trigger("walkStop");
        verify(animator).startAnimation("CROUCH");
    }

    @Test
    void testAnimateDashRight() {
        // Verify dash right animation plays and resets to idle
        player.getEvents().trigger("dash");
        verify(animator).startAnimation("DASH");

        player.getEvents().trigger("walkStop");
        verify(animator).startAnimation("IDLE");
    }

    @Test
    void testAnimateDashLeft() {
        // Verify dash left animation plays and resets to idle

        controller.animateWalk(new Vector2(-1f, 0f));
        player.getEvents().trigger("dash");
        verify(animator).startAnimation("DASHLEFT");

        player.getEvents().trigger("walkStop");
        verify(animator).startAnimation("IDLELEFT");
    }

    @Test
    void testAnimateHurtRight() {
        // Verify hurt right animation plays and resets to idle

        player.getEvents().trigger("hurt");
        verify(animator).startAnimation("HURT");

        player.getEvents().trigger("walkStop");
        verify(animator).startAnimation("IDLE");
    }

    @Test
    void testAnimateHurtLeft() {
        // Verify hurt left animation plays and resets to idle

        controller.animateWalk(new Vector2(-1f, 0f));
        player.getEvents().trigger("hurt");
        verify(animator).startAnimation("HURTLEFT");

        player.getEvents().trigger("walkStop");
        verify(animator).startAnimation("IDLELEFT");
    }

    @Test
    void testSetAnimation() {
        String[] animations = new String[] {"RIGHT", "LEFT", "DASH", "DASHLEFT", "HURT", "HURTLEFT",
                "JUMP", "JUMPLEFT", "CROUCHMOVE", "CROUCHMOVELEFT", "IDLE", "IDLELEFT"};

        for (String s : animations) {
            controller.setAnimation(s);
            verify(animator).startAnimation("RIGHT");
        }
    }
}
