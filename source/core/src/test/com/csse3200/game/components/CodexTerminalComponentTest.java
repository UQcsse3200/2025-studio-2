package com.csse3200.game.components;

import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.CodexEntry;
import com.csse3200.game.ui.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

@ExtendWith(GameExtension.class)
public class CodexTerminalComponentTest {
    /**
     * Terminal entity the component being tested is added to
     */
    private Entity terminal;
    /**
     * Component being tested
     */
    private CodexTerminalComponent terminalComponent;
    /**
     * The codex entry the terminal component/entity holds a reference to
     */
    private CodexEntry codexEntry;

    /**
     * Mocked components that are accessed by the terminal component
     */
    private TextureRenderComponent textureRenderComponent;
    private ConeLightComponent coneLightComponent;
    private TooltipSystem.TooltipComponent tooltipComponent;

    @BeforeEach
    void setUp() {
        // Create codex entry, make sure it's unlocked
        codexEntry = new CodexEntry("Test Title", "Test Text");
        assertFalse(codexEntry.isUnlocked());

        // Create component being tested
        terminalComponent = new CodexTerminalComponent(codexEntry);

        // Mock other components that should be attached to terminal entity
        textureRenderComponent = mock(TextureRenderComponent.class);
        coneLightComponent = mock(ConeLightComponent.class);
        tooltipComponent = mock(TooltipSystem.TooltipComponent.class);

        // Attach all components to terminal entity, and create
        terminal = new Entity()
                .addComponent(terminalComponent)
                .addComponent(textureRenderComponent)
                .addComponent(coneLightComponent)
                .addComponent(tooltipComponent);
        terminal.create();
    }
}
