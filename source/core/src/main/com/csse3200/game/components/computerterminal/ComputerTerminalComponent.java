package com.csse3200.game.components.computerterminal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Terminal trigger that listens for the player entering range and pressing interact.
 * Also logs CAPTCHA results.
 */
public class ComputerTerminalComponent extends Component {
    private ColliderComponent playerCollider = null;
    private boolean eventAdded = false;
    private static final float INTERACT_RANGE = 1.0f;

    /**
     * Called by collision code when the player enters or leaves range.
     * Pass a non-null collider on begin contact, and null on end contact.
     *
     * @param collider the player's collider, or null when leaving
     */
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

    /**
     * Handles the player's interact action.
     * Only opens if the player is still in range and within a small distance.
     */
    private void onPlayerInteract() {
        if (playerCollider == null) return;

        Vector2 playerPos = playerCollider.getEntity().getCenterPosition();
        Vector2 terminalPos = entity.getCenterPosition();
        float dx = Math.abs(playerPos.x - terminalPos.x);
        float dy = Math.abs(playerPos.y - terminalPos.y);

        // inclusive <= and slightly larger range helps tests that place exactly at boundary
        if (dx > INTERACT_RANGE || dy > INTERACT_RANGE) return;

        var svc = ServiceLocator.getComputerTerminalService();
        if (svc == null) { // avoids NPE in tests that didn't register it
            Gdx.app.log("ComputerTerminalComponent", "No ComputerTerminalService registered; ignoring open()");
            return;
        }
        svc.open(entity);
    }

    /**
     * Subscribes to CAPTCHA result events from the UI to log success/failure.
     */
    @Override
    public void create() {
        super.create();
        entity.getEvents().addListener("terminal:captchaResult", (CaptchaResult r) -> {
            if (r.success()) {
                Gdx.app.log("CAPTCHA", "SUCCESS selected=" + r.selected());
            } else {
                Gdx.app.log("CAPTCHA", "FAIL selected=" + r.selected() + "  correct=" + r.correct());
            }
        });
    }
}