package com.csse3200.game;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;


public class ComputerTerminalComponent extends Component {
    private ColliderComponent playerCollider = null;

    private boolean eventAdded = false;

    private boolean interactedWith = false;

    public void setPlayerInRange(ColliderComponent collider) {
        if (collider == null) {
            playerCollider = null;
            return;
        }

        playerCollider = collider;

        if (!eventAdded) {
            Entity player = playerCollider.getEntity();
            player.getEvents().addListener("interact", this::onPlayerInteract);
            eventAdded = true;
        }
    }

    private void onPlayerInteract() {
        if (playerCollider == null || interactedWith) {
            return;
        }

        // Proximity check (same idea as CodexTerminalComponent)
        Vector2 playerPos = playerCollider.getEntity().getCenterPosition();
        Vector2 terminalPos = entity.getCenterPosition();
        float dx = Math.abs(playerPos.x - terminalPos.x);
        float dy = Math.abs(playerPos.y - terminalPos.y);

        if (dx < 0.8f && dy < 0.8f) {
            interactedWith = true;

            // Let the game know we've interacted; minigame/factory can listen for this.
            entity.getEvents().trigger("terminal:interact");

        }
    }


    public boolean hasInteracted() {
        return interactedWith;
    }
}