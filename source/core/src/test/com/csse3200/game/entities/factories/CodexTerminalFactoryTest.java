package com.csse3200.game.entities.factories;

import com.csse3200.game.components.CodexTerminalComponent;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.lighting.LightingService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.CodexEntry;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

@ExtendWith(GameExtension.class)
public class CodexTerminalFactoryTest {
    @BeforeEach
    void setUp() {
        ServiceLocator.registerPhysicsService(new PhysicsService());

        // Load other service dependencies, but mock
        ServiceLocator.registerLightingService(mock(LightingService.class, RETURNS_DEEP_STUBS));
        ServiceLocator.registerResourceService(mock(ResourceService.class));
    }

    @Test
    @DisplayName("Creating terminal adds all needed components")
    void createAddsComponents() {
        // Create terminal that holds entry
        CodexEntry entry = new CodexEntry("Test Title", "Test Content");
        Entity terminal = CodexTerminalFactory.createTerminal(entry);

        // Ensure terminal entity holds all needed components
        assertNotNull(terminal.getComponent(TextureRenderComponent.class));
        assertNotNull(terminal.getComponent(PhysicsComponent.class));
        assertNotNull(terminal.getComponent(ColliderComponent.class));
        assertNotNull(terminal.getComponent(CodexTerminalComponent.class));
        assertNotNull(terminal.getComponent(TooltipSystem.TooltipComponent.class));
        assertNotNull(terminal.getComponent(ConeLightComponent.class));
    }
}
