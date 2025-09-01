package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Component for button entities that can be pushed by the player to trigger other events
 * Class controls the state of the button (normal or pushed) and
 * the button colour based on the type (door, platform or nothing)
 */
public class ButtonComponent extends Component {
    private boolean isPushed = false;
    private String type; //type of button

    private boolean playerInRange = false;
    private ColliderComponent playerCollider = null;
    private boolean addToPlayer = false;

    /**
     * Creates the button
     */
    @Override
    public void create() {
    }

    /**
     * Updates the button
     */
    @Override
    public void update() {
    }

    /**
     * Sets whether a player is in interaction range of this button
     * Adds the player to the "interact" event the first time
     *
     * @param collider Player's ColliderComponent (null if player leaves collision range)
     */
    public void setPlayerInRange(ColliderComponent collider) {
        //not in range if not colliding, return from function
        if(collider == null) {
            playerInRange = false;
            playerCollider = null;
            return;
        }

        playerInRange = true;
        playerCollider = collider;

        //adds player to event if not already
        if(!addToPlayer) {
            Entity player = playerCollider.getEntity();
            player.getEvents().addListener("interact", this::onPlayerInteract);
            addToPlayer = true;
        }

    }

    /**
     * Handles players interaction when they press 'E' (defined in KeyboardPlayerInputComponent)
     * Only toggles the button if the player is currently colliding with button
     */
    private void onPlayerInteract() {
        if( !playerInRange || playerCollider == null) { return;}

        Entity playerEntity = playerCollider.getEntity();

        Vector2 playerPos = playerEntity.getPosition();
        Vector2 buttonPos = entity.getPosition();

        if(playerPos.x < buttonPos.x - 0.5f) {
            toggleButton();
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
     * Sets the type for this button (which selects texture)
     *
     * @param type String representing the button type (door, platform or nothing)
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Checks if the player is currently in range of the button.
     *
     * @return true if the player is in range, false otherwise
     */
    public boolean isPlayerInRange() {
        return playerInRange;
    }

    /**
     * Retrieves the ColliderComponent of the player currently interacting with the box
     *
     * @return  the player's ColliderComponent, or null if no player is in range
     */
    public ColliderComponent getPlayerCollider() {
        return  playerCollider;
    }

    /**
     * Sets the buttons pushed state manually [for future implementations]
     * @param pushed state to set the button to
     */
    public void setPushed(boolean pushed) {
        this.isPushed = pushed;
    }
}
