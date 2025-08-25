package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.ButtonComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

public class ButtonFactory {
    public static Entity createButton(boolean isPressed, String type) {

        Entity button = new Entity();

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


        button.addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody));

        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(PhysicsLayer.OBSTACLE);
        collider.setSensor(false);
        button.addComponent(collider);

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
