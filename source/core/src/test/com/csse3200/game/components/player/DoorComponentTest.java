package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.components.obstacles.DoorComponent;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.CollectableFactory;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactoryTest;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.input.InputService;
import com.csse3200.game.input.InputFactory;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.areas.GameArea;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class DoorComponentTest {
    private GameArea game;

    // Reference: Gemini to set up the @BeforeEach
    @BeforeEach
    void setup() {
        ServiceLocator.registerPhysicsService(new PhysicsService());

        InputService inputSvc = mock(InputService.class);
        InputFactory inputFactory = mock(InputFactory.class);
        when(inputSvc.getInputFactory()).thenReturn(inputFactory);
        when(inputFactory.createForPlayer()).thenReturn(mock(InputComponent.class));
        ServiceLocator.registerInputService(inputSvc);

        RenderService render = mock(RenderService.class);
        doNothing().when(render).register(any());
        doNothing().when(render).unregister(any());
        ServiceLocator.registerRenderService(render);

        ResourceService rs = mock(ResourceService.class);
        when(rs.getAsset(anyString(), eq(Texture.class))).thenReturn(mock(Texture.class));
        when(rs.getAsset(anyString(), eq(Pixmap.class))).thenReturn(mock(Pixmap.class));
        doNothing().when(rs).loadTextures(any(String[].class));
        doNothing().when(rs).loadAll();
        ServiceLocator.registerResourceService(rs);

        EntityService es = mock(EntityService.class);
        doNothing().when(es).register(any());
        doNothing().when(es).unregister(any());
        ServiceLocator.registerEntityService(es);

        game = new GameArea() {
            @Override
            public void create() { }

            @Override
            protected void reset() {

            }
        };
    }

    // --- Helpers ---
    private Entity makePlayer() {
        Entity player = PlayerFactoryTest.createPlayer();
        player.create();
        return player;
    }

    private Entity makeDoor(String keyId, String levelId) {
        Entity door = ObstacleFactory.createDoor(keyId, game, levelId);
        door.create();
        return door;
    }

    private Entity makeKey(String keyId) {
        Entity key = CollectableFactory.createKey(keyId);
        key.create();
        return key;
    }
    // ----------------------------------------------------

    @Test
    void collideWithoutKey_doorRemainsLocked() {
        Entity player = makePlayer();
        Entity door   = makeDoor("pink-key", "level1");

        DoorComponent dc = door.getComponent(DoorComponent.class);
        door.getEvents().trigger("onCollisionStart", player);

        assertTrue(dc.isLocked(), "Door should stay locked when player has no key");
    }

    @Test
    void collectingKey_addsToInventory() {
        Entity player = makePlayer();
        InventoryComponent inv = player.getComponent(InventoryComponent.class);
        Entity key = makeKey("pink-key");

        key.getEvents().trigger("onCollisionStart", player);
        assertTrue(inv.hasItem("pink-key"), "Player should have the pink key after pickup");
    }

    @Test
    void collideWithKey_unlocksDoor_andConsumesKey() {
        Entity player = makePlayer();
        InventoryComponent inv = player.getComponent(InventoryComponent.class);

        // Give the player the key via the real pickup path to mirror gameplay
        Entity key = makeKey("pink-key");
        key.getEvents().trigger("onCollisionStart", player);
        assertTrue(inv.hasItem("pink-key"), "Sanity: player must have key before trying door");

        Entity door = makeDoor("pink-key", "level1");
        DoorComponent dc = door.getComponent(DoorComponent.class);
        assertTrue(dc.isLocked(), "Sanity: door starts locked");

        // Now collide: should unlock & consume key
        door.getEvents().trigger("onCollisionStart", player);

        assertFalse(dc.isLocked(), "Door should be unlocked after collision with correct key");
        assertFalse(inv.hasItem("pink-key"), "Key should be consumed when unlocking the door");
    }
}

