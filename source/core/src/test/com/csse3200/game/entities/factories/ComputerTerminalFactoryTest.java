package com.csse3200.game.entities.factories;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.computerterminal.ComputerTerminalComponent;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Tests for ComputerTerminalFactory. */
@ExtendWith(GameExtension.class)
class ComputerTerminalFactoryTest {

    @BeforeEach
    void setUp() {
        ServiceLocator.registerPhysicsService(new PhysicsService());

        ResourceService resources = mock(ResourceService.class);
        TextureAtlas atlas = mock(TextureAtlas.class);
        when(resources.getAsset("images/animated-monitors.atlas", TextureAtlas.class))
                .thenReturn(atlas);

        // Must be AtlasRegion, not TextureRegion
        Array<TextureAtlas.AtlasRegion> regions = new Array<>();
        regions.add(mock(TextureAtlas.AtlasRegion.class)); // no GL needed

        when(atlas.findRegions("terminal")).thenReturn(regions);

        ServiceLocator.registerResourceService(resources);
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    @DisplayName("createTerminal wires all expected components and properties")
    void createAddsComponentsAndColliderIsSensor() {
        Entity t = ComputerTerminalFactory.createTerminal();

        // Components present
        assertNotNull(t.getComponent(AnimationRenderComponent.class));
        assertNotNull(t.getComponent(PhysicsComponent.class));
        assertNotNull(t.getComponent(ColliderComponent.class));
        assertNotNull(t.getComponent(TooltipSystem.TooltipComponent.class));
        assertNotNull(t.getComponent(ComputerTerminalComponent.class));

        // Collider setup
        ColliderComponent col = t.getComponent(ColliderComponent.class);
        assertTrue(col.getIsSensor(), "Terminal collider should be a sensor");
        assertEquals(PhysicsLayer.OBSTACLE, col.getLayer(), "Wrong physics layer");

        // Animation can be started (factory already did this, but calling again should be safe)
        assertDoesNotThrow(() ->
                t.getComponent(AnimationRenderComponent.class).startAnimation("terminal")
        );
    }

    @Test
    @DisplayName("Factory class is non-instantiable")
    void cannotInstantiateUtilityClass() throws Exception {
        Constructor<ComputerTerminalFactory> ctor =
                ComputerTerminalFactory.class.getDeclaredConstructor();
        ctor.setAccessible(true);

        InvocationTargetException ite =
                assertThrows(InvocationTargetException.class, ctor::newInstance);

        // unwrap and assert the real reason
        Throwable cause = ite.getCause();
        assertTrue(cause instanceof IllegalStateException);
        assertEquals("Instantiating static util class", cause.getMessage());
    }
}
