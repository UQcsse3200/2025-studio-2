package com.csse3200.game.components;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.obstacles.TrapComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.TrapFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TrapComponentTest {
    private Entity spikeTrap;
    private TrapComponent trapComponent;
    private Entity player;
    private ColliderComponent playerCollider;

    @BeforeEach
    void setup() {
        // Register PhysicsService to initialise a trap's physics body during tests
        ServiceLocator.registerPhysicsService(new PhysicsService());

        // Mock ResourceService so Texture assets won't throw exceptions
        ResourceService mockResourceService = mock(ResourceService.class);
        when(mockResourceService.getAsset(anyString(), any())).thenReturn(null);
        ServiceLocator.registerResourceService(mockResourceService);

        // Create the trap entity
        spikeTrap = TrapFactory.createSpikes(new Vector2(0, 0), 0f);
        trapComponent = spikeTrap.getComponent(TrapComponent.class);

        // Create player with collider and health
        player = new Entity().addComponent(new PhysicsComponent()).addComponent(new CombatStatsComponent(100, 10));
        playerCollider = new ColliderComponent();
        player.addComponent(playerCollider);
    }

    @AfterEach
    void cleanup() {
        ServiceLocator.clear();
    }

    @Test
    void testDamage() {
        int playerInitHealth = player.getComponent(CombatStatsComponent.class).getHealth();
        trapComponent.damage(playerCollider);
        assert player.getComponent(CombatStatsComponent.class).getHealth() == playerInitHealth - trapComponent.getBaseAttack();
    }
}
