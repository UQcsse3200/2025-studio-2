package com.csse3200.game.components;

import com.badlogic.gdx.math.Octree;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import net.dermetfan.gdx.physics.box2d.PositionController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(GameExtension.class)
public class BoxComponentTest {

    private Entity box;
    private BoxComponent boxComponent;
    private Entity player;
    private ColliderComponent playerCollider;

    @BeforeEach
    void setup() {

        // Register PhysicsService to initialise a box's physics body during tests
        ServiceLocator.registerPhysicsService(new PhysicsService());

        // Mock ResourceService so Texture assets won't throw exceptions
        ResourceService mockResourceService = mock(ResourceService.class);
        when(mockResourceService.getAsset(anyString(), any())).thenReturn(null);
        ServiceLocator.registerResourceService(mockResourceService);

        // Create the box entity
        box = new Entity().addComponent(new PhysicsComponent()).addComponent(new BoxComponent());
        boxComponent = box.getComponent(BoxComponent.class);
        box.create();

        // Create player with collider
        player = new Entity().addComponent(new PhysicsComponent());
        playerCollider = new ColliderComponent();
        player.addComponent(playerCollider);
    }

    @AfterEach
    void cleanup() {
        ServiceLocator.clear();
    }

    @Test
    void create_assignBoxPhysics() {
        assertNotNull(boxComponent.getBoxPhysics());
    }

    @Test
    void setPlayerInRange_addPlayerAndListeners() {

        boxComponent.setPlayerInRange(playerCollider);

        assertTrue(boxComponent.isPlayerInRange());
        assertTrue(boxComponent.isAddToPlayer());
        assertEquals(playerCollider, boxComponent.getPlayerCollider());
    }

    @Test
    void onPlayerInteract_toggleLiftingBox() {

        player.setPosition(new Vector2(0, 0));
        box.setPosition(new Vector2(0.5f, 0));

        boxComponent.setPlayerInRange(playerCollider);
        player.getEvents().trigger("interact");

        assertTrue(boxComponent.isLifted());

        player.getEvents().trigger("interact");
        assertFalse(boxComponent.isLifted());
    }

    @Test
    void update_movesBoxWithPlayerWhenLifted() {

        boxComponent.setPlayerInRange(playerCollider);
        player.setPosition(new Vector2(0,0));
        player.getEvents().trigger("interact"); // lift

        Vector2 initialPos = boxComponent.getBoxPhysics().getBody().getPosition().cpy();
        player.setPosition(new Vector2(1,0));
        boxComponent.update();

        Vector2 newPos = boxComponent.getBoxPhysics().getBody().getPosition();
        assertNotEquals(initialPos, newPos);
        assertEquals(1, newPos.x, 0.01);
    }

    @Test
    void onPlayerWalk_movesBoxWhenLifted() {
        boxComponent.setPlayerInRange(playerCollider);
        player.setPosition(new Vector2(0,0));
        player.getEvents().trigger("interact"); // lift

        Vector2 initialPos = boxComponent.getBoxPhysics().getBody().getPosition().cpy();

        boxComponent.onPlayerWalk(new Vector2(1,0));

        Vector2 newPos = boxComponent.getBoxPhysics().getBody().getPosition();
        assertNotEquals(initialPos, newPos);
    }

    @Test
    void resetPlayerState_resetsBox() {
        boxComponent.setPlayerInRange(playerCollider);
        boxComponent.setPlayerInRange(null);

        assertFalse(boxComponent.isPlayerInRange());
        assertFalse(boxComponent.isLifted());
        assertNull(boxComponent.getPlayerCollider());
    }
}
