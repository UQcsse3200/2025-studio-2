package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeathZoneComponentTest {
    private Entity deathZone;
    private DeathZoneComponent deathZoneComponent;
    private Entity player;
    private ColliderComponent playerCollider;
    private Vector2 resetPos;

    @BeforeEach
    void setup() {
        ServiceLocator.registerPhysicsService(new PhysicsService());

        resetPos = new Vector2(5, 5);
        deathZone = new Entity().addComponent(new PhysicsComponent());
        deathZoneComponent = new DeathZoneComponent(0, 50);
        deathZone.addComponent(deathZoneComponent);

        player = new Entity().addComponent(new PhysicsComponent())
                .addComponent(new CombatStatsComponent(100, 10));
        playerCollider = new ColliderComponent();
        player.addComponent(playerCollider);
    }

    @AfterEach
    void cleanup() {
        ServiceLocator.clear();
    }

    @Test
    void testHealthMethodsDoNothing() {
        int originalHealth = deathZoneComponent.getHealth();
        deathZoneComponent.setHealth(999);
        assertEquals(originalHealth, deathZoneComponent.getHealth());

        deathZoneComponent.addHealth(50);
        assertEquals(originalHealth, deathZoneComponent.getHealth());

        deathZoneComponent.hit(player.getComponent(CombatStatsComponent.class));
        assertEquals(originalHealth, deathZoneComponent.getHealth());

        assertTrue(deathZoneComponent.isDead()); // always true
    }

    /*@Test
    void testDamageFromAbove() {
        // Place death zone at y=0, player above
        deathZone.setPosition(0, 0);
        player.setPosition(0, 1);

        int initialHealth = player.getComponent(CombatStatsComponent.class).getHealth();
        deathZoneComponent.damage(playerCollider);

        int expectedHealth = initialHealth - deathZoneComponent.getBaseAttack();
        assertEquals(expectedHealth, player.getComponent(CombatStatsComponent.class).getHealth(),
                "Player should take damage from death zone");
    }*/

    @Test
    void testDamageFromBelowDoesNothing() {
        deathZone.setPosition(0, 0);
        player.setPosition(0, -1);

        int initialHealth = player.getComponent(CombatStatsComponent.class).getHealth();
        deathZoneComponent.damage(playerCollider);

        assertEquals(initialHealth, player.getComponent(CombatStatsComponent.class).getHealth(),
                "Player should not take damage if below the death zone");
    }

    @Test
    void testDamageWithNullColliderDoesNothing() {
        int initialHealth = player.getComponent(CombatStatsComponent.class).getHealth();
        deathZoneComponent.damage(null);

        assertEquals(initialHealth, player.getComponent(CombatStatsComponent.class).getHealth(),
                "Null collider should not cause damage");
    }
}
