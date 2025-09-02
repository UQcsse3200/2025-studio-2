package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.ButtonComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Factory class for creating button entities.
 *
 * Image texture depends on type (blue for platform, red for door and white for standard)
 * Static body means that it is immovable and attaches ButtonComponent, as well as collider so that
 *  other objects can collide and not move it.
 *
 * PhysicsLayer is OBSTACLE so that the light cannot go through it and the player can hide behind it
 */
public class ButtonFactory {

    /**
     * Creates a button entity with specified state and type
     *
     * @param isPressed whether button should start in pressed state or not
     * @param type type of button (door, platform, standard)
     *
     * @return fully configured button entity
     */
    public static Entity createButton(boolean isPressed, String type) {

        Entity button = new Entity();

        //set texture based on type
        if(type.equals("platform")) {
            String texture = isPressed ? "images/blue_button_pushed.png" : "images/blue_button.png";
            button.addComponent(new TextureRenderComponent(texture));
        }else if(type.equals("door")) {
            String texture = isPressed ? "images/red_button_pushed.png" : "images/red_button.png";
            button.addComponent(new TextureRenderComponent(texture));
        }else {
            String texture = isPressed ? "images/button_pushed.png" : "images/button.png";
            button.addComponent(new TextureRenderComponent(texture));
        }

        //add physics and collider components
        button.addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody));

        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(PhysicsLayer.OBSTACLE);
        collider.setSensor(false);
        button.addComponent(collider);

        //add button component and set type
        ButtonComponent buttonComponent = new ButtonComponent();
        buttonComponent.setType(type);
        button.addComponent(buttonComponent);

        button.setScale(0.5f, 0.5f);

        return button;
    }

    private ButtonFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}
