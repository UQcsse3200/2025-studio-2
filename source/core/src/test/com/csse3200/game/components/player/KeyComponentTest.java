package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.components.collectables.KeyComponent;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.CollectableFactory;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class KeyComponentTest {

    private EntityService entityService;
    private Entity player;
    private InventoryComponent inv;

    /*
     * setup() authored with assistance from ChatGPT (GPT-5 Thinking)
     * Date: 2025-08-25 (AEST)
     */
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

        entityService = mock(EntityService.class);
        doNothing().when(entityService).register(any());
        doNothing().when(entityService).unregister(any());
        ServiceLocator.registerEntityService(entityService);

        player = PlayerFactoryTest.createPlayer();
        player.create();
        inv = player.getComponent(InventoryComponent.class);
        assertNotNull(inv, "Player should have InventoryComponent");
    }

    @Test
    void keyStartsUncollected() {
        Entity key = CollectableFactory.createKey("pink-key");
        key.create();

        assertFalse(inv.hasItem("pink-key"), "Inventory should not contain the key initially");
        verify(entityService, never()).unregister(key);

        KeyComponent kc = key.getComponent(KeyComponent.class);
        assertNotNull(kc, "Key entity should have a KeyComponent");
        assertEquals("pink-key", kc.getKeyId(), "KeyComponent should store its id");
    }

    @Test
    void collectMarksAsCollected() {
        Entity key = CollectableFactory.createKey("pink-key");
        key.create();
        assertFalse(inv.hasItem("pink-key"));

        // Simulate collision (adjust if your signal is single-arg)
        key.getEvents().trigger("collisionStart", key, player);

        // Inventory updated and entity disposed via unregister()
        assertTrue(inv.hasItem("pink-key"), "Inventory should contain the collected key");
        verify(entityService, times(1)).unregister(key);

        // Triggering again doesn't double-add or double-unregister
        int countAfter = inv.getItemCount("pink-key");
        key.getEvents().trigger("collisionStart", key, player);
        assertEquals(countAfter, inv.getItemCount("pink-key"));
        verify(entityService, times(1)).unregister(key);
    }

    @Test
    void cannotCollectTwiceAcrossMultipleEntitiesOfSameId() {
        Entity key1 = CollectableFactory.createKey("pink-key");
        Entity key2 = CollectableFactory.createKey("pink-key");
        key1.create();
        key2.create();

        // Collect first key
        key1.getEvents().trigger("collisionStart", key1, player);
        assertTrue(inv.hasItem("pink-key"));
        int afterFirst = inv.getItemCount("pink-key");

        // Collect second key of same id
        key2.getEvents().trigger("collisionStart", key2, player);
        assertEquals(afterFirst + 1, inv.getItemCount("pink-key"),
                "Collecting a second entity of the same key id should increment by one");

        // Double-trigger on the first should not increase coin
        key1.getEvents().trigger("collisionStart", key1, player);
        assertEquals(afterFirst + 1, inv.getItemCount("pink-key"));
    }

    @Test
    void nonPlayerCollisionDoesNothing() {
        Entity key = CollectableFactory.createKey("pink-key");
        key.create();
        Entity rock = new Entity(); // no InventoryComponent, not a player
        rock.create();

        key.getEvents().trigger("collisionStart", key, rock);
        assertEquals(0, inv.getTotalItemCount());
        verify(entityService, never()).unregister(key);
    }
}
