package com.csse3200.game.components.player;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.collectables.effects.ItemEffectRegistry;
import com.csse3200.game.entities.configs.CollectablesConfig;
import com.csse3200.game.services.CollectableService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tracks and manages a player's possessions, split into three logical bags:
 * - INVENTORY   : regular items picked up during gameplay (keys, etc.)
 * - UPGRADES    : upgrade tokens/items
 * - OBJECTIVES  : quest/goal items
 *
 * Each bag is a multiset (item id -> stack count).
 */
public class InventoryComponent extends Component {

    /** Logical groupings for items held by the player. */
    public enum Bag { INVENTORY, UPGRADES, OBJECTIVES }

    /** Regular inventory items. */
    private final Map<String, Integer> inventory;
    /** Upgrade items. */
    private final Map<String, Integer> upgrades;
    /** Objective/quest items. */
    private final Map<String, Integer> objectives;

    /** Creates a new, empty component with three empty bags. */
    public InventoryComponent() {
        this.inventory = new HashMap<>();
        this.upgrades = new HashMap<>();
        this.objectives = new HashMap<>();
    }

    /**
     * Copy constructor.
     * Creates deep copies of the three internal maps.
     *
     * @param other another InventoryComponent to copy
     * @throws NullPointerException if other is null
     */
    public InventoryComponent(InventoryComponent other) {
        if (other == null) throw new NullPointerException("Passed a null component");
        this.inventory = new HashMap<>(other.inventory);
        this.upgrades = new HashMap<>(other.upgrades);
        this.objectives = new HashMap<>(other.objectives);
    }

    // Read-only views (preferred getters)

    /**
     * @return unmodifiable view of the INVENTORY bag
     */
    public Map<String, Integer> getInventory() {
        return Collections.unmodifiableMap(inventory);
    }

    /**
     * @return unmodifiable view of the UPGRADES bag
     */
    public Map<String, Integer> getUpgrades() {
        return Collections.unmodifiableMap(upgrades);
    }

    /**
     * @return unmodifiable view of the OBJECTIVES bag
     */
    public Map<String, Integer> getObjectives() {
        return Collections.unmodifiableMap(objectives);
    }

    // Copy getters

    /**
     * @return copy of the INVENTORY bag
     */
    public Map<String, Integer> getInventoryCopy() {
        return new HashMap<>(inventory);
    }

    /**
     * @return copy of the UPGRADES bag
     */
    public Map<String, Integer> getUpgradesCopy() {
        return new HashMap<>(upgrades);
    }

    // Setters (Should be used with great caution, currently only here for saving/loading)

    /**
     * Add all items from a passed inventory.
     * @param inventory - passed inventory.
     */
    public void setInventory(Map<String, Integer> inventory) {
        this.inventory.putAll(inventory);
    }

    /**
     * Add all passed upgrades.
     * @param upgrades - passed upgrades.
     */
    public void setUpgrades(Map<String, Integer> upgrades) {
        this.upgrades.putAll(upgrades);
    }

    // Generic bag operations (recommended API)

    /**
     * Adds one instance of the given item to the inventory.
     * <p>
     * If the item's config has {@code autoConsume == true}, its effects are applied
     * immediately and the item is not stored. Otherwise the item is added to the
     * inventory stack; if it is not yet present, a new stack is created with count 1.
     * </p>
     *
     * @param bag which bag to modify
     * @param itemId non-null item identifier (e.g., "key:door")
     * @throws NullPointerException if bag or itemId is null
     */
    public void addItem(Bag bag, String itemId) {
    addItems(bag, itemId, 1);
  }

    /**
     * Adds {@code amount} instances of itemId to the specified bag.
     *
     * @param bag which bag to modify
     * @param itemId non-null item identifier
     * @param amount number of instances to add (must be >= 0)
     * @throws NullPointerException if bag or itemId is null
     * @throws IllegalArgumentException if amount is negative
     */
    public void addItems(Bag bag, String itemId, int amount) {
        if (bag == null)    throw new NullPointerException("bag");
        if (itemId == null) throw new NullPointerException("itemId");
        if (amount <= 0)     throw new IllegalArgumentException("Amount cannot be negative");

        Map<String, Integer> map = mapFor(bag);

        // CollectableService should only run on inventory items, not upgrades or objectives
        if (bag == Bag.INVENTORY) {
            var cfg = CollectableService.get(itemId);
            if (cfg == null) return;

            if (cfg.autoConsume) {
                for (int i = 0; i < amount; i++) {
                    applyEffects(cfg);
                }
            } else {
                map.put(itemId, map.getOrDefault(itemId, 0) + amount);
            }
        }
        else {
            map.put(itemId, map.getOrDefault(itemId, 0) + amount);
        }
    }

    /**
     * @param bag which bag to query
     * @param itemId non-null identifier
     * @return true if the bag contains at least one instance
     * @throws NullPointerException if bag or itemId is null
     */
    public boolean hasItem(Bag bag, String itemId) {
        return getItemCount(bag, itemId) > 0;
    }

    /**
     * Returns the stack count for itemId in the given bag.
     *
     * @param bag which bag to query
     * @param itemId non-null identifier
     * @return count (0 if absent)
     * @throws NullPointerException if bag or itemId is null
     */
    public int getItemCount(Bag bag, String itemId) {
        if (bag == null)    throw new NullPointerException("bag");
        if (itemId == null) throw new NullPointerException("itemId");
        return mapFor(bag).getOrDefault(itemId, 0);
    }

    /**
     * @param bag which bag to query
     * @return total count of all instances stored in the bag
     * @throws NullPointerException if bag is null
     */
    public int getTotalCount(Bag bag) {
        if (bag == null) throw new NullPointerException("bag");
        int total = 0;
        for (int v : mapFor(bag).values()) total += v;
        return total;
    }

    /**
     * Removes all instances of itemId from the specified bag.
     *
     * @param bag which bag to modify
     * @param itemId non-null identifier to remove
     * @throws NullPointerException if bag or itemId is null
     */
    public void removeItem(Bag bag, String itemId) {
        if (bag == null)    throw new NullPointerException("bag");
        if (itemId == null) throw new NullPointerException("itemId");
        mapFor(bag).remove(itemId);
    }

    /**
     * Clears all items from the specified bag.
     * Uses one instance of {@code itemId}.
     * <p>
     * Decrements inventory item count by 1 if the item is present
     * in the inventory and the number of items is &gt; 0. Also
     * applies its effect(s)
     * </p>
     *
     * @param bag which bag to clear
     * @throws NullPointerException if bag is null
     */
    public void resetBag(Bag bag) {
        if (bag == null) throw new NullPointerException("bag");
        mapFor(bag).clear();
    }

    /**
     * Consumes one instance of itemId from the specified bag (if present).
     *
     * @param bag which bag to modify
     * @param itemId non-null identifier to decrement
     * @return true if one instance was consumed; false if none were available
     * @throws NullPointerException if bag or itemId is null
     */
    public boolean useItem(Bag bag, String itemId) {
        return useItems(bag, itemId, 1) > 0;
    }

    /**
     * Consumes up to "amount" instances of itemId from the specified bag
     * Consumes a specified number of items from the inventory.
     * <p>
     * This method will decrement the count of the given {@code itemId} until either
     * the requested {@code amount} has been used or the available quantity is depleted.
     * If the item does not exist in the inventory or has a count of zero, no changes occur.
     * Looks up the item's config and applies its effects. If no effect can be
     * applied (e.g., using a heart at full HP), the item is not consumed.
     * </p>
     *
     * @param bag which bag to modify
     * @param itemId non-null identifier to decrement
     * @param amount desired number to consume (must be >= 0)
     * @return the number of instances actually consumed (0..amount)
     * @throws NullPointerException if bag or itemId is null
     * @throws IllegalArgumentException if amount is negative
     */
    public int useItems(Bag bag, String itemId, int amount) {
        if (bag == null)    throw new NullPointerException("bag");
        if (itemId == null) throw new NullPointerException("itemId");
        if (amount < 0)     throw new IllegalArgumentException("Amount cannot be negative");

        Map<String, Integer> map = mapFor(bag);
        int have = map.getOrDefault(itemId, 0);
        if (have <= 0) return 0;

        int used = Math.min(have, amount);
        int remaining = have - used;
        if (remaining >= 0) {
            var cfg = CollectableService.get(itemId);
            if (cfg == null) return 0;

            if (map.get(itemId) != null && map.get(itemId) > 0) {
                for (int i = 0; i < used; i++) {
                    applyEffects(cfg);
                }
            }

            map.put(itemId, remaining);
        }
        else {
            map.remove(itemId);
        }

        return used;
    }

    // Bag helpers

    /**
     * Returns true if the item exists in ANY bag (count > 0).
     *
     * @param itemId non-null identifier
     * @return true if present in inventory OR upgrades OR objectives
     * @throws NullPointerException if itemId is null
     */
    public boolean existsAnywhere(String itemId) {
        if (itemId == null) throw new NullPointerException("itemId");
        return hasItem(Bag.INVENTORY, itemId)
                || hasItem(Bag.UPGRADES, itemId)
                || hasItem(Bag.OBJECTIVES, itemId);
    }

    /**
     * @return total count across all bags (inventory + upgrades + objectives)
     */
    public int getGrandTotalCount() {
        return getTotalCount(Bag.INVENTORY)
                + getTotalCount(Bag.UPGRADES)
                + getTotalCount(Bag.OBJECTIVES);
    }

    // Backward compatibility: treat "inventory" as the default bag

    /** @deprecated Prefer bagged version: addItem(Bag.INVENTORY, itemId). */
    @Deprecated public void addItem(String itemId) {
        addItem(Bag.INVENTORY, itemId);
    }

    /** @deprecated Prefer bagged version: addItems(Bag.INVENTORY, itemId, amount). */
    @Deprecated public void addItems(String itemId, int amount) {
        addItems(Bag.INVENTORY, itemId, amount);
    }

    /** @deprecated Prefer bagged version: hasItem(Bag.INVENTORY, itemId). */
    @Deprecated public boolean hasItem(String itemId) {
        return hasItem(Bag.INVENTORY, itemId);
    }

    /** @deprecated Prefer bagged version: getItemCount(Bag.INVENTORY, itemId). */
    @Deprecated public int getItemCount(String itemId) {
        return getItemCount(Bag.INVENTORY, itemId);
    }

    /** @deprecated Prefer bagged version: getTotalCount(Bag.INVENTORY). */
    @Deprecated public int getTotalItemCount() {
        return getTotalCount(Bag.INVENTORY);
    }

    /** @deprecated Prefer bagged version: removeItem(Bag.INVENTORY, itemId). */
    @Deprecated public void removeItem(String itemId) {
        removeItem(Bag.INVENTORY, itemId);
    }

    /** @deprecated Prefer bagged version: useItem(Bag.INVENTORY, itemId). */
    @Deprecated public void useItem(String itemId) {
        useItem(Bag.INVENTORY, itemId);
    }

    /** @deprecated Prefer bagged version: useItems(Bag.INVENTORY, itemId, amount). */
    @Deprecated public void useItems(String itemId, int amount) {
        useItems(Bag.INVENTORY, itemId, amount);
    }


    /**
     * Returns the backing map for the given bag
     *
     * @param bag bag selector
     * @return mutable map for that bag
     */
    private Map<String, Integer> mapFor(Bag bag) {
        return switch (bag) {
            case INVENTORY -> inventory;
            case UPGRADES  -> upgrades;
            case OBJECTIVES-> objectives;
        };
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