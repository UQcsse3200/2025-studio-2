
package com.csse3200.game.components.enemy;

import com.csse3200.game.components.Component;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This component links an enemy to a security camera by ID.
 * When the camera detects/loses the target, it triggers the enemy's activation events.
 * To avoid spawn order race, attempts to link camera in create and update.
 */
public class ActivationComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(ActivationComponent.class);

    private final String cameraId;
    private Entity cam;

    public ActivationComponent(String cameraId) {
        this.cameraId = cameraId;
    }

    @Override
    public void create() {
        super.create();
        linkCamera();
    }

    @Override
    public void update() {
        if (cam == null) {
            linkCamera();
        }
    }

    @Override
    public void dispose() {
        cam = null;
        super.dispose();
    }

    private void linkCamera() {
        Entity cam = ServiceLocator.getSecurityCamRetrievalService().getSecurityCam(cameraId);
        if (cam == null) {
            logger.debug ("No security camera found with id '{}'", cameraId);
            return;
        }

        cam.getEvents().addListener("targetDetected", (Entity detected) -> {
            entity.getEvents().trigger("enemyActivated");
        });
        cam.getEvents().addListener("targetLost", (Entity detected) -> {
            entity.getEvents().trigger("enemyDeactivated");
        });

        logger.debug("Linked entity {} to security camera {}", entity, cameraId);
    }
}
