package com.csse3200.game.components;

import com.csse3200.game.components.obstacles.MoveableBoxComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Pressure plate that ONLY responds to weighted boxes.
 * Emits "plateToggled" (Boolean) when pressed/unpressed.
 */
public class BoxPressurePlateComponent extends Component {
    private String unpressedTexture = "images/plate.png";
    private String pressedTexture = "images/plate-pressed.png";

    private TextureRenderComponent renderer;
    private boolean pressed = false;
    private final List<Entity> activePressing = new ArrayList<>();

    /**
     * Called when component is created, intialises texture rendering
     */
    @Override
    public void create() {
        renderer = entity.getComponent(TextureRenderComponent.class);
        updateTexture();
    }

    /**
     * Update's the plate's state when entity moves on or off the plate
     * Only valid entities (weighted boxes or player) can trigger
     * @param other
     * @param onPlate
     */
    public void setEntityOnPlate(Entity other, boolean onPlate) {
        if (!isValid(other)) return;

        if (onPlate) {
            if (!activePressing.contains(other)) {
                activePressing.add(other);
            }
        } else {
            activePressing.remove(other);
        }
        setPressed(!activePressing.isEmpty());
    }

    /**
     * Determines if given entity is valid to be pressing down the pressure plate
     * Valid only if it is a weighted box or player
     *
     * @param other the entity to validate
     *
     * @return true is entity can press plate, false otherwise
     */
    private boolean isValid(Entity other) {
        if (other.getComponent(PlayerActions.class) != null) {
            return isAbove(other);
        }

        com.csse3200.game.components.obstacles.MoveableBoxComponent box = other.getComponent(MoveableBoxComponent.class);
        if (box != null) {
            ColliderComponent collider = box.getEntity().getComponent(ColliderComponent.class);
            if (collider != null && collider.getLayer() != PhysicsLayer.LASER_REFLECTOR) {
                return isAbove(other);
            }
        }

        return false;
    }

    /**
     * Checks whether entity pressing is positioned above plate
     *
     * @param other the entity to check
     *
     * @return true if entity vertically above, false otherwise
     */
    private boolean isAbove(Entity other) {
        float plateY = entity.getPosition().y;
        float otherY = other.getPosition().y;
        return otherY > plateY + 0.5f;
    }

    /**
     * Updates the plate's pressed state and triggered the plateToggled event
     *
     * @param pressed true to set the plate as pressed, false otherwise
     */
    private void setPressed(boolean pressed) {
        if(this.pressed == pressed) return;
        this.pressed = pressed;
        updateTexture();
        entity.getEvents().trigger("plateToggled", pressed);
    }

    /**
     * Updates plates texture based on its current pressed state
     */
    private void updateTexture() {
        if (renderer == null) return;
        renderer.setTexture(pressed ? pressedTexture : unpressedTexture);
    }
}