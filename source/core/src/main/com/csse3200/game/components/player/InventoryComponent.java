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
     * <p>
     *     If item not present in inventory, the new item is created
     *     and inserted into the inventory with a count of 1.
     * </p>
     *
     * @param itemId non-null identifier (e.g., {@code "key:door"}).
     */
    public void addItem(String itemId) {
        inventory.put(itemId, inventory.getOrDefault(itemId, 0) + 1);
    }

    /**
     * Adds {@code amount} instances of the given item id (stacking).
     * <p>
     *      If item not present in inventory, the new item is created
     *      and inserted into the inventory with a count of amount.
     * </p>
     *
     * @param itemId non-null identifier (e.g., {@code "key:door"}).
     * @param amount number of items to add.
     * @throws IllegalArgumentException when amount is negative
     */
    public void addItems(String itemId, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        inventory.put(itemId, inventory.getOrDefault(itemId, 0) + amount);
    }

    /**
     * Returns whether at least one instance of {@code itemId} exists in the inventory.
     *
     * @param itemId non-null identifier of item.
     * @return {@code true} if the count is &gt; 0; otherwise {@code false}.
     */
    public boolean hasItem(String itemId) {
        return getItemCount(itemId) > 0;
    }

    /**
     * Gets the current stack count for {@code itemId}, where the stack count
     * is the number of instances of an item in the inventory.
     *
     * @param itemId non-null identifier of item.
     * @return the stored count, or {@code 0} if the item is absent
     */
    public int getItemCount(String itemId) {
        return inventory.getOrDefault(itemId, 0);
    }

    /**
     * Gets the count for all instances of items in the inventory.
     *
     * @return the stored count, or {@code 0} if the inventory is empty
     */
    public int getTotalItemCount() {
        int total = 0;
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            total += entry.getValue();
        }
        return total;
    }

    /**
     * Removes all items with {@code itemId} from the inventory.
     *
     * @param itemId non-null identifier to remove.
     */
    public void removeItem(String itemId) {
        inventory.remove(itemId);
    }

    /**
     * Uses one instance of {@code itemId}.
     * <p>
     *     Decrements inventory item count by 1 if the item is present
     *     in the inventory and the number of items is &gt; 0.
\     * </p>
     *
     * @param itemId non-null identifier of item to decrement.
     */
    public void useItem(String itemId) {
        if (inventory.get(itemId) != null && inventory.get(itemId) > 0) {
            inventory.put(itemId, inventory.get(itemId) - 1);
        } else {
            System.err.println("You have no " + itemId + " in your inventory");
        }
    }

    /**
     * Consumes a specified number of items from the inventory.
     * <p>
     * This method will decrement the count of the given {@code itemId} until either
     * the requested {@code amount} has been used or the available quantity is depleted.
     * If the item does not exist in the inventory or has a count of zero, no changes occur.
     * </p>
     *
     * @param itemId the identifier of the item to use
     * @param amount the number of items to attempt to consume; if greater than the
     *               available count, all available items are consumed
     */
    public void useItems(String itemId,  int amount) {
        int i = amount;
        if (inventory.get(itemId) != null && inventory.get(itemId) > 0) {
            while (inventory.get(itemId) > 0 && i > 0) {
                inventory.put(itemId, inventory.get(itemId) - 1);
                i--;
            }
        }
    }
}