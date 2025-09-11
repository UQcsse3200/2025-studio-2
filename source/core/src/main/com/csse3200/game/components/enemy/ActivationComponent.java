
package com.csse3200.game.components.enemy;

import com.csse3200.game.components.Component;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This component links an enemy to a security camera by ID.
 * When the camera detects/loses the target, it triggers "enemyActivated"/"enemyDeactivated" on the owner entity.
 * If the camera isn't spawned yet, the component retries linking in update().
 */
public class ActivationComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(ActivationComponent.class);

    private final String cameraId;
    private Entity cam;
    private boolean linked = false;

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
        linkCamera();
    }

    /**
     * Retries linking if unsuccessful during create().
     */
    @Override
    public void update() {
        if (!linked) {
            linkCamera();
        }
    }

    /**
     * Clean up.
     */
    @Override
    public void dispose() {
        cam = null;
        linked = false;
        super.dispose();
    }

    /**
     * Locate the camera and attach listeners exactly once.
     */
    private void linkCamera() {
        if (linked) return;

        this.cam = ServiceLocator.getSecurityCamRetrievalService().getSecurityCam(cameraId);
        if (this.cam == null) {
            logger.debug ("ActivationComponent: camera '{}' not found yet; will retry.", cameraId);
            return;
        }

        cam.getEvents().addListener("targetDetected", (Entity detected) -> {
            entity.getEvents().trigger("enemyActivated");
        });
        cam.getEvents().addListener("targetLost", (Entity detected) -> {
            entity.getEvents().trigger("enemyDeactivated");
        });

        linked = true;
        logger.debug("Linked entity {} to security camera {}", entity, cameraId);
    }

    /**
     * Getter to check camera is linked to entity
     * @return true if linked, otherwise false.
     */
    public boolean isLinked() {
        return linked;
    }
}
