
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

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();

        // Mock cam service
        camService = mock(SecurityCamRetrievalService.class);
        ServiceLocator.registerSecurityCamRetrievalService(camService);
    }

    @Test
    void activation_linksImmediately() {
        // Camera exists before enemy.create()
        Entity camera = new Entity();
        camera.create();
        when(camService.getSecurityCam("1")).thenReturn(camera);

        Entity e = new Entity();
        e.addComponent(new ActivationComponent("1"));
        e.create();

        ActivationComponent ac = e.getComponent(ActivationComponent.class);
        assertTrue(ac.isLinked(), "Camera should be linked on create()");
    }

    @Test
    void activation_linksOnUpdate() {
        // Initially no camera
        when(camService.getSecurityCam("1")).thenReturn(null);
        Entity e = new Entity();
        e.addComponent(new ActivationComponent("1"));
        e.create();

        ActivationComponent ac = e.getComponent(ActivationComponent.class);
        assertFalse(ac.isLinked(), "Should not be linked before camera appears");

        // Camera appears later
        Entity camera = new Entity();
        camera.create();
        when(camService.getSecurityCam("1")).thenReturn(camera);

        e.update();
        assertTrue(ac.isLinked(), "Camera should link on update()");
    }

    @Test
    void activation_mismatchIdNoLink() {
        // Camera exists under different id
        Entity camera = new Entity();
        camera.create();
        when(camService.getSecurityCam("1")).thenReturn(camera);

        Entity e = new Entity();
        e.addComponent(new ActivationComponent("2"));
        e.create();
        e.update();

        ActivationComponent ac = e.getComponent(ActivationComponent.class);
        assertFalse(ac.isLinked(), "Should not link when camera IDs mismatch");
    }

    @Test
    void activation_triggersEnemyActivate() {
        Entity camera = new Entity();
        camera.create();
        when(camService.getSecurityCam("1")).thenReturn(camera);

        Entity e = new Entity();
        e.addComponent(new ActivationComponent("1"));
        e.create();

        EventListener0 onActivated = mock(EventListener0.class);
        e.getEvents().addListener("enemyActivated", onActivated);

        camera.getEvents().trigger("targetDetected", e);
        verify(onActivated, times(1)).handle();
    }

    @Test
    void activation_triggersEnemyDeactivate() {
        Entity camera = new Entity();
        when(camService.getSecurityCam("1")).thenReturn(camera);

        Entity e = new Entity();
        e.addComponent(new ActivationComponent("1"));
        e.create();

        EventListener0 onDeactivated = mock(EventListener0.class);
        e.getEvents().addListener("enemyDeactivated", onDeactivated);

        camera.getEvents().trigger("targetLost", e);
        verify(onDeactivated, times(1)).handle();
    }
}
