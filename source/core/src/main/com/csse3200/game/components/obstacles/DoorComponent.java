package com.csse3200.game.components.obstacles;

import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;

/**
 * Door that can be unlocked with a key and (optionally) trigger an area transition.
 *
 * <p><b>Usage</b>:
 * <ul>
 *   <li>Locked until the player collides and has {@code keyId} in {@link InventoryComponent.Bag#INVENTORY}.</li>
 *   <li>On unlock: collider becomes sensor, plays {@code door_opening}, then switches to {@code door_open}.</li>
 *   <li>After opening finishes, emits GameArea event {@code "doorEntered"(player, door)} if non-static and has target.</li>
 * </ul>
 *
 * <p><b>Requires</b>: {@link AnimationRenderComponent} with animations:
 * {@code door_closed} (loop), {@code door_opening} (non-loop), {@code door_open} (loop);
 * {@link ColliderComponent}, {@link HitboxComponent} on the door entity.</p>
 */
public class DoorComponent extends Component {
    private final String keyId;
    private final boolean isStaticDoor;
    private final String targetArea;

    private boolean locked = true;
    private boolean isOpening = false;
    private boolean animationFinished = false;

    private final GameArea area;
    private AnimationRenderComponent animationComponent;

    /**
     * @param keyId        inventory key id that unlocks the door
     * @param area         owning game area (used to emit {@code doorEntered})
     * @param isStaticDoor if true, acts as barrier only (no transition)
     * @param targetArea   target area id (used only when non-static)
     */
    public DoorComponent(String keyId, GameArea area, boolean isStaticDoor, String targetArea) {
        this.keyId = keyId;
        this.area = area;
        this.isStaticDoor = isStaticDoor;
        this.targetArea = (targetArea == null) ? "" : targetArea.trim();
    }

    /**
     * Wire listeners and set initial animation to {@code door_closed}.
     */
    @Override
    public void create() {
        entity.getEvents().addListener("onCollisionStart", this::onCollisionStart);
        entity.getEvents().addListener("onCollisionEnd", this::onCollisionEnd);
        entity.getEvents().addListener("openDoor", this::openDoor);
        entity.getEvents().addListener("closeDoor", this::closeDoor);

        animationComponent = entity.getComponent(AnimationRenderComponent.class);
        if (animationComponent != null) {
            animationComponent.startAnimation("door_closed");
        }
    }

    /**
     * Advance the animation; emit transition when it finishes (if eligible).
     */
    @Override
    public void update() {
        if (isOpening && !animationFinished && animationComponent != null && animationComponent.isFinished()) {
            onAnimationFinished();
            animationFinished = true;
        }
    }

    /**
     * Switch to {@code door_open}; emit {@code doorEntered} for non-static doors with a target.
     */
    private void onAnimationFinished() {
        ColliderComponent col = entity.getComponent(ColliderComponent.class);
        if (col != null) col.setSensor(true);

        if (animationComponent != null) {
            animationComponent.startAnimation("door_open");
        }
        isOpening = false;

        // trigger transition only for non-static doors with a target
        if (!isStaticDoor && area != null && area.getPlayer() != null && !targetArea.isEmpty()) {
            area.getEvents().trigger("doorEntered", area.getPlayer(), entity);
        }
    }

    /**
     * Player enters door collider.
     */
    private void onCollisionStart(Entity other) {
        if (other == null) return;

        HitboxComponent cc = other.getComponent(HitboxComponent.class);
        if (cc == null || cc.getLayer() != PhysicsLayer.PLAYER) return;

        if (locked) {
            tryUnlock(other);
            return;
        }

        if (!isOpening) {

            isOpening = true;
            animationFinished = false;
            if (animationComponent != null) {
                animationComponent.startAnimation("door_opening");
            }
        }
    }

    /**
     * Player exits door collider.
     */
    private void onCollisionEnd(Entity other) {
        if (other == null) return;

        HitboxComponent cc = other.getComponent(HitboxComponent.class);
        if (cc == null || cc.getLayer() != PhysicsLayer.PLAYER) return;
    }

    /**
     * Try to unlock the door using the player's inventory.
     */
    public void tryUnlock(Entity player) {
        if (player == null) return;

        InventoryComponent inv = player.getComponent(InventoryComponent.class);
        if (inv != null && inv.hasItem(InventoryComponent.Bag.INVENTORY, keyId)) {
            inv.useItem(InventoryComponent.Bag.INVENTORY, keyId);
            locked = false;
            entity.getEvents().trigger("openDoor");
        }
    }

    /**
     * Open the door (non-blocking + play opening animation).
     */
    public void openDoor() {
        locked = false;
        isOpening = true;
        animationFinished = false;

        if (animationComponent != null) {
            animationComponent.startAnimation("door_opening");
        }
    }

    /**
     * Close the door (solid + closed anim).
     */
    private void closeDoor() {
        ColliderComponent col = entity.getComponent(ColliderComponent.class);
        if (col != null) col.setSensor(false);

        if (animationComponent != null) {
            animationComponent.startAnimation("door_closed");
        }

        locked = true;
        isOpening = false;
        animationFinished = false;
    }

    /**
     * Accessor for isLocked
     *
     * @return true if door is locked, false otherwise.
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Accessory for door target area
     * @return the area (if any) on the other side of the door
     */
    public String getTargetArea() {
        return targetArea;
    }
}
