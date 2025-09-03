package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.PressurePlateComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Factory class for creating pressure plate entities.
 */
public class PressurePlateFactory {

    private static final float DEFAULT_SCALE = 1f;

    public static Entity createPressurePlate() {
        Entity plate = new Entity();

        // Unpressed texture; adjust the path if you chose a different location
        String texturePath = "images/pressure_plate_unpressed.png";
        plate.addComponent(new TextureRenderComponent(texturePath));

        // Static physics and collider so the plate stays in place and collides with the player
        plate.addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody));

        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(PhysicsLayer.OBSTACLE);
        collider.setSensor(false);
        plate.addComponent(collider);

        // Attach pressure plate logic
        plate.addComponent(new PressurePlateComponent());

        plate.setScale(DEFAULT_SCALE, DEFAULT_SCALE);

        return plate;
    }

    private PressurePlateFactory() {
        throw new IllegalStateException("Cannot instantiate static factory class");
    }
}
