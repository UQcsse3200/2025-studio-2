package com.csse3200.game.components;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.npc.DroneAnimationController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class SelfDestructComponentTest {

    private Entity drone;
    private Entity player;
    private SelfDestructComponent selfDestruct;
    private AnimationRenderComponent animator;
    private PhysicsComponent physics;
    private Sound sound;
    private CombatStatsComponent playerStats;

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();

        drone = mock(Entity.class);
        player = mock(Entity.class);

        animator = mock(AnimationRenderComponent.class);
        physics = mock(PhysicsComponent.class);
        sound = mock(Sound.class);
        playerStats = mock(CombatStatsComponent.class);

        EventHandler events = new EventHandler();
        when(drone.getEvents()).thenReturn(events);

        when(drone.getComponent(AnimationRenderComponent.class)).thenReturn(animator);
        when(drone.getComponent(PhysicsComponent.class)).thenReturn(physics);
        when(player.getComponent(CombatStatsComponent.class)).thenReturn(playerStats);

        // Default positions
        when(drone.getCenterPosition()).thenReturn(new Vector2(0, 0));
        when(player.getCenterPosition()).thenReturn(new Vector2(10, 10));

        // Resource service mock
        ResourceService resourceService = mock(ResourceService.class);
        when(resourceService.getAsset("sounds/explosion.mp3", Sound.class)).thenReturn(sound);
        when(sound.play(anyFloat())).thenReturn(1L);
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
        // Within collision radius
        when(player.getCenterPosition()).thenReturn(new Vector2(0.5f, 0.5f));
        when(playerStats.getHealth()).thenReturn(5);

        selfDestruct.update();

        verify(animator).startAnimation("bomb_effect");
        verify(playerStats).setHealth(3); // 5 - 2 damage
    }

    @Test
    void testExplodesWhenTooFarFromPlayer() {
        when(player.getCenterPosition()).thenReturn(new Vector2(100, 100));
        when(playerStats.getHealth()).thenReturn(10);

        selfDestruct.update();

        verify(animator).startAnimation("bomb_effect");
        verify(playerStats).setHealth(8); // 10 - 2 damage
    }

    @Test
    void testNoExplosionIfInRangeButNotTouching() {
        when(player.getCenterPosition()).thenReturn(new Vector2(5, 5));

        selfDestruct.update();

        verify(animator, never()).startAnimation("bomb_effect");
        verify(playerStats, never()).setHealth(anyInt());
    }

    @Test
    void testExplosionOnlyHappensOnce() {
        when(player.getCenterPosition()).thenReturn(new Vector2(0, 0));
        when(playerStats.getHealth()).thenReturn(10);

        selfDestruct.update();
        selfDestruct.update();

        verify(animator, times(1)).startAnimation("bomb_effect");
        verify(playerStats, times(1)).setHealth(8);
    }
}