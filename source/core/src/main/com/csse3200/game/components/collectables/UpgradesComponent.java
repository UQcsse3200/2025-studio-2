package com.csse3200.game.components.collectables;

import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
public class UpgradesComponent extends CollectableComponent {

    public final String upgradeId;

    public UpgradesComponent(String upgradeId) {
        this.upgradeId = upgradeId;
    }

    protected boolean onCollect(Entity player) {
        if (player == null) return false;

        InventoryComponent inventory = player.getComponent(InventoryComponent.class);
        if (inventory != null) {
            if (!inventory.hasItem(InventoryComponent.Bag.UPGRADES, upgradeId)) {
                inventory.addItem(InventoryComponent.Bag.UPGRADES, upgradeId);
            }
            return true;
        }

        return false;
    }

    public String getUpgradeId() {return upgradeId;}
}
