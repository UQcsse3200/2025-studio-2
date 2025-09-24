package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.CodexTerminalComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

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
        terminal.addComponent(new TextureRenderComponent("images/terminal.png"));
        // Add physics
        terminal.addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody));
        // Add collider
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(PhysicsLayer.OBSTACLE);
        collider.setSensor(false);
        terminal.addComponent(collider);

        // Add terminal-specific component
        terminal.addComponent(new CodexTerminalComponent());

        terminal.setScale(0.5f, 0.5f);

        return terminal;
    }
}