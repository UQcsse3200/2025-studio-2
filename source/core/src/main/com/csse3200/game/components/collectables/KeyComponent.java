package com.csse3200.game.components.collectables;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

/**
 * A collectable key component that allows the player to pick up and store keys
 * in their {@link InventoryComponent}. Each {@code KeyComponent} has a unique
 * {@code keyId} string which identifies it, and is used by systems such as
 * {@code DoorComponent} to determine whether a door can be unlocked.
 *
 * <p>When the player collides with an entity containing this component,
 * the key is added to the player's inventory. Multiple keys of the same
 * {@code keyId} may be collected and are tracked as a count in the inventory.</p>
 */
public class KeyComponent extends CollectableComponent {
    public final String keyId;

    /**
     * Creates a {@code KeyComponent} with a given key identifier.
     *
     * @param keyId the unique ID string used to identify this key
     */
    public KeyComponent(String keyId) {
        this.keyId = keyId;
    }

    /**
     * Attempts to collect this key into the player's inventory.
     * <p>
     * On success, the key is added to the {@link InventoryComponent} of the
     * colliding player entity, and debug information is logged to the console.
     * </p>
     *
     * @param player the player entity attempting to collect the key
     * @return {@code true} if the key was successfully collected and stored,
     *         {@code false} if the player entity is null or has no inventory
     */
    @Override
    protected boolean onCollect(Entity player) {
        if (player == null) return false;

        var inventory = player.getComponent(InventoryComponent.class);
        if (inventory != null) {
            inventory.addItem(keyId);
            inventory.removeItem(InventoryComponent.Bag.OBJECTIVES, "keycard");
            return true;
        }
        return false;
    }

    /**
     *Returns this key types identifier.
     *
     * @return the non-null key identifier string
     */
    public String getKeyId() {
        return keyId;
    }
}