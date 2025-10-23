package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.BoxPressurePlateComponent;
import com.csse3200.game.components.PressurePlateComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(GameExtension.class)
class PressurePlateFactoryTest {
    @BeforeEach
    void setUp() {
        ServiceLocator.clear();
        ServiceLocator.registerResourceService(mock(ResourceService.class));

        PhysicsService physicsService = new PhysicsService();
        ServiceLocator.registerPhysicsService(physicsService);
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    void createPressurePlate_hasExpectedComponents() {
        Entity plate = PressurePlateFactory.createPressurePlate();

        assertNotNull(plate.getComponent(TextureRenderComponent.class),
                "Pressure Plate should have a TextureRendererComponent");
        assertNotNull(plate.getComponent(PhysicsComponent.class),
                "Pressure plate should have a PhysicsComponent");
        assertNotNull(plate.getComponent(ColliderComponent.class),
                "Pressure plate should have a ColliderComponent");
        assertNotNull(plate.getComponent(PressurePlateComponent.class),
                "Pressure plate should have a PressurePlateComponent");
        assertNull(plate.getComponent(BoxPressurePlateComponent.class),
                "Pressure plate should not have a BoxPressurePlateComponent");
    }

    @Test
    void createPressurePlate_isStatic() {
        Entity plate = PressurePlateFactory.createPressurePlate();

        PhysicsComponent physics = plate.getComponent(PhysicsComponent.class);
        assertEquals(BodyDef.BodyType.StaticBody, physics.getBody().getType(),
                "Pressure Plate PhysicsComponent should have a static body type");
    }

    @Test
    void createPressurePlate_isOnObstacleLayer() {
        Entity plate = PressurePlateFactory.createPressurePlate();

        ColliderComponent collider = plate.getComponent(ColliderComponent.class);
        assertEquals(PhysicsLayer.OBSTACLE, collider.getLayer(),
                "Pressure Plate ColliderComponent should be on OBSTACLE layer");
    }

    @Test
    void createBoxOnlyPlate_hasExpectedComponents() {
        Entity plate = PressurePlateFactory.createBoxOnlyPlate();

        assertNotNull(plate.getComponent(TextureRenderComponent.class),
                "Box only pressure plate should have a TextureRendererComponent");
        assertNotNull(plate.getComponent(PhysicsComponent.class),
                "Box only pressure plate should have a PhysicsComponent");
        assertNotNull(plate.getComponent(ColliderComponent.class),
                "Box only pressure plate should have a ColliderComponent");
        assertNotNull(plate.getComponent(BoxPressurePlateComponent.class),
                "Box only pressure plate should have a BoxPressurePlateComponent");
        assertNull(plate.getComponent(PressurePlateComponent.class),
                "Box only pressure plate should not have PressurePlateComponent");
    }

    @Test
    void createBoxOnlyPlate_isStatic() {
        Entity plate = PressurePlateFactory.createBoxOnlyPlate();

        PhysicsComponent physics = plate.getComponent(PhysicsComponent.class);
        assertEquals(BodyDef.BodyType.StaticBody, physics.getBody().getType(),
                "Box Only Pressure Plate PhysicsComponent should have a static body type");
    }

    @Test
    void createBoxOnlyPlate_isOnObstacleLayer() {
        Entity plate = PressurePlateFactory.createBoxOnlyPlate();

        ColliderComponent collider = plate.getComponent(ColliderComponent.class);
        assertEquals(PhysicsLayer.OBSTACLE, collider.getLayer(),
                "Box Only Pressure Plate ColliderComponent should be on OBSTACLE layer");
    }
}