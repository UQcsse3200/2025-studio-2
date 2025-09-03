package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.components.obstacles.TrapComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TrapFactoryTest {
    @BeforeEach
    void setupGameServices() {
        // Register PhysicsService to initialise a trap's physics body during tests
        ServiceLocator.registerPhysicsService(new PhysicsService());

        // Mock ResourceService so assets won't throw exceptions
        ResourceService mockResourceService = mock(ResourceService.class);
        when(mockResourceService.getAsset(anyString(), any())).thenReturn(null);
        ServiceLocator.registerResourceService(mockResourceService);
    }

    @Test
    void createSpikeHasAllComponents() {
        Entity spikeTrap = TrapFactory.createSpikes(new GridPoint2(0,0), new GridPoint2(0, 0));
        assertNotNull(spikeTrap.getComponent(PhysicsComponent.class),
                "Spike trap should have a PhysicsComponent");
        assertNotNull(spikeTrap.getComponent(ColliderComponent.class),
                "Spike trap should have a ColliderComponent");
        assertNotNull(spikeTrap.getComponent(TrapComponent.class),
                "Static Box should have a TrapComponent");
    }
}
