package com.csse3200.game.components.player;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(GameExtension.class)
class InventoryComponentTest {
  @Test
  void addItem_addsNewItemInstance() {
      InventoryComponent inv = new InventoryComponent();
      assertFalse(inv.hasItem("key:door"));
      assertEquals(0, inv.getItemCount("key:door"));
      assertFalse(inv.hasItem("key:door"));

      inv.addItem("key:door");
      assertTrue(inv.hasItem("key:door"));
      assertEquals(1, inv.getItemCount("key:door"));

      inv.addItem("key:door");
      assertTrue(inv.hasItem("key:door"));
      assertEquals(2, inv.getItemCount("key:door"));
  }
  @Test
  void removeItem_removesAllItemOfKey() {
      InventoryComponent inv = new InventoryComponent();
      inv.addItem("key:door");
      inv.removeItem("key:door");
      assertFalse(inv.hasItem("key:door"));
  }
  @Test
  void useItem_decrementsItemCount () {
      InventoryComponent inv = new InventoryComponent();
      inv.useItem("key:door");
      assertFalse(inv.hasItem("key:door"));

      inv.addItems("key:door", 4);
      assertTrue(inv.hasItem("key:door"));
      assertEquals(4, inv.getItemCount("key:door"));

      inv.useItem("key:door");
      assertEquals(3, inv.getItemCount("key:door"));
  }

}