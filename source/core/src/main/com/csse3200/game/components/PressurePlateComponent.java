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
    private String unpressedTexture = "images/pressure_plate_unpressed.png";
    private String pressedTexture = "images/pressure_plate_pressed.png";

    /**
     * Called when component is created
     */
    @Override
    public void create() {
    }

    /**
     * Updates the pressure plates pressed state when a collider steps on or off
     * @param collider
     */
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

    /**
     * Updates the pressure plates texture based on its current pressed state
     */
    private void updateTexture() {
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

    /**
     * Sets texture for pressed and unpressed state of the pressure plate
     *
     * @param unpressed texture for when unpressed
     * @param pressed texture for when pressed
     */
    public void setTextures(String unpressed, String pressed) {
        this.unpressedTexture = unpressed;
        this.pressedTexture = pressed;
        updateTexture();
    }
}
