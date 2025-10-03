package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

public class SignpostFactory {
    static final String up = "images/upSignpost.png";
    static final String down = "images/downSignpost.png";
    static final String left = "images/leftSignpost.png";
    static final String right = "images/rightSignpost.png";
    static final String standard = "images/signpost.png";

    public static Entity createSignpost(String direction) {
        Entity signpost = new Entity();
        switch (direction) {
            case "up" -> {
                TextureRenderComponent textureRenderComponent = new TextureRenderComponent(up);
                textureRenderComponent.setLayer(0);
                signpost.addComponent(textureRenderComponent);
            }
            case "down" -> {
                TextureRenderComponent textureRenderComponent = new TextureRenderComponent(down);
                textureRenderComponent.setLayer(0);
                signpost.addComponent(textureRenderComponent);
            }
            case "left" -> {
                TextureRenderComponent textureRenderComponent = new TextureRenderComponent(left);
                textureRenderComponent.setLayer(0);
                signpost.addComponent(textureRenderComponent);
            }
            case "right" -> {
                TextureRenderComponent textureRenderComponent = new TextureRenderComponent(right);
                textureRenderComponent.setLayer(0);
                signpost.addComponent(textureRenderComponent);
            }
            default ->  {
                TextureRenderComponent textureRenderComponent = new TextureRenderComponent(standard);
                textureRenderComponent.setLayer(0);
                signpost.addComponent(textureRenderComponent);
            }
        }
        return signpost;
    }
}
