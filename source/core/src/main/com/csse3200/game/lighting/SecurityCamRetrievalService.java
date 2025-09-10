package com.csse3200.game.lighting;

import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.components.lighting.ConeDetectorComponent;
import com.csse3200.game.entities.Entity;

import java.util.HashMap;
import java.util.Map;

/**
 * A service to hold a map of the key-item pairs (id string, security camera entity) allowing
 * for global access. This is useful when registering a new enemy with the corresponding camera
 */
public class SecurityCamRetrievalService implements Disposable {
    private final Map<String, Entity> cameraList;

    public SecurityCamRetrievalService() {
        this.cameraList = new HashMap<>();
    }

    /**
     * Register a security camera entity with the service.
     *
     * @param id The id of the security camera
     * @param entity The parent entity of the security camera
     */
    public void registerCamera(String id, Entity entity) {
        cameraList.put(id, entity);
    }

    /**
     * Retrieve the corresponding security camera based upon its id.
     *
     * @param id The id of the security camera
     * @return The parent entity of the security camera
     */
    public Entity getSecurityCam(String id) {
        return cameraList.get(id);
    }

    /**
     * Returns the cone detector component of the security camera
     *
     * @param id The id of the security camera
     * @return The cone detector component from the security camera
     */
    public ConeDetectorComponent getDetectorComp(String id) {
        Entity e = cameraList.get(id);
        if (e != null) {
            return e.getComponent(ConeDetectorComponent.class);
        }
        return null;
    }

    @Override
    public void dispose() {
        // clear the map
        cameraList.clear();
    }
}
