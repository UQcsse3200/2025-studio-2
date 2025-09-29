package com.csse3200.game.components;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.PressurePlateFactory;
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
class BoxPressurePlateComponentTest {
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
    void create_hasExpectedComponentsAndConfig() {
        Entity plate = PressurePlateFactory.createBoxOnlyPlate();

        // components
        assertNotNull(plate.getComponent(TextureRenderComponent.class));
        PhysicsComponent pc = plate.getComponent(PhysicsComponent.class);
        assertNotNull(pc);
        ColliderComponent collider = plate.getComponent(ColliderComponent.class);
        assertNotNull(collider);
        assertNotNull(plate.getComponent(BoxPressurePlateComponent.class));

        // collider config
        assertNotNull(collider);
        assertEquals(PhysicsLayer.OBSTACLE, collider.getLayer(), "Plate layer");
        assertEquals(PhysicsLayer.OBSTACLE, collider.getLayer(), "Plate layer");

        // default scale
        assertEquals(32f / 21f, plate.getScale().x, 0.0001f);
        assertEquals(0.5f, plate.getScale().y, 0.0001f);
    }

    @Test
    void defaultScale_isOneByOne() {
        Entity plate = PressurePlateFactory.createBoxOnlyPlate();
        assertEquals(32f / 21f, plate.getScale().x, 0.0001f);
        assertEquals(0.5f, plate.getScale().y, 0.0001f);
    }
}