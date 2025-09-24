package com.csse3200.game.components;

import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * A pressure plate that toggles its pressed state when a player steps on it.
 * The plate latches (stays pressed) until reset manually.
 */
public class PressurePlateComponent extends Component {
    private boolean isPressed = false;
    private final boolean latched = true; // Always latched as per requirements
    private String unpressedTexture = "images/pressure_plate_unpressed.png";
    private String pressedTexture = "images/pressure_plate_pressed.png";


    @Override
    public void create() {
    }

    public void setPlayerOnPlate(ColliderComponent collider) {
        if (collider != null && !isPressed) {
            isPressed = true;
            updateTexture();
            entity.getEvents().trigger("plateToggled", true);
        } else if (collider == null && isPressed) {
            isPressed = false;
            updateTexture();
            entity.getEvents().trigger("plateToggled", false);
        }
    }

    private void onCollisionStart(Object me, Object other) {
        // System.out.println("Pressure plate collision with: " + other);
        if (isPressed) {
            return; // Already pressed; ignore further collisions
        }
        if (!(other instanceof Entity)) {
            return;
        }
        Entity otherEntity = (Entity) other;
        // Only react when the player steps on the plate
        if (otherEntity.getComponent(PlayerActions.class) != null) {
            isPressed = true;
            updateTexture();
            entity.getEvents().trigger("plateToggled", true);
        }
    }

    private void updateTexture() {
        // Change the texture only if a pressed texture has been specified
        TextureRenderComponent render = entity.getComponent(TextureRenderComponent.class);
        if (render == null) {
            return;
        }
        if (isPressed && pressedTexture != null) {
            render.setTexture(pressedTexture);
        } else {
            render.setTexture(unpressedTexture);
        }
    }

    /** Allow external configuration of textures if you add a pressed texture later */
    public void setTextures(String unpressed, String pressed) {
        this.unpressedTexture = unpressed;
        this.pressedTexture = pressed;
        // Immediately update to the current state’s texture
        updateTexture();
    }
}
