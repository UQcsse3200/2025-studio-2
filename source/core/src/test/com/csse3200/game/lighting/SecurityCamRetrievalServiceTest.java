package com.csse3200.game.lighting;

import com.csse3200.game.components.lighting.ConeDetectorComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class SecurityCamRetrievalServiceTest {
    @SuppressWarnings("unchecked")
    private static Map<String, Entity> getBackingMap(SecurityCamRetrievalService svc) throws Exception {
        Field f = SecurityCamRetrievalService.class.getDeclaredField("cameraList");
        f.setAccessible(true);
        return (Map<String, Entity>) f.get(svc);
    }

    @Test
    void getDetectorComp_returnsDetectorWhenRegistered() throws Exception {
        SecurityCamRetrievalService svc = new SecurityCamRetrievalService();

        Entity camera = new Entity();
        Entity target = new Entity();
        ConeDetectorComponent detector = new ConeDetectorComponent(target, "test");
        camera.addComponent(detector);

        getBackingMap(svc).put("test", camera);

        ConeDetectorComponent out = svc.getDetectorComp("test");
        assertNotNull(out);
        assertSame(detector, out);
    }

    @Test
    void getDetectorComp_getSecurityCam_returnsNullForUnknownId() throws Exception {
        SecurityCamRetrievalService svc = new SecurityCamRetrievalService();

        // unknown id
        assertNull(svc.getDetectorComp("none"));
        assertNull(svc.getSecurityCam("none"));

        // registered entity without detector comp
        Entity noDetector = new Entity();
        getBackingMap(svc).put("empty", noDetector);
        assertNull(svc.getDetectorComp("empty"));
    }

    @Test
    void registerCamera_overwritesPreviousWithSameId() throws Exception {
        SecurityCamRetrievalService svc = new SecurityCamRetrievalService();

        Entity camA = new Entity();
        Entity camB = new Entity();
        Entity target = new Entity();

        ConeDetectorComponent detA = new ConeDetectorComponent(target, "dup");
        ConeDetectorComponent detB = new ConeDetectorComponent(target, "dup");
        camA.addComponent(detA);
        camB.addComponent(detB);

        getBackingMap(svc).put("dup", camA);
        getBackingMap(svc).put("dup", camB); // overwrite

        ConeDetectorComponent out = svc.getDetectorComp("dup");
        assertNotNull(out);
        assertSame(detB, out);
    }

    @Test
    void getSecurityCam_returnsSecurityCam() throws Exception {
        SecurityCamRetrievalService svc = new SecurityCamRetrievalService();

        Entity cam = new Entity();
        getBackingMap(svc).put("test", cam);

        Entity out = svc.getSecurityCam("test");
        assertNotNull(out);
        assertSame(cam, out);
    }
}