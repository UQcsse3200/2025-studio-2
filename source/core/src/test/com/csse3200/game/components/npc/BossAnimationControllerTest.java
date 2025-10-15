package com.csse3200.game.components.npc;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.AnimationRenderComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class BossAnimationControllerTest {
    private Entity makeEntityWithDAC() {
        Entity e = new Entity();
        e.addComponent(new BossAnimationController());
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
        verify(anim, times(1)).startAnimation("bossChase");
    }

    @Test
    void chaseStart_playsAngryFloat() {
        Entity e = makeEntityWithDAC();
        AnimationRenderComponent anim = mock(AnimationRenderComponent.class);
        e.addComponent(anim);
        e.create();
        e.getEvents().trigger("chaseStart");
        verify(anim, times(1)).startAnimation("bossChase");
    }


    @Test
    void generateDroneStart_playsFloat() {
        // Arrange
        AnimationRenderComponent animator = mock(AnimationRenderComponent.class);
        Entity boss = new Entity();
        boss.addComponent(animator);
        boss.addComponent(new BossAnimationController());
        boss.create(); // register

        // Act
        boss.getEvents().trigger("generateDroneStart");

        // Assert
        verify(animator).startAnimation("bossGenerateDrone");
    }

    @Test
    void touchKillStart_playsDrop() {
        Entity e = makeEntityWithDAC();
        AnimationRenderComponent anim = mock(AnimationRenderComponent.class);
        e.addComponent(anim);
        e.create();
        e.getEvents().trigger("touchKillStart");
        verify(anim, times(1)).startAnimation("bossTouchKill");
    }

    @Test
    void laserShootStart_playsDrop() {
        Entity e = makeEntityWithDAC();
        AnimationRenderComponent anim = mock(AnimationRenderComponent.class);
        e.addComponent(anim);
        e.create();
        e.getEvents().trigger("shootLaserStart");
        verify(anim, times(1)).startAnimation("bossShootLaser");
    }

    @Test
    void correctAnimationsCalledForSequence() {
        Entity e = makeEntityWithDAC();
        AnimationRenderComponent anim = mock(AnimationRenderComponent.class);
        e.addComponent(anim);
        e.create();

        e.getEvents().trigger("chaseStart");
        e.getEvents().trigger("touchKillStart");
        e.getEvents().trigger("chaseStart");

        verify(anim, times(2)).startAnimation("bossChase");
        verify(anim, times(1)).startAnimation("bossTouchKill");
        verify(anim, never()).startAnimation("bossShootLaser");
    }
}
