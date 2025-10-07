package com.csse3200.game.components;

import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.CodexEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("Interacting with the terminal in range unlocks entry and changes state")
    void interactingUnlocksEntryChangesState() {
        // Create simple player entity for testing on top of terminal
        Entity player = new Entity();
        ColliderComponent mockedCollider = mock(ColliderComponent.class);
        when(mockedCollider.getEntity()).thenReturn(player);
        player.addComponent(mockedCollider);
        player.setPosition(terminal.getCenterPosition());
        player.create();

        // Make player interact with the terminal
        terminalComponent.setPlayerInRange(player.getComponent(ColliderComponent.class));
        player.getEvents().trigger("interact");

        // Ensure codex entry got unlocked
        assertTrue(codexEntry.isUnlocked());
        // Make sure texture updated, and cone light/tooltip disposed
        verify(textureRenderComponent).setTexture("images/terminal_off.png");
        verify(coneLightComponent).dispose();
        verify(tooltipComponent).dispose();
    }

    @Test
    @DisplayName("Interacting with the terminal out of range does nothing")
    void interactingOutOfRangeDoesNothing() {
        // Create simple entity for testing when player is far away
        Entity player = new Entity();
        ColliderComponent mockedCollider = mock(ColliderComponent.class);
        when(mockedCollider.getEntity()).thenReturn(player);
        player.addComponent(mockedCollider);
        player.setPosition(terminal.getCenterPosition().x + 100f,
                terminal.getCenterPosition().y + 100f);
        player.create();

        // Make player interact with the terminal
        terminalComponent.setPlayerInRange(player.getComponent(ColliderComponent.class));
        player.getEvents().trigger("interact");

        // Verify no state changes occurred
        assertFalse(codexEntry.isUnlocked());
        verify(textureRenderComponent, never()).setTexture(anyString());
        verify(coneLightComponent, never()).dispose();
        verify(tooltipComponent, never()).dispose();
    }
}
