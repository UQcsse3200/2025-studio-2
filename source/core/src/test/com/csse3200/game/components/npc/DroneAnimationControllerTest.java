package com.csse3200.game.components.npc;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.AnimationRenderComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class DroneAnimationControllerTest {
    private Entity makeEntityWithDAC() {
        Entity e = new Entity();
        e.addComponent(new DroneAnimationController());
        return e;
    }

    @Test
    void sameEventDoesNotRestartAnim() {
        Entity e = makeEntityWithDAC();
        AnimationRenderComponent anim = mock(AnimationRenderComponent.class);
        e.addComponent(anim);
        e.create();
        e.getEvents().trigger("chaseStart");
        e.getEvents().trigger("chaseStart");
        verify(anim, times(1)).startAnimation("angry_float");
    }

    @Test
    void chaseStart_playsAngryFloat() {
        Entity e = makeEntityWithDAC();
        AnimationRenderComponent anim = mock(AnimationRenderComponent.class);
        e.addComponent(anim);
        e.create();
        e.getEvents().trigger("chaseStart");
        verify(anim, times(1)).startAnimation("angry_float");
    }

    @Test
    void wanderStart_playsFloat() {
        Entity e = makeEntityWithDAC();
        AnimationRenderComponent anim = mock(AnimationRenderComponent.class);
        e.addComponent(anim);
        e.create();
        e.getEvents().trigger("wanderStart");
        verify(anim, times(1)).startAnimation("float");
    }

    @Test
    void patrolStart_playsFloat() {
        Entity e = makeEntityWithDAC();
        AnimationRenderComponent anim = mock(AnimationRenderComponent.class);
        e.addComponent(anim);
        e.create();
        e.getEvents().trigger("patrolStart");
        verify(anim, times(1)).startAnimation("float");
    }

    @Test
    void dropStart_playsDrop() {
        Entity e = makeEntityWithDAC();
        AnimationRenderComponent anim = mock(AnimationRenderComponent.class);
        e.addComponent(anim);
        e.create();
        e.getEvents().trigger("dropStart");
        verify(anim, times(1)).startAnimation("drop");
    }

    @Test
    void correctAnimationsCalledForSequence() {
        Entity e = makeEntityWithDAC();
        AnimationRenderComponent anim = mock(AnimationRenderComponent.class);
        e.addComponent(anim);
        e.create();

        e.getEvents().trigger("chaseStart");
        e.getEvents().trigger("dropStart");
        e.getEvents().trigger("chaseStart");

        verify(anim, times(2)).startAnimation("angry_float");
        verify(anim, times(1)).startAnimation("drop");
        verify(anim, never()).startAnimation("float");
    }
}
