package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
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

    // Reference: Gemini to set up the @BeforeEach
    @BeforeEach
    void setup() {
        ServiceLocator.registerPhysicsService(new PhysicsService());

        InputService inputSvc = mock(InputService.class);
        InputFactory inputFactory = mock(InputFactory.class);
        when(inputSvc.getInputFactory()).thenReturn(inputFactory);
        when(inputFactory.createForPlayer()).thenReturn(mock(InputComponent.class));
        ServiceLocator.registerInputService(inputSvc);

        // If your entities render or dispose, also stub these:
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

    }

    @Test
    void playerCollectsKeyOnCollision() {
        Entity player = PlayerFactoryTest.createPlayer();
        player.create();
        InventoryComponent inv = player.getComponent(InventoryComponent.class);
        assertNotNull(inv);

        Entity key = CollectableFactory.createKey("pink-key");
        key.create();
        key.getEvents().trigger("collisionStart", key, player);

        assertTrue(inv.hasItem("pink-key"));

        Entity key2 = CollectableFactory.createKey("pink-key");
        key2.create();
        key2.getEvents().trigger("collisionStart", key, player);
        assertEquals(2, inv.getItemCount("pink-key"));

        key.getEvents().trigger("collisionStart", key, player);
        assertEquals(2, inv.getItemCount("pink-key"));
    }
}
