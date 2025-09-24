package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.CodexTerminalComponent;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Factory class for spawning terminals used to unlock codex entries.
 */
public class CodexTerminalFactory {
    private CodexTerminalFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

    public static Entity createTerminal() {
        Entity terminal = new Entity();

        // Add texture
        terminal.addComponent(new TextureRenderComponent("images/terminal_on.png"));
        // Add physics
        terminal.addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody));
        // Add collider
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(PhysicsLayer.OBSTACLE);
        collider.setSensor(true);
        terminal.addComponent(collider);

        // Add terminal-specific component
        terminal.addComponent(new CodexTerminalComponent());

        terminal.setScale(0.5f, 0.5f);
        PhysicsUtils.setScaledCollider(terminal, 0.5f, 0.5f);

        // Enable lighting effect
        Color coneColor = new Color(0f, 1f, 0f, 0.6f);
        ConeLightComponent cone = new ConeLightComponent(
                ServiceLocator.getLightingService().getEngine().getRayHandler(),
                128,
                coneColor,
                1.5f,
                0f,
                180f
        );
        terminal.addComponent(cone);

        // Add tooltip
        terminal.addComponent(new TooltipSystem.TooltipComponent(
                "Interact to add codex entry",
                TooltipSystem.TooltipStyle.DEFAULT)
        );

        return terminal;
    }
}