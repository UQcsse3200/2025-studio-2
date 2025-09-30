package com.csse3200.game.components;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.entities.Entity;
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

    @BeforeEach
    void setUp() {
        // Mock drone and player
        drone = mock(Entity.class);
        player = mock(Entity.class);

        // Mock components
        animator = mock(AnimationRenderComponent.class);
        physics = mock(PhysicsComponent.class);
        sound = mock(Sound.class);

        ResourceService resourceService = mock(ResourceService.class);
        when(resourceService.getAsset("sounds/explosion.mp3", Sound.class)).thenReturn(sound);
        ServiceLocator.registerResourceService(resourceService);

        // Attach components to drone
        when(drone.getComponent(AnimationRenderComponent.class)).thenReturn(animator);
        when(drone.getComponent(PhysicsComponent.class)).thenReturn(physics);
        when(drone.getCenterPosition()).thenReturn(new Vector2(0, 0));
        when(player.getCenterPosition()).thenReturn(new Vector2(10, 10));

        // Initialize SelfDestructComponent
        selfDestruct = new SelfDestructComponent(player);
        selfDestruct.setEntity(drone);
    }


    @Test
    void testChaseActivatesWhenPlayerInLight() {
        // Mock a light component
        ConeLightComponent light = mock(ConeLightComponent.class);
        when(light.getDistance()).thenReturn(20f);
        when(light.getDirectionDeg()).thenReturn(45f);
        when(light.getConeDegree()).thenReturn(90f);
        when(drone.getComponent(ConeLightComponent.class)).thenReturn(light);

        when(player.getCenterPosition()).thenReturn(new Vector2(5, 5));

        selfDestruct.update();

        verify(light).dispose();
        verify(drone).removeComponent(light);
    }


    @Test
    void testNoExplosionWhenNotChasingAndNotInLight() {
        when(player.getCenterPosition()).thenReturn(new Vector2(50, 50));

        selfDestruct.update();

        verify(animator, never()).startAnimation(anyString());
        verify(sound, never()).play(anyFloat());
        verify(drone, never()).removeComponent(selfDestruct);
    }
}