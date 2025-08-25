package com.csse3200.game.components.player;

import com.csse3200.game.components.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A component to track and manage a player's inventory, including stacking items.
 */
public class InventoryComponent extends Component {
    private final Map<String, Integer> inventory;

    public InventoryComponent() {
        this.inventory = new HashMap<>();
    }

    public void addItem(String itemId) {
        inventory.put(itemId, inventory.getOrDefault(itemId, 0) + 1);
    }

    public void addItems(String itemId, int amount) {
        inventory.put(itemId, inventory.getOrDefault(itemId, 0) + amount);
    }

    public boolean hasItem(String itemId) {
        return getItemCount(itemId) > 0;
    }

    public int getItemCount(String itemId) {
        return inventory.getOrDefault(itemId, 0);
    }

    public void removeItem(String itemId) {
        inventory.remove(itemId);
    }

    public void useItem(String itemId) {
        if (inventory.get(itemId) != null && inventory.get(itemId) > 0) {
            inventory.put(itemId, inventory.get(itemId) - 1);
        } else {
            System.err.println("You have no " + itemId + " in your inventory");
        }
    }
}