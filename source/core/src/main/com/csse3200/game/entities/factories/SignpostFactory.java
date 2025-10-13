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
    static final String UP = "images/upSignpost.png";
    static final String DOWN = "images/downSignpost.png";
    static final String LEFT = "images/leftSignpost.png";
    static final String RIGHT = "images/rightSignpost.png";
    static final String STANDARD = "images/signpost.png";

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
        TextureRenderComponent textureRenderComponent = new TextureRenderComponent(switch (direction) {
          case "up" -> UP;
          case "down" -> DOWN;
          case "left" -> LEFT;
          case "right" -> RIGHT;
          default -> STANDARD;
        });
        textureRenderComponent.setLayer(0);
        return new Entity().addComponent(textureRenderComponent);
    }
}
