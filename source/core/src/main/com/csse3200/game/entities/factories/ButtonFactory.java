package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.ButtonComponent;
import com.csse3200.game.components.ButtonManagerComponent;
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
     * Uses getTextureRenderComponent to assign texture colour and state depending on type and pressed
     *
     * @param isPressed whether button should start in pressed state or not
     * @param type type of button (door, platform, standard)
     *
     * @return fully configured button entity
     */
    public static Entity createButton(boolean isPressed, String type, String direction) {

        Entity button = new Entity();
        TextureRenderComponent render = getTextureRenderComponent(isPressed, type);

        if(direction != null) {
            direction = direction.toLowerCase();
            switch (direction) {
                case "right":
                    render.setRotation(180f);
                    break;
                case "down":
                    render.setRotation(90f);
                    break;
                case "up":
                    render.setRotation(270f);
                    break;
                case "left":
                default:
                    render.setRotation(0f);
                    break;
            }
        }
        button.addComponent(render);
        //add physics and collider components
        button.addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody));

        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(PhysicsLayer.OBSTACLE);
        collider.setSensor(false);
        button.addComponent(collider);

        //add button component and set type
        ButtonComponent buttonComponent = new ButtonComponent();
        buttonComponent.setType(type);
        buttonComponent.setDirection(direction);
        button.addComponent(buttonComponent);

        button.setScale(0.5f, 0.5f);

        return button;
    }

    /**
     *  Creates button entity that is part of button puzzle
     *  Button is linked to a ButtonManagerComponent, which tracks all buttons assigned to the puzzle
     *
     * @param isPressed whether button should start in pressed state or not
     * @param type type of button (door, platform, standard) (standard if not recognised)
     * @param direction the direction the button should face (left, right, up, down) (left if not recognised)
     * @param manager ButtonManagerComponent managing this button's puzzle
     *
     * @return fully configured puzzle entity button
     */
    public static Entity createPuzzleButton(boolean isPressed, String type, String direction, ButtonManagerComponent manager) {
        Entity button = createButton(isPressed, type, direction);

        ButtonComponent buttonComp = button.getComponent(ButtonComponent.class);
        if (buttonComp != null && manager != null) {
            buttonComp.setPuzzleManager(manager);
            manager.addButton(buttonComp);
        }

        return button;
    }

    /**
     * Returns a TextureRenderComponent with correct texture based on button's type and pressed state
     *
     * @param isPressed whether button is initially pressed
     * @param type type of button (door, platform, standard) (standard if not recognised)
     *
     * @return a TextureRenderComponent with the appropriate texture
     */
    private static TextureRenderComponent getTextureRenderComponent(boolean isPressed, String type) {
        String texture;
        //set texture based on type
        if(type.equals("platform")) {
            texture = isPressed ? "images/blue_button_pushed.png" : "images/blue_button.png";
        }else if(type.equals("door")) {
            texture = isPressed ? "images/red_button_pushed.png" : "images/red_button.png";
        }else {
            texture = isPressed ? "images/button_pushed.png" : "images/button.png";
        }
        return new TextureRenderComponent(texture);
    }

    private ButtonFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}
