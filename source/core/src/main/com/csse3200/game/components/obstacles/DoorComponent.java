package com.csse3200.game.components.obstacles;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.rendering.TextureRenderComponent;

public class DoorComponent extends Component {
    private final String keyId;
    private boolean locked = true;
    private final GameArea area;

    /**
     * A component that represents a door which can be locked or unlocked with a specific key.
     *
     * <p>The door listens for collisions with player entities and checks their inventory
     * for a matching key. If the player has the correct key, the door is unlocked and opened.</p>
     */
    public DoorComponent(String keyId, GameArea area) {
        this.keyId = keyId;
        this.area = area;
    }


    /**
     * Registers listeners for collision and door events when the component is created.
     */
    @Override
    public void create() {
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        entity.getEvents().addListener("openDoor", this::openDoor);
        entity.getEvents().addListener("closeDoor", this::closeDoor);

        this.openDoor();
    }

    /**
     * Handles collision events. If the colliding entity is the player and the door is locked,
     * it attempts to unlock the door using the player's inventory.
     *
     * @param me    the door entity (ignored here)
     * @param other the colliding entity
     */
    private void onCollisionStart(Object me, Object other) {
        if (!(other instanceof Entity player)) return;

        ColliderComponent cc = player.getComponent(ColliderComponent.class);
        if (cc == null || cc.getLayer() != PhysicsLayer.PLAYER) return;

        if (locked) {
            tryUnlock(player);
        } else {
            // Door already open -> trigger transition
            this.area.trigger("doorEntered", keyId);
        }
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

            // Notify the area
            this.area.trigger("doorEntered", keyId);
        }
    }

    /**
     * Opens the door by making its collider a sensor (non-blocking).
     */
    public void openDoor() {
        ColliderComponent col = entity.getComponent(ColliderComponent.class);
        col.setSensor(true);

        String texture = "images/Gate_open.png";
        TextureRenderComponent render = entity.getComponent(TextureRenderComponent.class);
        if (render != null) {
            render.setTexture(texture);
        }
    }

    /**
     * Closes the door by restoring its collider to solid.
     */
    private void closeDoor() {
        ColliderComponent col = entity.getComponent(ColliderComponent.class);
        if (col != null) col.setSensor(false);

        String texture = "images/gate.png";
        TextureRenderComponent render = entity.getComponent(TextureRenderComponent.class);
        if (render != null) {
            render.setTexture(texture);
        }
    }

    /**
     * Check if door is currently locked
     */
    public boolean isLocked() {
        return locked;
    }
}
