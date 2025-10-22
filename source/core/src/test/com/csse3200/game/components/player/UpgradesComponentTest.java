package com.csse3200.game.components.player;

import com.csse3200.game.components.collectables.effects.ItemEffectRegistry;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.CollectablesConfig;
import com.csse3200.game.entities.configs.EffectConfig;
import com.csse3200.game.services.CollectableService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.withSettings;

/**
 * Verifies upgrade items via the new inventory + effect pipeline (EffectConfig.target).
 * - Auto-consume upgrades fire effects on pickup and don't stay in inventory.
 * - Non-auto upgrades are stored in UPGRADES bag and fire on use.
 * - Effect proven by "upgradeUnlocked" event on the player.
 */
class InventoryUpgradesEffectsTest {

    private Entity player;
    private InventoryComponent inv;
    private MockedStatic<CollectableService> svcMock;

    /** Helper to build a CollectablesConfig for an upgrade item. */
    private static CollectablesConfig upgradeCfg(String itemId, boolean autoConsume, String targetAbility) {
        CollectablesConfig cfg = new CollectablesConfig();
        cfg.id = itemId;
        cfg.name = itemId;
        cfg.autoConsume = autoConsume;
        cfg.bag = "upgrades";
        EffectConfig e = new EffectConfig();
        e.type = "upgrade";
        e.target = targetAbility;
        cfg.effects = List.of(e);
        return cfg;
    }

    @BeforeEach
    void setUp() {
        // Fresh effect registry
        ItemEffectRegistry.clear();

        ItemEffectRegistry.register("upgrade", (player, cfg) -> {
            if (player == null || cfg == null || cfg.target == null || cfg.target.isBlank()) return false;
            player.getEvents().trigger("upgradeUnlocked", cfg.target.trim());
            return true;
        });

        svcMock = mockStatic(CollectableService.class, withSettings());
        svcMock.when(() -> CollectableService.get(eq("upgrade:dash")))
                .thenReturn(upgradeCfg("upgrade:dash",true, "dash"));
        svcMock.when(() -> CollectableService.get(eq("upgrade:glide")))
                .thenReturn(upgradeCfg("upgrade:glide",false, "glide"));
        svcMock.when(() -> CollectableService.get(eq("upgrade:jetpack")))
                .thenReturn(upgradeCfg("upgrade:jetpack", true, "jetpack"));
        svcMock.when(() -> CollectableService.get(argThat(id ->
                !"upgrade:dash".equals(id) &&
                        !"upgrade:glide".equals(id) &&
                        !"upgrade:jetpack".equals(id)))).thenReturn(null);

        // Minimal player with Inventory only
        player = new Entity();
        inv = new InventoryComponent();
        player.addComponent(inv);
        player.create();
    }

    @AfterEach
    void tearDown() {
        if (svcMock != null) svcMock.close();
        ItemEffectRegistry.clear();
    }

    @Test
    void autoConsumeUpgrade_unlocksOnPickup_andNotStored() {
        AtomicInteger unlockedCount = new AtomicInteger(0);
        player.getEvents().addListener("upgradeUnlocked", (String id) -> {
            assertEquals("dash", id);
            unlockedCount.incrementAndGet();
        });

        inv.addItems("upgrade:dash", 1);

        assertEquals(1, unlockedCount.get(), "Effect should fire once on pickup");
        assertEquals(0, inv.getItemCount(InventoryComponent.Bag.UPGRADES, "upgrade:dash"),
                "Auto-consume items should not remain in inventory");
    }

    @Test
    void nonAutoUpgrade_storedUntilUse_thenUnlocksAndConsumes() {
        AtomicInteger unlockedCount = new AtomicInteger(0);
        player.getEvents().addListener("upgradeUnlocked", (String id) -> {
            assertEquals("glide", id);
            unlockedCount.incrementAndGet();
        });

        inv.addItems("upgrade:glide", 1);

        assertEquals(1, unlockedCount.get(), "Effect fires on pickup even for non-auto items");

        assertEquals(1, inv.getItemCount(InventoryComponent.Bag.UPGRADES, "upgrade:glide"));

        assertTrue(inv.useItem(InventoryComponent.Bag.UPGRADES, "upgrade:glide"));
    }

    @Test
    void multipleUpgrades_mixedAutoAndNonAuto_allUnlock() {
        AtomicInteger unlockedCount = new AtomicInteger(0);
        player.getEvents().addListener("upgradeUnlocked", (String id) -> unlockedCount.incrementAndGet());

        inv.addItems("upgrade:dash", 1);
        inv.addItems("upgrade:jetpack", 1);
        inv.addItems("upgrade:glide", 1);

        assertEquals(3, unlockedCount.get(), "All upgrades unlock on pickup (including non-auto)");

        assertEquals(1, inv.getItemCount(InventoryComponent.Bag.UPGRADES, "upgrade:glide"),
                "Non-auto item should be stored after pickup");

        assertTrue(inv.useItem(InventoryComponent.Bag.UPGRADES, "upgrade:glide"),
                "Use should succeed when token is present");
    }


    @Test
    void unknownItemId_throwsIllegalArgumentException() {
        InventoryComponent inv = new InventoryComponent();

        assertThrows(IllegalArgumentException.class, () ->
                inv.addItems(InventoryComponent.Bag.UPGRADES, "upgrade:unknown", 1)
        );
    }
}
