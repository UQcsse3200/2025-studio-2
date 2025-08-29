package com.csse3200.game.components.obstacles;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;

public class DoorComponent extends Component {
    private final String keyId;
    private boolean locked = true;

    public DoorComponent(String keyId) {
        this.keyId = keyId;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        entity.getEvents().addListener("openDoor", this::openDoor);
        entity.getEvents().addListener("closeDoor", this::closeDoor);
    }

    private void onCollisionStart(Object me, Object other) {
        if (!(other instanceof Entity player)) return;

        ColliderComponent cc = player.getComponent(ColliderComponent.class);
        if (cc == null || cc.getLayer() != PhysicsLayer.PLAYER) return;

        if (locked) tryUnlock(player);
    }

    public void tryUnlock(Entity player) {
        InventoryComponent inv = player.getComponent(InventoryComponent.class);
        if (inv != null && inv.hasItem(keyId)) {
            inv.useItem(keyId);
            locked = false;
            entity.getEvents().trigger("openDoor");
        }
    }

    private void openDoor() {
        ColliderComponent col = entity.getComponent(ColliderComponent.class);
        col.setSensor(true);
    }

    private void closeDoor() {
        ColliderComponent col = entity.getComponent(ColliderComponent.class);
        if (col != null) col.setSensor(false);
    }

    public boolean isLocked() {
        return locked;
    }
}
