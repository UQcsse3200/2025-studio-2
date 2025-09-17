package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.PressurePlateComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.components.BoxPressurePlateComponent;
import com.csse3200.game.physics.components.HitboxComponent;
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

        String texturePath = "images/pressure_plate_unpressed.png";
        plate.addComponent(new TextureRenderComponent(texturePath));
        plate.addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody));

        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(PhysicsLayer.OBSTACLE);
        collider.setSensor(true);
        plate.addComponent(collider);

        // Attach pressure plate logic
        plate.addComponent(new PressurePlateComponent());

        plate.setScale(DEFAULT_SCALE, DEFAULT_SCALE);

        return plate;
    }

    public static Entity createBoxOnlyPlate() {
        Entity plate = new Entity();

        plate.addComponent(new TextureRenderComponent("images/pressure_plate_unpressed.png"));
        plate.addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody));

        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(PhysicsLayer.OBSTACLE);
        collider.setSensor(true); // trigger-only
        plate.addComponent(collider);

        BoxPressurePlateComponent comp = new BoxPressurePlateComponent();
        plate.addComponent(comp);
        comp.setTextures("images/pressure_plate_unpressed.png",
                "images/pressure_plate_pressed.png");

        plate.setScale(DEFAULT_SCALE, DEFAULT_SCALE);
        return plate;
    }


    private PressurePlateFactory() {
        throw new IllegalStateException("Cannot instantiate static factory class");
    }
}
