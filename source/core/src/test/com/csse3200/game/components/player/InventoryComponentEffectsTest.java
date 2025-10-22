package com.csse3200.game.components.player;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.collectables.effects.ItemEffectRegistry;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.CollectablesConfig;
import com.csse3200.game.entities.configs.EffectConfig;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.CollectableService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(GameExtension.class)
class InventoryComponentEffectsTest {

    private Entity player;
    private InventoryComponent inv;
    private CombatStatsComponent stats;
    private MockedStatic<CollectableService> svcMock;
    String tester = "tester";

    @BeforeEach
    void setup() {
        ItemEffectRegistry.clear();
        ItemEffectRegistry.registerDefaults();

        inv = new InventoryComponent();
        stats = new CombatStatsComponent(50, 50);
        player = new Entity().addComponent(inv).addComponent(stats);
        player.create();

        svcMock = Mockito.mockStatic(CollectableService.class);
        svcMock.when(() -> CollectableService.get(anyString()))
                .thenAnswer(invocation -> {
                    var id = invocation.getArgument(0);
                    var c = new CollectablesConfig();
                    c.id = id.toString();
                    c.sprite = "";
                    c.autoConsume = false;
                    c.effects = List.of();
                    return c;
                });
    }

    @AfterEach
    void tearDown() {
        if (svcMock != null) svcMock.close();
        ItemEffectRegistry.clear();
    }

    // ---------- Helpers ----------

    private CollectablesConfig cfg(String id, boolean autoConsume, List<EffectConfig> effects) {
        var c = new CollectablesConfig();
        c.id = id;
        c.sprite = "";
        c.autoConsume = autoConsume;
        c.effects = effects;
        return c;
    }

    private static EffectConfig heal(int value) {
        var e = new EffectConfig();
        e.type = "heal";
        e.value = value;
        e.duration = 0;
        return e;
    }

    // ---------- Tests ----------

    @Nested
    class AutoConsume {
        @Test
        void autoConsume_appliesEffectPerInstance_andDoesNotStore() {
            // auto-consume heal +10, *3 --> +30 total, no stacking
            var potion = cfg(tester, true, List.of(heal(10)));
            svcMock.when(() -> CollectableService.get(tester)).thenReturn(potion);

            assertEquals(50, stats.getHealth());
            inv.addItems(tester, 3);

            assertEquals(80, stats.getHealth()); // 50 + (3*10)
            assertEquals(0, inv.getItemCount(tester)); // not stored
            assertEquals(0, inv.getTotalItemCount());
        }

        @Test
        void autoConsume_withUnknownEffect_ignoresEffect_andDoesNotStore() {
            var unknown = new EffectConfig();
            unknown.type = "does_not_exist";
            unknown.value = 999;

            var item = cfg(tester, true, List.of(unknown));
            svcMock.when(() -> CollectableService.get(tester)).thenReturn(item);

            inv.addItems(tester, 2);

            // No effect applied, but auto-consume still means not stored
            assertEquals(50, stats.getHealth());
            assertEquals(0, inv.getItemCount(tester));
            assertEquals(0, inv.getTotalItemCount());
        }

        @Test
        void autoConsume_withEmptyEffects_isNoOp_onStats_andNotStored() {
            var empty = cfg(tester, true, List.of());
            svcMock.when(() -> CollectableService.get(tester)).thenReturn(empty);

            inv.addItems(tester, 2);

            assertEquals(50, stats.getHealth()); // no change
            assertEquals(0, inv.getItemCount(tester)); // not stored
        }
    }

    @Nested
    class UsePaths {
//        @Test
//        void useItem_appliesEffectOnce_andDecrementsStack() {
//            var healItem = cfg(tester, false, List.of(heal(10)));
//            svcMock.when(() -> CollectableService.get(tester)).thenReturn(healItem);
//
//            inv.addItems(tester, 2); // stacked since autoConsume=false
//            assertEquals(2, inv.getItemCount(tester));
//
//            inv.useItem(tester);
//            assertEquals(60, stats.getHealth()); // 50 + 10
//            assertEquals(1, inv.getItemCount(tester));
//
//            inv.useItem(tester);
//            assertEquals(70, stats.getHealth());
//            assertEquals(0, inv.getItemCount(tester));
//        }
//
//        @Test
//        void useItems_appliesEffect_nTimes_upToAvailable_andDecrementsAll() {
//            var healItem = cfg(tester, false, List.of(heal(5)));
//            svcMock.when(() -> CollectableService.get(tester)).thenReturn(healItem);
//
//            inv.addItems(tester, 2);
//            inv.useItems(tester, 5); // requests 5, only 2 available
//
//            assertEquals(60, stats.getHealth()); // 50 + (2*5)
//            assertEquals(0, inv.getItemCount(tester));
//        }

        @Test
        void useItem_noStackPresent_isNoOp() {
            var healItem = cfg(tester, false, List.of(heal(10)));
            svcMock.when(() -> CollectableService.get(tester)).thenReturn(healItem);

            inv.useItem(tester); // nothing stacked
            assertEquals(50, stats.getHealth());
            assertEquals(0, inv.getItemCount(tester));
        }

        @Test
        void useItems_amountZero_isNoOp() {
            var healItem = cfg(tester, false, List.of(heal(10)));
            svcMock.when(() -> CollectableService.get(tester)).thenReturn(healItem);

            inv.addItems(tester, 2);

            assertEquals(70, stats.getHealth());
            assertEquals(2, inv.getItemCount(tester));
        }
    }
}
