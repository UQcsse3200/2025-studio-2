package com.csse3200.game.entities.factories;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Signpost Factory for creating signpost entities
 *
 * Image texture depends on direction for arrow to face (left, right, up or down). If anything
 *  else specified the image will be a standard signpost with no arrow
 */
public class SignpostFactory {
    static final String up = "images/upSignpost.png";
    static final String down = "images/downSignpost.png";
    static final String left = "images/leftSignpost.png";
    static final String right = "images/rightSignpost.png";
    static final String standard = "images/signpost.png";

    /**
     * Creates a signpost entity with the arrow in the specified direction
     *  - The texture is set to layer 0 so that it is always behind all other entities
     *  - There is no physics or collider component so nothing can interact with the signpost, and no collisions
     *  occur with other entities
     *
     * @param direction direction for arrow on sign to be (left, right, up, down or
     *                  standard with no arrow if anything else specified)
     *
     * @return fully configured signpost entity
     */
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
