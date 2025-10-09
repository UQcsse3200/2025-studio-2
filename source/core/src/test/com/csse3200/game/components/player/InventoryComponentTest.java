package com.csse3200.game.components.player;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.entities.configs.CollectablesConfig;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.CollectableService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

import com.csse3200.game.components.player.InventoryComponent.Bag;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the three-bag InventoryComponent:
 * - inventory  (consumable items, supports use/remove)
 * - upgrades   (non-consumable flags, e.g., jetpack/glider)
 * - objectives (non-consumable, cleared via resetObjectives)
 *
 * NOTE: We avoid legacy methods like getTotalItemCount() and assert by summing
 * the exposed read-only maps returned by getInventory()/getUpgrades()/getObjectives().
 */
@ExtendWith(GameExtension.class)
class InventoryComponentTest {

    private InventoryComponent inv;

    private MockedStatic<CollectableService> svcMock;

    private static CollectablesConfig cfg(String id) {
        var c = new CollectablesConfig();
        c.id = id;
        c.sprite = "";
        c.effects = List.of();
        return c;
    }

    @BeforeEach
    void setUp() {
        inv = new InventoryComponent();
        svcMock = Mockito.mockStatic(CollectableService.class);
        svcMock.when(() -> CollectableService.get(anyString()))
                .thenAnswer(invocation -> cfg(invocation.getArgument(0)));

        ResourceService rs = mock(ResourceService.class);
        when(rs.getAsset(anyString(), eq(Sound.class))).thenReturn(mock(Sound.class));

        ServiceLocator.registerResourceService(rs);
    }
    @AfterEach
    void tearDown() {
        if (svcMock != null) svcMock.close();
    }

    void addItemsBulk() {
        inv.addItems("pink-key", 8);
        inv.addItems("purple-key", 3);
        inv.addItems("blue-key", 2);
        inv.addItems("red-key", 8);
    }

  private static int sum(Map<String, Integer> m) {
    return m.values().stream().mapToInt(Integer::intValue).sum();
  }

    // --------------------------
    // Construction / empty state
    // --------------------------
  @Nested
    class Construction {
        @Test
        void startsEmptyAcrossAllBags() {
            assertTrue(inv.getInventory().isEmpty());
            assertTrue(inv.getUpgrades().isEmpty());
            assertTrue(inv.getObjectives().isEmpty());

            assertEquals(0, inv.getTotalCount(Bag.INVENTORY));
            assertEquals(0, inv.getTotalCount(Bag.UPGRADES));
            assertEquals(0, inv.getTotalCount(Bag.OBJECTIVES));
            assertEquals(0, inv.getGrandTotalCount());
        }

        @Test
        void copyConstructorDeepCopies() {
            inv.addItems(Bag.INVENTORY, "pink-key", 2);
            inv.addItems(Bag.UPGRADES, "jetpack", 1);
            inv.addItems(Bag.OBJECTIVES, "find-door", 3);

            InventoryComponent copy = new InventoryComponent(inv);

            // mutate original
            inv.addItems(Bag.INVENTORY, "pink-key", 5);
            inv.removeItem(Bag.UPGRADES, "jetpack");
            inv.resetBag(Bag.OBJECTIVES);

            // copy remains with original values
            assertEquals(2, copy.getItemCount(Bag.INVENTORY, "pink-key"));
            assertEquals(1, copy.getItemCount(Bag.UPGRADES, "jetpack"));
            assertEquals(3, copy.getItemCount(Bag.OBJECTIVES, "find-door"));
        }
    }

    // -------------
    // Add / stack
    // -------------
    @Nested
    class AddAndStack {
        @Test
        void addSingleAndBulkPerBag() {
            inv.addItem(Bag.INVENTORY, "pink-key");
            inv.addItems(Bag.INVENTORY, "purple-key", 2);
            inv.addItems(Bag.UPGRADES, "jetpack", 1);
            inv.addItems(Bag.OBJECTIVES, "find-door", 3);

            assertEquals(1, inv.getItemCount(Bag.INVENTORY, "pink-key"));
            assertEquals(2, inv.getItemCount(Bag.INVENTORY, "purple-key"));
            assertEquals(1, inv.getItemCount(Bag.UPGRADES, "jetpack"));
            assertEquals(3, inv.getItemCount(Bag.OBJECTIVES, "find-door"));

            assertEquals(3, inv.getTotalCount(Bag.INVENTORY));
            assertEquals(1, inv.getTotalCount(Bag.UPGRADES));
            assertEquals(3, inv.getTotalCount(Bag.OBJECTIVES));
            assertEquals(7, inv.getGrandTotalCount());
        }

        @Test
        void stackingSameKeyAccumulates() {
            inv.addItems(Bag.INVENTORY, "pink-key", 3);
            inv.addItem(Bag.INVENTORY, "pink-key");
            assertEquals(4, inv.getItemCount(Bag.INVENTORY, "pink-key"));
        }

        @Test
        void addNegativeThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> inv.addItems(Bag.INVENTORY, "pink-key", -1));
            assertThrows(IllegalArgumentException.class,
                    () -> inv.addItems(Bag.UPGRADES, "jetpack", -2));
            assertThrows(IllegalArgumentException.class,
                    () -> inv.addItems(Bag.OBJECTIVES, "find-door", -3));
        }

      @Test
      void addsItems() {
        inv.addItems("pink-key", 3);
        inv.addItems("purple-key", 2);
        assertEquals(3, inv.getItemCount("pink-key"));
        assertEquals(2, inv.getItemCount("purple-key"));
        assertEquals(5, inv.getTotalItemCount());
      }

      @Test
      void stacksSameItem() {
        inv.addItems("pink-key", 3);
        inv.addItems("pink-key", 2);
        inv.addItem("pink-key");
        inv.addItems("purple-key", 2);
        assertEquals(6, inv.getItemCount("pink-key"));
        assertEquals(8, inv.getTotalItemCount());
      }

      @Test
      void stacksDifferentItem() {
        inv.addItems("pink-key", 3);
        inv.addItems("purple-key", 2);
        assertEquals(3, inv.getItemCount("pink-key"));
        assertEquals(2, inv.getItemCount("purple-key"));
        assertEquals(5, inv.getTotalItemCount());
      }

      @Test
      void hasItemsIndependently() {
        inv.addItems("pink-key", 3);
        inv.addItems("purple-key", 2);
        assertTrue(inv.hasItem("pink-key"));
        assertTrue(inv.hasItem("purple-key"));
      }

      @Test
      void addZeroItems() {
        assertThrows(IllegalArgumentException.class, () -> inv.addItems("pink-key", 0));
        assertEquals(0, inv.getItemCount("pink-key"));
        assertFalse(inv.hasItem("pink-key"));
      }

    }

    // -------------
    // Queries
    // -------------
    @Nested
    class Queries {
        @Test
        void hasItemAndCountsWorkPerBag() {
            inv.addItems(Bag.INVENTORY, "pink-key", 2);
            assertTrue(inv.hasItem(Bag.INVENTORY, "pink-key"));
            assertFalse(inv.hasItem(Bag.INVENTORY, "ghost"));
            assertEquals(2, inv.getItemCount(Bag.INVENTORY, "pink-key"));
            assertEquals(0, inv.getItemCount(Bag.INVENTORY, "ghost"));
        }

        @Test
        void existsAnywhereChecksAllBags() {
            inv.addItems(Bag.UPGRADES, "jetpack", 1);
            assertTrue(inv.existsAnywhere("jetpack"));
            assertFalse(inv.existsAnywhere("glider"));

            inv.addItems(Bag.OBJECTIVES, "glider", 1);
            assertTrue(inv.existsAnywhere("glider"));
        }
    }

    // -------------------
    // Remove / reset bag
    // -------------------
    @Nested
    class RemoveAndReset {
        @Test
        void removeItemDeletesAllInstances() {
            inv.addItems(Bag.INVENTORY, "pink-key", 5);
            inv.removeItem(Bag.INVENTORY, "pink-key");
            assertEquals(0, inv.getItemCount(Bag.INVENTORY, "pink-key"));
            assertEquals(0, inv.getTotalCount(Bag.INVENTORY));
        }

        @Test
        void resetBagClearsOnlyThatBag() {
            inv.addItems(Bag.INVENTORY, "pink-key", 2);
            inv.addItems(Bag.UPGRADES, "jetpack", 1);
            inv.addItems(Bag.OBJECTIVES, "find-door", 3);

            inv.resetBag(Bag.OBJECTIVES);

            assertEquals(2, inv.getTotalCount(Bag.INVENTORY));
            assertEquals(1, inv.getTotalCount(Bag.UPGRADES));
            assertEquals(0, inv.getTotalCount(Bag.OBJECTIVES));
            assertEquals(3, inv.getGrandTotalCount());
        }
    }

    // -------------
    // Consumption
    // -------------
    @Nested
    class Consumption {
        @Test
        void useItemConsumesOneIfAvailable() {
            inv.addItems(Bag.INVENTORY, "pink-key", 2);
            assertTrue(inv.useItem(Bag.INVENTORY, "pink-key"));
            assertEquals(1, inv.getItemCount(Bag.INVENTORY, "pink-key"));
        }

        @Test
        void useItemsClampedToAvailable() {
            inv.addItems(Bag.INVENTORY, "pink-key", 3);
            int used = inv.useItems(Bag.INVENTORY, "pink-key", 5);
            assertEquals(3, used);
            assertEquals(0, inv.getItemCount(Bag.INVENTORY, "pink-key"));
        }

        @Test
        void useItemsZeroAmountIsNoop() {
            inv.addItems(Bag.INVENTORY, "pink-key", 3);
            int used = inv.useItems(Bag.INVENTORY, "pink-key", 0);
            assertEquals(0, used);
            assertEquals(3, inv.getItemCount(Bag.INVENTORY, "pink-key"));
        }

        @Test
        void useItemsNegativeThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> inv.useItems(Bag.INVENTORY, "pink-key", -1));
        }

        @Test
        void useFromEmptyReturnsFalseOrZero() {
            assertFalse(inv.useItem(Bag.INVENTORY, "ghost-key"));
            assertEquals(0, inv.useItems(Bag.INVENTORY, "ghost-key", 3));
        }
    }

    // --------------------
    // Getters
    // --------------------
    @Nested
    class Getters {
        @Test
        void mapsAreUnmodifiable() {
            inv.addItems(Bag.INVENTORY, "pink-key", 1);
            assertThrows(UnsupportedOperationException.class,
                    () -> inv.getInventory().put("hack", 999));
            assertThrows(UnsupportedOperationException.class,
                    () -> inv.getUpgrades().put("hack", 999));
            assertThrows(UnsupportedOperationException.class,
                    () -> inv.getObjectives().put("hack", 999));
        }

        @Test
        void gettersAreCopies() {
            // Original inv should not contain changes made to copies
            Map<String, Integer> inventoryCopy = inv.getInventoryCopy();
            inventoryCopy.put("test", 3);
            assertFalse(inv.getInventoryCopy().containsKey("test"));

            Map<String, Integer> upgradesCopy = inv.getUpgradesCopy();
            upgradesCopy.put("test2", 8);
            assertFalse(inv.getUpgradesCopy().containsKey("test"));
        }
    }

    @Nested
    class Setters {
        @Test
        void settersWork() {
            Map<String, Integer> inventoryTest = new HashMap<String, Integer>();
            inventoryTest.put("test", 5);
            inventoryTest.put("test2", 9);
            inv.setInventory(inventoryTest);

            Map<String, Integer> upgradesTest = new HashMap<String, Integer>();
            upgradesTest.put("test3", 2);
            upgradesTest.put("test4", 10);
            inv.setUpgrades(upgradesTest);

            // Keys and values should match
            assertEquals(inv.getInventory().keySet(), inventoryTest.keySet());

            assertEquals(inv.getUpgrades().keySet(), upgradesTest.keySet());

            // Any changes after the set should not be registered
            inventoryTest.put("test5", 4);
            assertFalse(inv.getInventory().containsKey("test5"));

            upgradesTest.put("test6", 1);
            assertFalse(inv.getUpgrades().containsKey("test6"));
        }
    }

    // ----------------
    // Null parameters
    // ----------------
    @Nested
    class NullGuards {
        @Test
        void nullBagOrItemIdThrows() {
            assertThrows(NullPointerException.class, () -> inv.addItem(null, "x"));
            assertThrows(NullPointerException.class, () -> inv.addItem(Bag.INVENTORY, null));
            assertThrows(NullPointerException.class, () -> inv.addItems(null, "x", 1));
            assertThrows(NullPointerException.class, () -> inv.addItems(Bag.INVENTORY, null, 1));
            assertThrows(NullPointerException.class, () -> inv.getItemCount(null, "x"));
            assertThrows(NullPointerException.class, () -> inv.getItemCount(Bag.INVENTORY, null));
            assertThrows(NullPointerException.class, () -> inv.getTotalCount(null));
            assertThrows(NullPointerException.class, () -> inv.removeItem(null, "x"));
            assertThrows(NullPointerException.class, () -> inv.removeItem(Bag.INVENTORY, null));
            assertThrows(NullPointerException.class, () -> inv.resetBag(null));
            assertThrows(NullPointerException.class, () -> inv.useItem(null, "x"));
            assertThrows(NullPointerException.class, () -> inv.useItem(Bag.INVENTORY, null));
            assertThrows(NullPointerException.class, () -> inv.useItems(null, "x", 1));
            assertThrows(NullPointerException.class, () -> inv.useItems(Bag.INVENTORY, null, 1));
            assertThrows(NullPointerException.class, () -> inv.existsAnywhere(null));
            // copy constructor guard
            assertThrows(NullPointerException.class, () -> new InventoryComponent(null));
        }
    }
}