package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.BoxPressurePlateComponent;
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
    /**
     * Creates a general pressure plate that can be pressed by the player
     *
     * @return a new entity of the pressure plate
     */
    public static Entity createPressurePlate() {
        Entity plate = new Entity();
        plate.addComponent(new TextureRenderComponent("images/plate.png"));
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
        plate.setScale(32f / 21f, 0.5f);
        plate.addComponent(new TextureRenderComponent("images/plate.png"));
        plate.addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody));

        ColliderComponent collider = new ColliderComponent();
        plate.addComponent(collider);

        // scale down collider
        float scaleX = plate.getScale().x;
        float scaleY = plate.getScale().y;
        float unitsPerPxY = scaleY / 21f;
        float down = 3f * unitsPerPxY;

        float unitsPerPxX = scaleX / 64f;
        float width = 40f * unitsPerPxX;

        Vector2 p = plate.getCenterPosition().cpy().sub(unitsPerPxX, down - unitsPerPxY);

        // set new collider size
        collider.setAsBox(new Vector2(width, scaleY - down), p);

        collider.setLayer(PhysicsLayer.OBSTACLE);
        collider.setSensor(false);

        plate.addComponent(new BoxPressurePlateComponent());
        return plate;
    }

    private PressurePlateFactory() {
        throw new IllegalStateException("Cannot instantiate static factory class");
    }
}
