package com.csse3200.game.components.player;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(GameExtension.class)
class InventoryComponentTest {

    private InventoryComponent inv;

    @BeforeEach
    void setUp() {
        inv = new InventoryComponent();
    }

    void addItemsBulk() {
        inv.addItems("pink-key", 8);
        inv.addItems("purple-key", 3);
        inv.addItems("blue-key", 2);
        inv.addItems("red-key", 8);
        inv.addItems("orange-key", 0);
    }

    @Nested
    class Construction {
        @Test
        void startsWithEmptyInventory() {
            assertEquals(0, inv.getTotalItemCount());
            assertFalse(inv.hasItem("pink-key"));
            assertEquals(0, inv.getItemCount("pink-key"));
        }
    }

    @Nested
    class AddItemStack {
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
            inv.addItems("pink-key", 0);
            assertEquals(0, inv.getItemCount("pink-key"));
            assertFalse(inv.hasItem("pink-key"));
        }
    }

    @Nested
    class RemoveItem {
        @Test
        void removesItems() {
            addItemsBulk();
            inv.removeItem("pink-key");
            inv.removeItem("purple-key");
            assertEquals(0, inv.getItemCount("pink-key"));
            assertEquals(0, inv.getItemCount("purple-key"));
            assertEquals(10, inv.getTotalItemCount());
        }

        @Test
        void useMultipleItems() {
            addItemsBulk();
            inv.useItems("pink-key", 5);
            assertEquals(3, inv.getItemCount("pink-key"));
            assertEquals(3, inv.getItemCount("purple-key"));
        }

        @Test
        void useNonExistentItemInstance() {
            inv.useItems("pink-key", 12);
            assertEquals(0, inv.getTotalItemCount());
            assertFalse(inv.hasItem("pink-key"));

            inv.useItems("heart", 2);
            assertFalse(inv.hasItem("heart"));
            assertEquals(0, inv.getItemCount("heart"));

            assertFalse(inv.hasItem("red-key"));
            inv.useItems("red-key", 5);
            assertEquals(0, inv.getItemCount("red-key"));
        }

        @Test
        void usesItemInstance() {
            addItemsBulk();
            inv.useItem("purple-key");
            assertEquals(2, inv.getItemCount("purple-key"));
        }

        @Test
        void useDecrementsItemCount() {
            addItemsBulk();
            inv.useItem("purple-key");
            assertEquals(2, inv.getItemCount("purple-key"));
        }

        @Test
        void useItemDecrementsSelectedOnly() {
            addItemsBulk();
            inv.useItem("purple-key");
            assertEquals(2, inv.getItemCount("purple-key"));
            assertEquals(8, inv.getItemCount("red-key"));
            assertEquals(20, inv.getTotalItemCount());
        }
    }
}