package com.csse3200.game.components;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.npc.DroneAnimationController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
class SelfDestructComponentTest {
    private Entity player;
    private SelfDestructComponent selfDestruct;
    private AnimationRenderComponent animator;
    private Sound sound;
    private CombatStatsComponent playerStats;
    @Mock
    ResourceService resourceService;

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();

        Entity drone = mock(Entity.class);
        player = mock(Entity.class);

        animator = mock(AnimationRenderComponent.class);
        sound = mock(Sound.class);
        playerStats = mock(CombatStatsComponent.class);

        EventHandler events = new EventHandler();
        when(drone.getEvents()).thenReturn(events);

        when(drone.getComponent(AnimationRenderComponent.class)).thenReturn(animator);

        when(drone.getCenterPosition()).thenReturn(new Vector2(0, 0));
        when(player.getCenterPosition()).thenReturn(new Vector2(10, 10));

        ServiceLocator.registerResourceService(resourceService);

        DroneAnimationController controller = new DroneAnimationController();
        controller.setEntity(drone);
        controller.create();

        selfDestruct = new SelfDestructComponent(player);
        selfDestruct.setEntity(drone);
        selfDestruct.create();
    }

    @Test
    void testExplodesWhenTouchingPlayer() {
        when(resourceService.getAsset(anyString(), eq(Sound.class))).thenReturn(sound);
        when(sound.play(anyFloat())).thenReturn(1L);
        when(player.getComponent(CombatStatsComponent.class)).thenReturn(playerStats);

        // Within collision radius
        when(player.getCenterPosition()).thenReturn(new Vector2(0.5f, 0.5f));
        when(playerStats.getHealth()).thenReturn(5);

        selfDestruct.update();

        verify(animator).startAnimation("bomb_effect");
        verify(sound).play(anyFloat());
        verify(playerStats).setHealth(3); // 5 - 2 damage
    }

    @Test
    void testExplodesWhenTooFarFromPlayer() {
        when(resourceService.getAsset(anyString(), eq(Sound.class))).thenReturn(sound);
        when(sound.play(anyFloat())).thenReturn(1L);
        when(player.getComponent(CombatStatsComponent.class)).thenReturn(playerStats);

        when(player.getCenterPosition()).thenReturn(new Vector2(100, 100));
        when(playerStats.getHealth()).thenReturn(10);

        selfDestruct.update();

        verify(animator).startAnimation("bomb_effect");
        verify(sound).play(anyFloat());
        verify(playerStats).setHealth(8); // 10 - 2 damage
    }

    @Test
    void testNoExplosionIfInRangeButNotTouching() {
        when(player.getCenterPosition()).thenReturn(new Vector2(5, 5));

        selfDestruct.update();

        verifyNoInteractions(animator);
        verifyNoInteractions(sound);
        verifyNoInteractions(playerStats);
    }

    @Test
    void testExplosionOnlyHappensOnce() {
        when(resourceService.getAsset(anyString(), eq(Sound.class))).thenReturn(sound);
        when(sound.play(anyFloat())).thenReturn(1L);
        when(player.getComponent(CombatStatsComponent.class)).thenReturn(playerStats);

        when(player.getCenterPosition()).thenReturn(new Vector2(0, 0));
        when(playerStats.getHealth()).thenReturn(10);

        selfDestruct.update();
        selfDestruct.update();

        verify(animator).startAnimation("bomb_effect");
        verify(sound).play(anyFloat());
        verify(playerStats).setHealth(8);
    }
}