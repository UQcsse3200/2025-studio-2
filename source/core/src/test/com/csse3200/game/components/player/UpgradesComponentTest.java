package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.components.collectables.UpgradesComponent;
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
public class UpgradesComponentTest {

    private Entity player;
    private InventoryComponent inv;

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

        EntityService entityService = mock(EntityService.class);
        doNothing().when(entityService).register(any());
        doNothing().when(entityService).unregister(any());
        ServiceLocator.registerEntityService(entityService);

        player = PlayerFactoryTest.createPlayer();
        player.create();
        inv = player.getComponent(InventoryComponent.class);
        assertNotNull(inv, "Player should have InventoryComponent");
    }

    @Test
    void upgradeStartsUncollected() {
        Entity upgrade = CollectableFactory.createDashUpgrade("dash");
        upgrade.create();

        assertFalse(inv.hasItem("dash"), "Inventory should not contain an upgrade initially");
        UpgradesComponent dash = upgrade.getComponent(UpgradesComponent.class);
        assertNotNull(dash, "Upgrade entity should have an UpgradeComponent");
        assertEquals("dash", dash.getUpgradeId(), "UpgradeComponent should store its ID");
    }
}
