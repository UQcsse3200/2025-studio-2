package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.computerterminal.ComputerTerminalComponent;
import com.csse3200.game.components.tooltip.TooltipSystem;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;


/**
 * Factory for creating computer terminal entities with visuals, physics,
 * interaction tooltip, and terminal behaviour pre-wired.
 */
public final class ComputerTerminalFactory {
    private ComputerTerminalFactory() { throw new IllegalStateException("Instantiating static util class"); }


    /**
     * Builds a terminal entity that:
     * Renders an animated monitor from an atlas
     * Has a static physics body with a sensor collider
     * Shows a tooltip prompting the player to press E
     * Emits interaction events via ComputerTerminalComponent
     *
     * @return a fully configured terminal entity
     */
    public static Entity createTerminal() {
        Entity terminal = new Entity();

        TextureAtlas atlas = ServiceLocator.getResourceService()
                .getAsset("images/animated-monitors.atlas", TextureAtlas.class);
        AnimationRenderComponent animator = new AnimationRenderComponent(atlas);
        animator.addAnimation("terminal", 0.08f, PlayMode.LOOP);
        animator.setLayer(-10);

        terminal.addComponent(animator);

        // non-moving body with a sensor collider
        terminal.addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody));
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(PhysicsLayer.OBSTACLE);
        collider.setSensor(true);
        terminal.addComponent(collider);

        terminal.addComponent(new TooltipSystem.TooltipComponent(
                "Press E to interact with terminal", TooltipSystem.TooltipStyle.DEFAULT));

        // handles input prompts and fires open/close events to the UI
        terminal.addComponent(new ComputerTerminalComponent());

        terminal.setScale(2.5f, 2.5f);
        PhysicsUtils.setScaledCollider(terminal, 3.0f, 3.0f);

        // Start animation after entity is assembled
        animator.startAnimation("terminal");
        return terminal;
    }
}