
package com.csse3200.game.components.enemy;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.listeners.EventListener0;
import com.csse3200.game.extensions.GameExtension;

import com.csse3200.game.lighting.SecurityCamRetrievalService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class ActivationComponentTest {
    private SecurityCamRetrievalService camService;

    private static Entity newCamera() {
        Entity cam = new Entity();
        cam.create();
        return cam;
    }

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();
        camService = mock(SecurityCamRetrievalService.class);
        ServiceLocator.registerSecurityCamRetrievalService(camService);
    }

    @Test
    void activation_linksImmediately() {
        Entity camera = newCamera();
        when(camService.getSecurityCam("1")).thenReturn(camera);

        Entity drone = new Entity();
        drone.addComponent(new ActivationComponent("1"));
        drone.create();

        ActivationComponent ac = drone.getComponent(ActivationComponent.class);
        assertTrue(ac.isLinked(), "Camera should be linked on create() when present");
    }

    @Test
    void activation_linksOnUpdate() {
        // Initially no camera (test spawn order race)
        when(camService.getSecurityCam("1")).thenReturn(null);

        Entity drone = new Entity();
        drone.addComponent(new ActivationComponent("1"));
        drone.create();

        ActivationComponent ac = drone.getComponent(ActivationComponent.class);
        assertFalse(ac.isLinked(), "Should not be linked before camera appears");

        // Add camera
        Entity camera = newCamera();
        when(camService.getSecurityCam("1")).thenReturn(camera);

        drone.update();
        assertTrue(ac.isLinked(), "Camera should link on update() when it becomes available");
    }

    @Test
    void activation_mismatchIdNoLink() {
        // Camera exists under different id
        Entity camera = newCamera();
        when(camService.getSecurityCam("1")).thenReturn(camera);

        when(camService.getSecurityCam("2")).thenReturn(null);

        Entity drone = new Entity();
        drone.addComponent(new ActivationComponent("2"));
        drone.create();
        drone.update();

        ActivationComponent ac = drone.getComponent(ActivationComponent.class);
        assertFalse(ac.isLinked(), "Should not link when camera ID does not match");
    }

    @Test
    void activation_triggersEnemyActivate() {
        Entity camera = newCamera();
        when(camService.getSecurityCam("1")).thenReturn(camera);

        Entity drone = new Entity();
        drone.addComponent(new ActivationComponent("1"));
        drone.create();
        drone.update();

        EventListener0 onActivated = mock(EventListener0.class);
        drone.getEvents().addListener("enemyActivated", onActivated);

        camera.getEvents().trigger("targetDetected", drone);
        verify(onActivated, times(1)).handle();
        verifyNoMoreInteractions(onActivated);
    }

    @Test
    void activation_triggersEnemyDeactivate() {
        Entity camera = newCamera();
        when(camService.getSecurityCam("1")).thenReturn(camera);

        Entity drone = new Entity();
        drone.addComponent(new ActivationComponent("1"));
        drone.create();
        drone.update();

        EventListener0 onDeactivated = mock(EventListener0.class);
        drone.getEvents().addListener("enemyDeactivated", onDeactivated);

        camera.getEvents().trigger("targetLost", drone);
        verify(onDeactivated, times(1)).handle();
        verifyNoMoreInteractions(onDeactivated);
    }

    @Test
    void activation_noDupListeners() {
        Entity camera = newCamera();
        when(camService.getSecurityCam("1")).thenReturn(camera);

        Entity drone = new Entity();
        drone.addComponent(new ActivationComponent("1"));
        drone.create();
        drone.update();

        // Spam updates
        for (int i = 0; i < 20; i++) {
            drone.update();
        }

        // One camera trigger should map to one enemyActivated call
        EventListener0 onActivated = mock(EventListener0.class);
        drone.getEvents().addListener("enemyActivated", onActivated);

        camera.getEvents().trigger("targetDetected", drone);
        verify(onActivated, times(1)).handle();

        camera.getEvents().trigger("targetDetected", drone);
        verify(onActivated, times(2)).handle();

        verifyNoMoreInteractions(onActivated);
    }

    @Test
    void activation_reLinksAfterReset() {
        Entity cam1 = newCamera();
        when(camService.getSecurityCam("1")).thenReturn(cam1);

        Entity drone = new Entity();
        drone.addComponent(new ActivationComponent("1"));
        drone.create();
        drone.update();
        assertTrue(drone.getComponent(ActivationComponent.class).isLinked());

        // New camera same id
        Entity cam2 = newCamera();
        when(camService.getSecurityCam("1")).thenReturn(cam2);

        // Should relink for new instance
        drone.update();

        // Check events from new camera instance reach the drone
        EventListener0 onActivated = mock(EventListener0.class);
        drone.getEvents().addListener("enemyActivated", onActivated);

        cam2.getEvents().trigger("targetDetected", drone);
        verify(onActivated, times(1)).handle();
    }
}
