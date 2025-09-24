package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.components.collectables.KeyComponent;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.configs.CollectablesConfig;
import com.csse3200.game.entities.factories.CollectableFactory;
import com.csse3200.game.entities.factories.PlayerFactoryTest;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.CollectableService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.input.InputService;
import com.csse3200.game.input.InputFactory;
import com.csse3200.game.services.ResourceService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class KeyComponentTest {

    private Entity player;
    private InventoryComponent inv;
    private MockedStatic<CollectableService> svcMock;

    private static CollectablesConfig cfg(String id) {
        var c = new CollectablesConfig();
        c.id = id;
        c.sprite = "";
        c.effects = List.of();
        return c;
    }

    /*
     * setup() authored with assistance from ChatGPT (GPT-5 Thinking)
     * Date: 2025-08-25 (AEST)
     */
    @BeforeEach
    void setup() {
        // Physics
        ServiceLocator.registerPhysicsService(new PhysicsService());

        // Input
        InputService inputSvc = mock(InputService.class);
        InputFactory inputFactory = mock(InputFactory.class);
        when(inputSvc.getInputFactory()).thenReturn(inputFactory);
        when(inputFactory.createForPlayer()).thenReturn(mock(InputComponent.class));
        ServiceLocator.registerInputService(inputSvc);

        // Rendering
        RenderService render = mock(RenderService.class);
        doNothing().when(render).register(any());
        doNothing().when(render).unregister(any());
        ServiceLocator.registerRenderService(render);

        // Resources
        ResourceService rs = mock(ResourceService.class);
        when(rs.getAsset(anyString(), eq(Texture.class))).thenReturn(mock(Texture.class));
        when(rs.getAsset(anyString(), eq(Pixmap.class))).thenReturn(mock(Pixmap.class));
        // Be lenient with varargs
        doNothing().when(rs).loadTextures(any(String[].class));
        doNothing().when(rs).loadAll();
        ServiceLocator.registerResourceService(rs);

        // Entities
        EntityService entityService = mock(EntityService.class);
        doNothing().when(entityService).register(any());
        doNothing().when(entityService).unregister(any());
        ServiceLocator.registerEntityService(entityService);

        // Static mock: CollectableService.get(String)
        svcMock = Mockito.mockStatic(CollectableService.class);
        svcMock.when(() -> CollectableService.get(anyString()))
                .thenAnswer(invocation -> cfg(invocation.getArgument(0)));

        // Player entity with inventory
        player = PlayerFactoryTest.createPlayer();
        player.create();
        inv = player.getComponent(InventoryComponent.class);
        assertNotNull(inv, "Player should have InventoryComponent");
    }

    @AfterEach
    void tearDown() {
        if (svcMock != null) {
            svcMock.close(); // IMPORTANT: release static mock to avoid cross-test pollution
        }
        // If your codebase provides this — clears registered services between tests.
        try {
            ServiceLocator.clear();
        } catch (Throwable ignored) { /* no-op if not available */ }
    }


    @Test
    void keyStartsUncollected() {
        Entity key = CollectableFactory.createKey("pink-key");
        key.create();

        assertFalse(inv.hasItem("pink-key"), "Inventory should not contain the key initially");

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
        key.getEvents().trigger("onCollisionStart", player);

        // Inventory updated
        assertTrue(inv.hasItem("pink-key"), "Inventory should contain the collected key");

        // Triggering again doesn't double-add or double-unregister
        int countAfter = inv.getItemCount("pink-key");
        key.getEvents().trigger("onCollisionStart", player);
        assertEquals(countAfter, inv.getItemCount("pink-key"));
    }

    @Test
    void cannotCollectTwiceAcrossMultipleEntitiesOfSameId() {
        Entity key1 = CollectableFactory.createKey("pink-key");
        Entity key2 = CollectableFactory.createKey("pink-key");
        key1.create();
        key2.create();

        // Collect first key
        key1.getEvents().trigger("onCollisionStart",  player);
        assertTrue(inv.hasItem("pink-key"));
        int afterFirst = inv.getItemCount("pink-key");

        // Collect second key of same id
        key2.getEvents().trigger("onCollisionStart", player);
        assertEquals(afterFirst + 1, inv.getItemCount("pink-key"),
                "Collecting a second entity of the same key id should increment by one");

        // Double-trigger on the first should not increase coin
        key1.getEvents().trigger("onCollisionStart", player);
        assertEquals(afterFirst + 1, inv.getItemCount("pink-key"));
    }
}
