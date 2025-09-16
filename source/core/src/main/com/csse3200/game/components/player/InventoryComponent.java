package com.csse3200.game.components.player;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.collectables.effects.ItemEffectRegistry;
import com.csse3200.game.entities.configs.CollectablesConfig;
import com.csse3200.game.services.CollectableService;

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

    public InventoryComponent(InventoryComponent other) {
        this.inventory = other.inventory;
    }

    /**
     * Adds one instance of the given item to the inventory.
     * <p>
     * If the item's config has {@code autoConsume == true}, its effects are applied
     * immediately and the item is not stored. Otherwise the item is added to the
     * inventory stack; if it is not yet present, a new stack is created with count 1.
     * </p>
     *
     * @param itemId non-null identifier (e.g., {@code "key:red"}).
     */
    public void addItem(String itemId) {
        addItems(itemId, 1);
    }

    /**
     * Adds {@code amount} instances of the given item (stacking).
     * <p>
     * If {@code autoConsume == true}, effects are applied once per instance and nothing
     * is stored. Otherwise the inventory count is increased by {@code amount}, creating
     * a new stack if needed.
     * </p>
     *
     * @param itemId non-null identifier (e.g., {@code "key:red"}).
     * @param amount number of items to add (must be ≥ 1).
     * @throws IllegalArgumentException if {@code amount < 1}.
     */

    public void addItems(String itemId, int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("Amount must be at least 1");
        }

        var cfg = CollectableService.get(itemId);
        if (cfg == null) return;

        if (cfg.autoConsume) {
            for (int i = 0; i < amount; i++) {
                applyEffects(cfg);
            }
        } else {
            inventory.put(itemId, inventory.getOrDefault(itemId, 0) + amount);
        }
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
     * Decrements inventory item count by 1 if the item is present
     * in the inventory and the number of items is &gt; 0. Also
     * applies its effects.s
     * </p>
     *
     * @param itemId non-null identifier of item to decrement.
     */
    public void useItem(String itemId) {
        if (inventory.get(itemId) != null && inventory.get(itemId) > 0) {
            applyEffects(CollectableService.get(itemId));
            inventory.put(itemId, inventory.get(itemId) - 1);
        }
    }

    /**
     * Consumes a specified number of items from the inventory.
     * <p>
     * This method will decrement the count of the given {@code itemId} until either
     * the requested {@code amount} has been used or the available quantity is depleted.
     * If the item does not exist in the inventory or has a count of zero, no changes occur.
     *
     * Looks up the item's config and applies its effects. If no effect can be
     * applied (e.g., using a heart at full HP), the item is not consumed.
     * </p>
     *
     * @param itemId the identifier of the item to use
     * @param amount the number of items to attempt to consume; if greater than the
     *               available count, all available items are consumed
     */
    public void useItems(String itemId,  int amount) {
        var cfg = CollectableService.get(itemId);
        if (cfg == null || amount <= 0) return;


        if (inventory.get(itemId) != null && inventory.get(itemId) > 0) {
            int numToUse = Math.min(amount, inventory.get(itemId));
            for (int i = 0; i < numToUse; i++) {
                applyEffects(cfg);
                inventory.put(itemId, inventory.get(itemId) - 1);

            }
        }
    }

    /**
     * Applies all effects defined in the collectables' config
     * <p>
     * Each effect is looked up in the {@link ItemEffectRegistry} and executed if a handler
     * is registered. Unknown effect types are ignored.
     * </p>
     *
     * @param cfg the config containing effects to apply (maybe empty).
     */

    private void applyEffects(CollectablesConfig cfg) {
        if (cfg.effects == null || cfg.effects.isEmpty()) {
            return;
        }
        for (var effect : cfg.effects) {
            var handler = ItemEffectRegistry.get(effect.type);
            if (handler != null) {
                handler.apply(getEntity(), effect);
            }
        }
    }

}