package com.csse3200.game.lighting;

import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.components.lighting.ConeDetectorComponent;
import com.csse3200.game.entities.Entity;

import java.util.HashMap;
import java.util.Map;

public class SecurityCamRetrievalService implements Disposable {
    private final Map<String, Entity> cameraList;

    public SecurityCamRetrievalService() {
        this.cameraList = new HashMap<>();
    }

    public void registerCamera(String id, Entity entity) {
        cameraList.put(id, entity);
    }

    public Entity getSecurityCam(String id) {
        return cameraList.get(id);
    }

    public ConeDetectorComponent getDetectorComp(String id) {
        Entity e = cameraList.get(id);
        if (e != null) {
            return e.getComponent(ConeDetectorComponent.class);
        }
        return null;
    }

    @Override
    public void dispose() {
        cameraList.clear();
    }
}
