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
 * Pressure plate that ONLY responds to moveable boxes.
 * Emits "plateToggled" (Boolean) when pressed/unpressed.
 */
public class BoxPressurePlateComponent extends Component {
    private String unpressedTexture = "images/pressure_plate_unpressed.png";
    private String pressedTexture = "images/pressure_plate_pressed.png";

    private TextureRenderComponent renderer;
    private boolean pressed = false;
    private final List<Entity> activePressing = new ArrayList<>();


    @Override
    public void create() {
        renderer = entity.getComponent(TextureRenderComponent.class);
        updateTexture();
    }

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

    private boolean isAbove(Entity other) {
        float plateY = entity.getPosition().y;
        float otherY = other.getPosition().y;
        return otherY > plateY + 0.5f;
    }

    private void setPressed(boolean pressed) {
        if(this.pressed == pressed) return;
        this.pressed = pressed;
        updateTexture();
        entity.getEvents().trigger("plateToggled", pressed);
    }

    private void updateTexture() {
        if (renderer == null) return;
        renderer.setTexture(pressed ? pressedTexture : unpressedTexture);
    }
}