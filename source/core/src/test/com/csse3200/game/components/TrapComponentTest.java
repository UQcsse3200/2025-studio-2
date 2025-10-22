package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.obstacles.TrapComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.TrapFactory;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrapComponentTest {
    private Entity spikeTrap;
    private TrapComponent trapComponent;
    private Entity player;
    private ColliderComponent playerCollider;

    @BeforeEach
    void setup() {
        // Register PhysicsService to initialise a trap's physics body during tests
        ServiceLocator.registerPhysicsService(new PhysicsService());

        // Mock services so assets won't throw exceptions
        ResourceService mockResourceService = mock(ResourceService.class);
        when(mockResourceService.getAsset(anyString(), any())).thenReturn(null);
        ServiceLocator.registerResourceService(mockResourceService);
        Graphics mockGraphics = mock(Graphics.class);
        when(mockGraphics.getFrameId()).thenReturn(1L);
        Gdx.graphics = mockGraphics;


        // Create the trap entity
        spikeTrap = TrapFactory.createSpikes(new Vector2(0, 0), 0f);
        trapComponent = spikeTrap.getComponent(TrapComponent.class);

        // Create player with collider and health
        player = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new CombatStatsComponent(100, 10));
        player.setPosition(0.35f, 0.1f);
        playerCollider = new ColliderComponent();
        player.addComponent(playerCollider);
    }

    @AfterEach
    void cleanup() {
        ServiceLocator.clear();
    }

    @Test
    void testDamageUp() {
        int playerInitHealth = player.getComponent(CombatStatsComponent.class).getHealth();
        trapComponent.damage(playerCollider);
        assert player.getComponent(CombatStatsComponent.class).getHealth()
                == playerInitHealth - trapComponent.getBaseAttack();
    }

    @Test
    void testHealthAliveUnchanging() {
        assert trapComponent.isDead();
        int trapInitHealth = trapComponent.getHealth();
        trapComponent.addHealth(50);
        assert trapComponent.getHealth() == trapInitHealth;
        trapComponent.hit(player.getComponent(CombatStatsComponent.class));
        assert trapComponent.getHealth() == trapInitHealth;
    }

    @Test
    void testDamageLeft() {
        CombatStatsComponent playerCombat = player.getComponent(CombatStatsComponent.class);
        int playerInitHealth = playerCombat.getHealth();
        Entity rotatedSpikes = TrapFactory.createSpikes(new Vector2(0, 0), 90f);
        player.setPosition(new Vector2(rotatedSpikes.getPosition().x - 1f,
                rotatedSpikes.getPosition().y + 0.1f));
        trapComponent = rotatedSpikes.getComponent(TrapComponent.class);
        trapComponent.damage(playerCollider);
        assert playerCombat.getHealth()
                == playerInitHealth - trapComponent.getBaseAttack();
    }

    @Test
    void testDamageRight() {
        CombatStatsComponent playerCombat = player.getComponent(CombatStatsComponent.class);
        int playerInitHealth = playerCombat.getHealth();
        Entity rotatedSpikes = TrapFactory.createSpikes(new Vector2(0, 0), 270f);
        player.setPosition(new Vector2(rotatedSpikes.getPosition().x + 1f,
                rotatedSpikes.getPosition().y + 0.1f));
        trapComponent = rotatedSpikes.getComponent(TrapComponent.class);
        trapComponent.damage(playerCollider);
        assert playerCombat.getHealth()
                == playerInitHealth - trapComponent.getBaseAttack();
    }

    @Test
    void testDamageDown() {
        CombatStatsComponent playerCombat = player.getComponent(CombatStatsComponent.class);
        int playerInitHealth = playerCombat.getHealth();
        Entity rotatedSpikes = TrapFactory.createSpikes(new Vector2(0, 0), 180f);
        player.setPosition(new Vector2(rotatedSpikes.getPosition().x,
                rotatedSpikes.getPosition().y - 0.1f));
        trapComponent = rotatedSpikes.getComponent(TrapComponent.class);
        trapComponent.damage(playerCollider);
        assert playerCombat.getHealth()
                == playerInitHealth - trapComponent.getBaseAttack();
    }
}
