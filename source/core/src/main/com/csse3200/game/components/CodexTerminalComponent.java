package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Special component that can be attached to codex terminal entries.
 * Responsible for handling player interactions and performing side effects.
 */
public class CodexTerminalComponent extends Component {
    /**
     * Logger object
     */
    private static final Logger log = LoggerFactory.getLogger(CodexTerminalComponent.class);
    /**
     * Reference to the most recent collider interacting with object. Presumed to be player.
     */
    private ColliderComponent playerCollider = null;
    /**
     * Flag determining whether the "interact" listener was added to the player.
     */
    private boolean eventAdded = false;
    /**
     * Flag determining whether the terminal has been interacted with.
     */
    private boolean interactedWith = false;

    /**
     * Decide if the player is considered in range given a collider input. If the collider is not
     * null, presume player is colliding and add "interact" event.
     * @param collider The collider interacting with this component's entity.
     */
    public void setPlayerInRange(ColliderComponent collider) {
        // Updates for null collider
        if (collider == null) {
            playerCollider = null;
            return;
        }

        // Updates for non-null collider
        playerCollider = collider;

        // Add event to player exactly once
        if (!eventAdded) {
            Entity player = playerCollider.getEntity();
            player.getEvents().addListener("interact", this::onPlayerInteract);
            eventAdded = true;
        }
    }

    /**
     * Event called when the player interacts with the terminal. Determines whether the player is
     * in an acceptable range to interact, and then performs side effects of interacting
     * (changing texture, removing tooltip and removing light).
     */
    private void onPlayerInteract() {
        // Do nothing if player collider not detected, or in range, or terminal
        // already interacted with
        if (playerCollider == null || interactedWith) {
            return;
        }

        // Compare player position with terminal
        Vector2 playerPos = playerCollider.getEntity().getCenterPosition();
        Vector2 terminalPos = entity.getCenterPosition();
        float dx = Math.abs(playerPos.x - terminalPos.x);
        float dy = Math.abs(playerPos.y - terminalPos.y);

        // Should the player be in range...
        if (dx < 0.8f && dy < 0.8f) {
            // Update the texture of the terminal
            entity.getComponent(TextureRenderComponent.class).setTexture("images/terminal_off.png");

            // Disable light component
            entity.getComponent(ConeLightComponent.class).dispose();
            // Disable tooltip component
            entity.getComponent(TooltipSystem.TooltipComponent.class).dispose();

            // Prevent player from interacting after first interaction
            interactedWith = true;
        }
    }
}
