package com.csse3200.game.components.npc;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LaserAnimationControllerTest {

    @Mock
    private Entity entity;

    @Mock
    private AnimationRenderComponent animator;

    @Mock
    private Application app;

    private LaserAnimationController controller;

    @BeforeEach
    void setUp() {
        Gdx.app = app;
        controller = new LaserAnimationController();
        controller.setEntity(entity);
    }

    @Test
    void shouldStartLaserAnimationOnCreate() {
        when(entity.getComponent(AnimationRenderComponent.class)).thenReturn(animator);

        controller.create();

        verify(animator).startAnimation("laserEffect");
    }

    @Test
    void shouldHandleNullAnimatorOnCreate() {
        when(entity.getComponent(AnimationRenderComponent.class)).thenReturn(null);

        controller.create();

        verify(entity).getComponent(AnimationRenderComponent.class);
    }

    @Test
    void shouldNotCleanUpWhenAnimationNotFinished() {
        when(entity.getComponent(AnimationRenderComponent.class)).thenReturn(animator);
        when(animator.isFinished()).thenReturn(false);

        controller.create();
        controller.update();

        verify(animator, never()).stopAnimation();
        verify(app, never()).postRunnable(any());
    }

    @Test
    void shouldCleanUpWhenAnimationFinished() {
        when(entity.getComponent(AnimationRenderComponent.class)).thenReturn(animator);
        when(animator.isFinished()).thenReturn(true);

        controller.create();
        controller.update();

        verify(animator).stopAnimation();
        verify(app).postRunnable(any(Runnable.class));
    }

    @Test
    void shouldDisposeEntityWhenCleaningUp() {
        when(entity.getComponent(AnimationRenderComponent.class)).thenReturn(animator);
        when(animator.isFinished()).thenReturn(true);
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(app).postRunnable(any(Runnable.class));

        controller.create();
        controller.update();

        verify(entity).dispose();
    }

    @Test
    void shouldNotCleanUpMultipleTimes() {
        when(entity.getComponent(AnimationRenderComponent.class)).thenReturn(animator);
        when(animator.isFinished()).thenReturn(true);

        controller.create();
        controller.update();
        controller.update(); // Call update again

        verify(animator, times(1)).stopAnimation();
        verify(app, times(1)).postRunnable(any(Runnable.class));
    }

    @Test
    void shouldHandleUpdateWithNullAnimator() {
        when(entity.getComponent(AnimationRenderComponent.class)).thenReturn(null);

        controller.create();
        controller.update();


        verify(app, never()).postRunnable(any());
    }

    @Test
    void shouldNotUpdateAfterCleanup() {
        when(entity.getComponent(AnimationRenderComponent.class)).thenReturn(animator);
        when(animator.isFinished()).thenReturn(true);

        controller.create();
        controller.update();

        reset(animator);
        controller.update();

        verify(animator, never()).isFinished();
    }

    @Test
    void shouldStopAnimationBeforeDisposal() {
        when(entity.getComponent(AnimationRenderComponent.class)).thenReturn(animator);
        when(animator.isFinished()).thenReturn(true);

        controller.create();
        controller.update();


        var inOrder = inOrder(animator, app);
        inOrder.verify(animator).stopAnimation();
        inOrder.verify(app).postRunnable(any(Runnable.class));
    }
}