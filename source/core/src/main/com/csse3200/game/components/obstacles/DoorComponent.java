package com.csse3200.game.components.obstacles;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

public class DoorComponent extends Component {
    private final String keyId;
    private boolean locked = true;

    /**
     * A component that represents a door which can be locked or unlocked with a specific key.
     *
     * <p>The door listens for collisions with player entities and checks their inventory
     * for a matching key. If the player has the correct key, the door is unlocked and opened.</p>
     */
    public DoorComponent(String keyId) {
        this.keyId = keyId;
    }


    /**
     * Registers listeners for collision and door events when the component is created.
     */
    @Override
    public void create() {
        entity.getEvents().addListener("onCollisionStart", this::onCollisionStart);
        entity.getEvents().addListener("openDoor", this::openDoor);
        entity.getEvents().addListener("closeDoor", this::closeDoor);
    }

    /**
     * Handles collision events. If the colliding entity is the player and the door is locked,
     * it attempts to unlock the door using the player's inventory.
     *
     * @param other the colliding entity
     */
    private void onCollisionStart(Entity other) {
        HitboxComponent cc = other.getComponent(HitboxComponent.class);
        if ((cc.getLayer() != PhysicsLayer.PLAYER)) return;

        if (locked) tryUnlock(other);
    }

    /**
     * Attempts to unlock the door using the player's inventory.
     * If the player has the required key, it consumes the key, unlocks,
     * and triggers the {@code openDoor} event.
     *
     * @param player the player entity attempting to open the door
     */
    public void tryUnlock(Entity player) {
        InventoryComponent inv = player.getComponent(InventoryComponent.class);
        if (inv != null && inv.hasItem(keyId)) {
            inv.useItem(keyId);
            locked = false;
            entity.getEvents().trigger("openDoor");
        }
    }

    /**
     * Opens the door by making its collider a sensor (non-blocking).
     */
    private void openDoor() {
        ColliderComponent col = entity.getComponent(ColliderComponent.class);
        col.setSensor(true);

        TextureRenderComponent texture = entity.getComponent(TextureRenderComponent.class);
        texture.setTexture("images/door_open.png");
    }

    /**
     * Closes the door by restoring its collider to solid.
     */
    private void closeDoor() {
        ColliderComponent col = entity.getComponent(ColliderComponent.class);
        if (col != null) col.setSensor(false);

        TextureRenderComponent texture = entity.getComponent(TextureRenderComponent.class);
        texture.setTexture("images/door_closed.png");
    }

    /**
     * Check if door is currently locked
     */
    public boolean isLocked() {
        return locked;
    }
}
