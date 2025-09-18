package com.csse3200.game.components;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.obstacles.TrapComponent;
import com.csse3200.game.components.DeathZoneComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.rendering.AnimationRenderComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class DeathOnTrapComponentTest {

    private Entity droneEntity;
    private Entity trapEntity;
    private Fixture fixture;
    private Body body;

    @BeforeEach
    void setup() {
        // Create drone entity
        droneEntity = new Entity();
        DeathOnTrapComponent deathComp = new DeathOnTrapComponent();
        droneEntity.addComponent(deathComp);

        // Add animation component
        AnimationRenderComponent animator = mock(AnimationRenderComponent.class);
        when(animator.hasAnimation("bomb_effect")).thenReturn(true);
        droneEntity.addComponent(animator);

        droneEntity.create();

        // Create trap entity
        trapEntity = new Entity();
        trapEntity.addComponent(new TrapComponent(null));
        trapEntity.create();

        // Mock Box2D Body and Fixture
        body = mock(Body.class);
        BodyUserData userData = new BodyUserData();
        userData.entity = trapEntity;
        when(body.getUserData()).thenReturn(userData);

        fixture = mock(Fixture.class);
        when(fixture.getBody()).thenReturn(body);
    }

    @Test
    void shouldTriggerDeathWhenCollidingWithTrap() {
        // Trigger collision event
        droneEntity.getEvents().trigger("collisionStart", fixture, fixture);

    }

    @Test
    void shouldNotTriggerDeathIfAlreadyTriggered() {
        // First collision
        droneEntity.getEvents().trigger("collisionStart", fixture, fixture);
        // Second collision should not throw
        droneEntity.getEvents().trigger("collisionStart", fixture, fixture);
    }

    @Test
    void shouldResetTriggerOnResetEvent() {
        // Trigger death
        droneEntity.getEvents().trigger("collisionStart", fixture, fixture);
        // Trigger reset
        droneEntity.getEvents().trigger("reset");

    }
}
