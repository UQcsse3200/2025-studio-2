package com.csse3200.game.components.player;

import com.csse3200.game.entities.configs.CollectablesConfig;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

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
            assertThrows(IllegalArgumentException.class, () -> inv.addItems("pink-key", 0));
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