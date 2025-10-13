
package com.csse3200.game.components.enemy;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This component links an enemy to a security camera by ID.
 * - Attaches listeners once the camera exists
 * - Re-links automatically if the camera instance changes (e.g. after reset)
 */
public class ActivationComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(ActivationComponent.class);

    private final String cameraId;
    private Entity cam;

    /**
     * Create an ActivationComponent for a specific camera
     * @param cameraId Unique camera identified to link to to (e.g. "1")
     */
    public ActivationComponent(String cameraId) {
        this.cameraId = cameraId;
    }

    /**
     * Attempts to link immediately.
     * Retries in update() if camera isn't ready (no spawn race).
     */
    @Override
    public void create() {
        super.create();
        ensureLinked();
    }

    /**
     * Retries linking if unsuccessful during create().
     */
    @Override
    public void update() {
        ensureLinked();
    }

    /**
     * Clean up.
     */
    @Override
    public void dispose() {
        cam = null;
        super.dispose();
    }

    /**
     * Ensure owner entity is linked to the current camera instance.
     * Attach listeners exactly once.
     */
    private void ensureLinked() {
        Entity current = ServiceLocator.getSecurityCamRetrievalService().getSecurityCam(cameraId);
        if (current == null) {
            // Camera not spawned yet - retries next update.
            return;
        }

        if (current == cam) {
            // Already linked to this instance
            return;
        }

        // Attach listeners
        current.getEvents().addListener("targetDetected", (Entity detected) ->
            entity.getEvents().trigger("enemyActivated")
        );
        current.getEvents().addListener("targetLost", (Entity detected) ->
            entity.getEvents().trigger("enemyDeactivated")
        );

        cam = current;
        logger.debug("Linked entity {} to security camera {}", entity, cameraId);
    }

    /**
     * Getter to check camera is linked to entity
     * @return true if linked, otherwise false.
     */
    public boolean isLinked() {
        return cam  != null;
    }
}
