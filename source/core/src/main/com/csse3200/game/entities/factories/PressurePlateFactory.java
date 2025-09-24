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
        plate.addComponent(new TextureRenderComponent("images/pressure_plate_unpressed.png"));
        plate.addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody));
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(PhysicsLayer.OBSTACLE);
        collider.setSensor(false);
        plate.addComponent(collider);
        plate.addComponent(new PressurePlateComponent()); // generic plate
        plate.setScale(1f, 1f);
        return plate;  // <-- DON'T FORGET THIS
    }

    public static Entity createBoxOnlyPlate() {
        Entity plate = new Entity();
        plate.addComponent(new TextureRenderComponent("images/pressure_plate_unpressed.png"));
        plate.addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody));
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(PhysicsLayer.OBSTACLE);
        collider.setSensor(false);
        plate.addComponent(collider);
        plate.addComponent(new BoxPressurePlateComponent()); // boxes-only logic
        plate.setScale(1f, 1f);
        return plate;  // <-- AND THIS
    }


    private PressurePlateFactory() {
        throw new IllegalStateException("Cannot instantiate static factory class");
    }
}
