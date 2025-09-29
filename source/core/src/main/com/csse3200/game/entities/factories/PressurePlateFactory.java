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
    /**
     * Creates a general pressure plate that can be pressed by the player
     *
     * @return a new entity of the pressure plate
     */
    public static Entity createPressurePlate() {
        Entity plate = new Entity();
        plate.addComponent(new TextureRenderComponent("images/pressure_plate_unpressed.png"));
        plate.addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody));
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(PhysicsLayer.OBSTACLE);
        collider.setSensor(false);
        plate.addComponent(collider);
        plate.addComponent(new PressurePlateComponent());
        plate.setScale(1f, 1f);
        return plate;
    }

    /**
     * Creates a pressure plate that can only be pressed down by weighted boxes and the player
     *
     * @return a new entity of the box pressable only pressure plate
     */
    public static Entity createBoxOnlyPlate() {
        Entity plate = new Entity();
        plate.addComponent(new TextureRenderComponent("images/pressure_plate_unpressed.png"));
        plate.addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody));
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(PhysicsLayer.OBSTACLE);
        collider.setSensor(false);
        plate.addComponent(collider);
        plate.addComponent(new BoxPressurePlateComponent());
        plate.setScale(1f, 1f);
        return plate;
    }

    private PressurePlateFactory() {
        throw new IllegalStateException("Cannot instantiate static factory class");
    }
}
