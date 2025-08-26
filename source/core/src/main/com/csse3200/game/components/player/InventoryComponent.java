package com.csse3200.game.components.player;

import com.csse3200.game.components.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * A component to track and manage a player's inventory, including stacking items.
 * Inventory is set up as a multiset of item ids (stack counts).
 */
public class InventoryComponent extends Component {
    private final Map<String, Integer> inventory;

    public InventoryComponent() {
        this.inventory = new HashMap<>();
    }

    /**
     * Adds an item to the inventory
     *
     * @apiNote If item not present, it is inserted with a count of one.
     * @param itemId non-null identifier (e.g., {@code "key:door"}).
     */
    public void addItem(String itemId) {
        inventory.put(itemId, inventory.getOrDefault(itemId, 0) + 1);
    }

    /**
     * Adds {@code amount} instances of the given item id (stacking).
     *
     * @apiNote If item not present, it is inserted with a count of amount.
     * @param itemId non-null identifier (e.g., {@code "key:door"}).
     * @param amount number of items to add; where amount >= 0.
     */
    public void addItems(String itemId, int amount) {
        inventory.put(itemId, inventory.getOrDefault(itemId, 0) + amount);
    }

    /**
     * Returns whether at least one instance of {@code itemId} exists in the inventory.
     *
     * @param itemId non-null identifier.
     * @return {@code true} if the count is &gt; 0; otherwise {@code false}.
     */
    public boolean hasItem(String itemId) {
        return getItemCount(itemId) > 0;
    }

    /**
     * Gets the current stack count for {@code itemId}.
     *
     * @param itemId non-null identifier.
     * @return the stored count, or {@code 0} if the item is absent
     */
    public int getItemCount(String itemId) {
        return inventory.getOrDefault(itemId, 0);
    }


    /**
     * Completely removes the entry for {@code itemId} from the inventory.
     *
     * @param itemId non-null identifier to remove.
     */
    public void removeItem(String itemId) {
        inventory.remove(itemId);
    }


    /**
     * Uses one instance of {@code itemId}: decrements its count by {@code 1} if present.
     *
     * @param itemId non-null identifier to decrement.
     */
    public void useItem(String itemId) {
        if (inventory.get(itemId) != null && inventory.get(itemId) > 0) {
            inventory.put(itemId, inventory.get(itemId) - 1);
        } else {
            System.err.println("You have no " + itemId + " in your inventory");
        }
    }
}