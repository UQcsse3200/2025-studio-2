package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.AutonomousBoxComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class BoxFactoryTest {

    @BeforeEach
    void setupGameServices() {
        // Register PhysicsService to initialise a box's physics body during tests
        ServiceLocator.registerPhysicsService(new PhysicsService());

        // Mock ResourceService so assets won't throw exceptions
        ResourceService mockResourceService = mock(ResourceService.class);
        when(mockResourceService.getAsset(anyString(), any())).thenReturn(null);
        ServiceLocator.registerResourceService(mockResourceService);
    }

    @Test
    void createStaticBox_hasAllComponents() {
        Entity staticBox = BoxFactory.createStaticBox();
        assertNotNull(staticBox.getComponent(TextureRenderComponent.class),
                "Static Box should have a TextureRendererComponent");
        assertNotNull(staticBox.getComponent(PhysicsComponent.class),
                "Static Box should have a PhysicsComponent");
        assertNotNull(staticBox.getComponent(ColliderComponent.class),
                "Static Box should have a ColliderComponent");
    }

    @Test
    void createStaticBox_isStatic() {
        Entity staticBox = BoxFactory.createStaticBox();

        PhysicsComponent physics = staticBox.getComponent(PhysicsComponent.class);
        assertEquals(BodyDef.BodyType.StaticBody, physics.getBody().getType(),
                "Static Box PhysicsComponent should have a static body type");
    }

    @Test
    void createMoveableBox_hasAllComponents() {
        Entity moveableBox = BoxFactory.createMoveableBox();
        assertNotNull(moveableBox.getComponent(TextureRenderComponent.class),
                "Moveable Box should have a TextureRendererComponent");
        assertNotNull(moveableBox.getComponent(PhysicsComponent.class),
                "Moveable Box should have a PhysicsComponent");
        assertNotNull(moveableBox.getComponent(ColliderComponent.class),
                "Moveable Box should have a ColliderComponent");
    }

    @Test
    void createMoveableBox_isMoveable() {
        Entity moveableBox = BoxFactory.createMoveableBox();
        PhysicsComponent physics = moveableBox.getComponent(PhysicsComponent.class);
        assertEquals(BodyDef.BodyType.DynamicBody, physics.getBody().getType(),
                "Moveable Box PhysicsComponent should have a dynamic body type");
    }

    @Test
    void createAutonomousBox_hasAllComponents() {
        Entity autonomousBox = BoxFactory.createAutonomousBox(3f, 10f, 3f, 3f, 2f, 1, 5, 2f);
        assertNotNull(autonomousBox.getComponent(TextureRenderComponent.class),
                "Autonomous Box should have a TextureRendererComponent");
        assertNotNull(autonomousBox.getComponent(PhysicsComponent.class),
                "Autonomous Box should have a PhysicsComponent");
        assertNotNull(autonomousBox.getComponent(ColliderComponent.class),
                "Autonomous Box should have a ColliderComponent");
    }

    @Test
    void createAutonomousBox_isKinematic() {
        Entity autonomousBox = BoxFactory.createAutonomousBox(3f, 10f, 3f, 3f, 2f, 1, 5, 2f);
        PhysicsComponent physics = autonomousBox.getComponent(PhysicsComponent.class);
        assertEquals(BodyDef.BodyType.KinematicBody, physics.getBody().getType(),
                "Autonomous Box PhysicsComponent should have a kinematic body type");
    }

    @Test
    void createAutonomousBox_setsBoundsAndSpeed() {
        float leftX = 3f;
        float rightX = 10f;
        float speed = 2f;
        Entity autonomousBox = BoxFactory.createAutonomousBox(3f, 10f, 3f, 3f, 2f, 1, 5, 2f);
        AutonomousBoxComponent component = autonomousBox.getComponent(AutonomousBoxComponent.class);

        assertEquals(
                leftX,
                component.getLeftX(),
                "Left bound should match value passed to factory"
        );
        assertEquals(
                rightX,
                component.getRightX(),
                "Right bound should match value passed to factory"
        );
        assertEquals(
                speed,
                component.getSpeed(),
                "Speed should match value passed to factory"
        );
    }
}
