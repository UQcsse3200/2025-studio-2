package com.csse3200.game.components.boss;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BossAnchorComponentTest {
    private CameraComponent camComp;
    private OrthographicCamera cam;

    private static float centerX(Entity e) {
        return e.getPosition().x + e.getScale().x * 0.5f;
    }
    private static float centerY(Entity e) {
        return e.getPosition().y + e.getScale().y * 0.5f;
    }

    @BeforeEach
    void setUp() {
        // Mocks for ServiceLocator
        RenderService renderService = mock(RenderService.class);
        Renderer renderer = mock(Renderer.class);
        camComp = mock(CameraComponent.class);

        cam = new OrthographicCamera();
        cam.viewportWidth = 20f;
        cam.viewportHeight = 12f;
        cam.position.set(10f, 6f, 0f);

        when(renderService.getRenderer()).thenReturn(renderer);
        when(renderer.getCamera()).thenReturn(camComp);
        when(camComp.getCamera()).thenReturn(cam);

        ServiceLocator.registerRenderService(renderService);
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    void pinsOnCreateAndUpdate_NoPhysics() {
        float margin = 1.0f, offsetY = 0.75f;

        Entity boss = new Entity();
        boss.setScale(2f, 2f);
        var anchor = new BossAnchorComponent(margin, offsetY);
        boss.addComponent(anchor);

        anchor.setEntity(boss);
        assertDoesNotThrow(anchor::create);
        assertDoesNotThrow(anchor::update);

        float left = cam.position.x - cam.viewportWidth * 0.5f;
        float expectedCx = left + margin + boss.getScale().x * 0.5f;
        float expectedCy = cam.position.y + offsetY;

        assertEquals(expectedCx, centerX(boss), 1e-4, "centerX should be left+margin+halfW");
        assertEquals(expectedCy, centerY(boss), 1e-4, "centerY should be camera mid + offset");

        // Move camera, should re-pin
        cam.position.x += 5f;
        cam.position.y -= 2f;
        anchor.update();

        float left2 = cam.position.x - cam.viewportWidth * 0.5f;
        assertEquals(left2 + margin + boss.getScale().x * 0.5f, centerX(boss), 1e-4);
        assertEquals(cam.position.y + offsetY, centerY(boss), 1e-4);
    }

    @Test
    void stableWhenCameraStatic() {
        Entity boss = new Entity();
        boss.setScale(2.5f, 1.5f);
        var anchor = new BossAnchorComponent(2.0f, 0f);
        boss.addComponent(anchor);

        anchor.setEntity(boss);
        anchor.create();
        anchor.update();
        float cx = centerX(boss), cy = centerY(boss);

        // Multiple updates without camera changes should not move the boss
        for (int i = 0; i < 4; i++) anchor.update();

        assertEquals(cx, centerX(boss), 1e-4);
        assertEquals(cy, centerY(boss), 1e-4);
    }

    @Test
    void followsCameraHorizontally() {
        Entity boss = new Entity();
        boss.setScale(2f, 2f);
        var anchor = new BossAnchorComponent(1.0f, 0f);
        boss.addComponent(anchor);

        anchor.setEntity(boss);
        anchor.create();
        anchor.update();

        float before = centerX(boss);
        cam.position.x += 3f; // move camera right
        anchor.update();
        assertTrue(centerX(boss) > before, "centerX should increase when camera moves right");
    }

    @Test
    void verticalOffsetApplied() {
        float offsetY = 1.5f;
        Entity boss = new Entity();
        boss.setScale(1.6f, 2.0f);
        var anchor = new BossAnchorComponent(1.0f, offsetY);
        boss.addComponent(anchor);

        anchor.setEntity(boss);
        anchor.create();
        anchor.update();

        assertEquals(cam.position.y + offsetY, centerY(boss), 1e-4);
    }

    @Test
    void physicsBody_isTransformed() {
        float margin = 1.0f, offsetY = 0f;

        // Mock PhysicsComponent
        PhysicsComponent phys = mock(PhysicsComponent.class);
        Body body = mock(Body.class);
        when(phys.getBody()).thenReturn(body);
        when(body.getAngle()).thenReturn(0f); // used by component

        Entity boss = new Entity();
        boss.setScale(2f, 2f);

        var anchor = new BossAnchorComponent(margin, offsetY);
        boss.addComponent(phys);
        boss.addComponent(anchor);

        anchor.setEntity(boss);
        anchor.create();
        anchor.update();

        float left = cam.position.x - cam.viewportWidth * 0.5f;
        float cx = left + margin + boss.getScale().x * 0.5f;
        float cy = cam.position.y + offsetY;

        verify(body).setGravityScale(0f);
        verify(body).setFixedRotation(true);
        verify(body, atLeastOnce()).setTransform(cx, cy, 0f);
        verify(body, atLeastOnce()).setLinearVelocity(0f, 0f);

        // Move camera and verify it places again with new X
        cam.position.x += 2f;
        anchor.update();
        float left2 = cam.position.x - cam.viewportWidth * 0.5f;
        float cx2 = left2 + margin + boss.getScale().x * 0.5f;
        verify(body, atLeastOnce()).setTransform(cx2, cy, 0f);
    }

    @Test
    void nullCamera_noThrow() {
        when(camComp.getCamera()).thenReturn(null);

        Entity boss = new Entity();
        boss.setScale(2f, 2f);
        var anchor = new BossAnchorComponent(1.0f, 0f);
        boss.addComponent(anchor);

        anchor.setEntity(boss);
        assertDoesNotThrow(anchor::create);
        assertDoesNotThrow(anchor::update);
    }
}
