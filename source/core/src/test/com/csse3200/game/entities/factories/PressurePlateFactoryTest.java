package com.csse3200.game.entities.factories;

import com.csse3200.game.components.BoxPressurePlateComponent;
import com.csse3200.game.components.PressurePlateComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class PressurePlateFactoryTest {
    private static final String[] PLATE_TEXTURES = {
            "images/plate.png",
            "images/plate-pressed.png"
    };

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.registerPhysicsService(new PhysicsService());
        ServiceLocator.getResourceService().loadTextures(PLATE_TEXTURES);
        while (!ServiceLocator.getResourceService().loadForMillis(5)) { /* spin */ }
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.getResourceService().unloadAssets(PLATE_TEXTURES);
        ServiceLocator.clear();
    }

    @Test
    void createPressurePlate_hasExpectedComponents() {
        Entity plate = PressurePlateFactory.createPressurePlate();

        assertNotNull(plate.getComponent(TextureRenderComponent.class));
        assertNotNull(plate.getComponent(PhysicsComponent.class));
        ColliderComponent collider = plate.getComponent(ColliderComponent.class);
        assertNotNull(collider);
        assertNotNull(plate.getComponent(PressurePlateComponent.class));

        assertNotNull(collider);
        assertEquals(PhysicsLayer.OBSTACLE, collider.getLayer(), "Plate layer");
        assertEquals(PhysicsLayer.OBSTACLE, collider.getLayer());
    }

    @Test
    void createBoxOnlyPlate_hasBoxPressurePlateComponent() {
        Entity plate = PressurePlateFactory.createBoxOnlyPlate();

        assertNotNull(plate.getComponent(TextureRenderComponent.class));
        assertNotNull(plate.getComponent(PhysicsComponent.class));
        assertNotNull(plate.getComponent(ColliderComponent.class));
        assertNotNull(plate.getComponent(BoxPressurePlateComponent.class));
        assertNull(plate.getComponent(PressurePlateComponent.class),
                "Box-only plate should not attach the generic player-pressable component");
    }
}