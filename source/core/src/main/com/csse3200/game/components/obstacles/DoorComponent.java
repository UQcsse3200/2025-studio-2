package com.csse3200.game.components.obstacles;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.physics.components.HitboxComponent;

public class DoorComponent extends Component {
    private final String keyId;
    //private final String levelId;
    private boolean locked = true;
    private boolean isOpening = false; // track animation state
    private boolean animationFinished = false;
    private final GameArea area;
    private AnimationRenderComponent animationComponent;

    /**
     * A component that represents a door which can be locked or unlocked with a specific key.
     *
     * <p>The door listens for collisions with player entities and checks their inventory
     * for a matching key. If the player has the correct key, the door is unlocked and opened.</p>
     */
    public DoorComponent(String keyId, GameArea area) {
        this.keyId = keyId;
        this.area = area;
//        this.levelId = levelId;
    }


    /**
     * Registers listeners for collision and door events when the component is created.
     */
    @Override
    public void create() {
        entity.getEvents().addListener("onCollisionStart", this::onCollisionStart);
        entity.getEvents().addListener("onCollisionEnd", this::onCollisionEnd);
        entity.getEvents().addListener("openDoor", this::openDoor);
        entity.getEvents().addListener("closeDoor", this::closeDoor);

        // Get the animation component
        animationComponent = entity.getComponent(AnimationRenderComponent.class);

        // start with closed door
        if (animationComponent != null) {
            animationComponent.startAnimation("door_closed");
        }
    }

    // Continually checks if animation finished
    @Override
    public void update() {
        // Check if opening animation has finished
        if (isOpening && !animationFinished && animationComponent != null) {
            if (animationComponent.isFinished()) {
                onAnimationFinished();
                animationFinished = true;
            }
        }
    }

    private void onAnimationFinished() {
        // Switch to open animation
        if (animationComponent != null) {
            animationComponent.startAnimation("door_open");
        }

        isOpening = false;
    }

    /**
     * Handles collision events. If the colliding entity is the player and the door is locked,
     * it attempts to unlock the door using the player's inventory.
     *
     * @param other the colliding entity
     */
    private void onCollisionStart(Entity other) {
        HitboxComponent cc = other.getComponent(HitboxComponent.class);
        if (cc == null || (cc.getLayer() != PhysicsLayer.PLAYER)) return;


        if (locked) {
            tryUnlock(other);
        } else if (!isOpening) {
            // Make collider a sensor
            ColliderComponent col = entity.getComponent(ColliderComponent.class);
            if (col != null) {
                col.setSensor(true);
            }

            // Door is fully open -> trigger transition
            this.area.trigger("doorEntered");
        }
    }

    private void onCollisionEnd(Entity other) {
        HitboxComponent cc = other.getComponent(HitboxComponent.class);
        if (cc == null || (cc.getLayer() != PhysicsLayer.PLAYER)) return;

        // When player leaves the door area, reset it
        if (!locked) {
            ColliderComponent col = entity.getComponent(ColliderComponent.class);
            if (col != null) {
                col.setSensor(false); // Make solid again
            }
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
        }
    }

    /**
     * Opens the door by making its collider a sensor (non-blocking).
     */
    public void openDoor() {
//        ColliderComponent col = entity.getComponent(ColliderComponent.class);
//        col.setSensor(true);

        isOpening = true;
        animationFinished = false;
        locked = false;

        // play door opening animation
        if (animationComponent != null) {
            animationComponent.startAnimation("door_opening");
        }

    }

    /**
     * Closes the door by restoring its collider to solid.
     */
    private void closeDoor() {
        ColliderComponent col = entity.getComponent(ColliderComponent.class);
        if (col != null) col.setSensor(false);

        // play door closing animation
        if (animationComponent != null) {
            animationComponent.startAnimation("door_closing");
        }

        locked = true;
        isOpening = false;
        animationFinished = false;
    }

    /**
     * Check if door is currently locked
     */
    public boolean isLocked() {
        return locked;
    }
}
