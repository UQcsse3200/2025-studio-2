package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Component for button entities that can be pushed by the player to trigger other events
 * Class controls the state of the button (normal or pushed), the logic for cooldown and
 * the button colour based on the type (door, platform or nothing)
 */
public class ButtonComponent extends Component {
    private boolean isPushed = false;
    private float cooldown = 0f; //time remaining before button can be pushed again
    private String type; //type of button

    /**
     * Registers the button to listen for "push" events
     */
    @Override
    public void create() {
        entity.getEvents().addListener("push", this::onPush);
    }

    /**
     * Updates the button cooldown timer each frame
     */
    @Override
    public void update() {
        if(cooldown > 0) {
            cooldown -= ServiceLocator.getTimeSource().getDeltaTime();
        }
    }

    /**
     * Sets the type for this button (which selects texture)
     * @param type String representing the button type (door, platform or nothing)
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Handles a "push" event: checks for player collision and triggers collision if player
     *  is to the left of the button
     * @param other object that collided with the button (expected to be a ColliderComponent)
     */
    private void onPush(Object other) {
        if(cooldown > 0) {
            return;
        }
        if (other instanceof ColliderComponent) {
            Entity otherEntity = ((ColliderComponent) other).getEntity();

            // Ensure other entity is the player
            if (otherEntity.getComponent(PlayerActions.class) != null) {
                Vector2 playerPos = otherEntity.getPosition();
                Vector2 buttonPos = entity.getPosition();

                //only trigger if player is left of button and start countdown
                if(playerPos.x < buttonPos.x - 0.5f) {
                    toggleButton();
                    cooldown = 0.5f;
                }
            }
        }
    }

    /**
     * Toggles the buttons state and updates its texture based on its type
     * Triggers buttonToggled event that can be listened for so events can be implemented on push
     */
    private void toggleButton() {
        isPushed = !isPushed;
        entity.getEvents().trigger("buttonToggled", isPushed);

        // set button texture based on its type
        if("platform".equals(type)) {
            String texture = isPushed ? "images/blue_button_pushed.png" : "images/blue_button.png";
            TextureRenderComponent render = entity.getComponent(TextureRenderComponent.class);
            if (render != null) {
                render.setTexture(texture);
            }
        }else if("door".equals(type)) {
            String texture = isPushed ? "images/red_button_pushed.png" : "images/red_button.png";
            TextureRenderComponent render = entity.getComponent(TextureRenderComponent.class);
            if (render != null) {
                render.setTexture(texture);
            }
        }else {
            String texture = isPushed ? "images/button_pushed.png" : "images/button.png";
            TextureRenderComponent render = entity.getComponent(TextureRenderComponent.class);
            if (render != null) {
                render.setTexture(texture);
            }
        }
    }

    /**
     * sets the buttons pushed state manually [for future implementations]
     * @param pushed state to set the button to
     */
    public void setPushed(boolean pushed) { this.isPushed = pushed; }
}
