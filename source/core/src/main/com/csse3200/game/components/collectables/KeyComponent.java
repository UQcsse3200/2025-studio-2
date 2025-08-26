package com.csse3200.game.components.collectables;

import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;

public class KeyComponent extends CollectableComponent {
    public final String keyId;

    public KeyComponent(String keyId) {
        this.keyId = keyId;
    }

    @Override
    protected boolean onCollect(Entity player) {
        if (player == null) return false;

        var inventory = player.getComponent(InventoryComponent.class);
        if (inventory != null) {
            inventory.addItem(keyId);
            System.out.println("Key collected by player");
            System.out.println("Number of " + keyId + " in inventory: " + inventory.getItemCount(keyId));
            return true;
        }
        return false;
    }
}