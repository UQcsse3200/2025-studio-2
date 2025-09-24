package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

public class CodexTerminalComponent extends Component {

    private boolean playerInRange = false;
    private ColliderComponent playerCollider = null;
    private boolean eventAdded = false;
    private boolean interactedWith = false;

    public void setPlayerInRange(ColliderComponent collider) {
        if (collider == null) {
            playerInRange = false;
            playerCollider = null;
            return;
        }

        playerInRange = true;
        playerCollider = collider;

        // Add event to player exactly once
        if (!eventAdded) {
            Entity player = playerCollider.getEntity();
            player.getEvents().addListener("interact", this::onPlayerInteract);
            eventAdded = true;
        }
    }

    private void onPlayerInteract() {
        // Do nothing if player collider not detected, or in range, or terminal
        // already interacted with
        if (!playerInRange || playerCollider == null || interactedWith) {
            return;
        }

        // Compare player position with terminal
        Vector2 playerPos = playerCollider.getEntity().getPosition();
        Vector2 terminalPos = entity.getPosition();
        float dx = Math.abs(playerPos.x - terminalPos.x);
        float dy = Math.abs(playerPos.y - terminalPos.y);

        if (dx < 0.5f && dy < 0.5f) {
            // Update the texture of the terminal
            entity.getComponent(TextureRenderComponent.class).setTexture("images/terminal_off.png");

            // Disable light component
            entity.getComponent(ConeLightComponent.class).dispose();

            interactedWith = true;
        }
    }
}
