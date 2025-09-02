package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.AutonomousBoxComponent;
import com.csse3200.game.components.MoveableBoxComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Factory class for creating box entities in the game.
 * <p>
 * Types of boxes that can be created include: <br>
 * -  white static (immovable) <br>
 * -  blue movable
 */
public class BoxFactory {

    /**
     * Private constructor to prevent instantiation of non-static box instances and remove gradle
     * warnings about missing constructor in this class.
     */
    private BoxFactory() {
        // Intentionally blank
    }

    /**
     * Creates a static (immovable) box entity.
     * <p>
     * The box currently displays as a white square, scaled to half a game unit.
     * <p>
     * Its static body type makes it immovable.  Other physical game objects can collide with it,
     * but it does not move, react to collisions and cannot be destroyed.
     * @return A new static box Entity
     */
    public static Entity createStaticBox() {

        Entity staticBox = new Entity()
                .addComponent(new TextureRenderComponent("images/box_white.png"))
                .addComponent(new PhysicsComponent()
                        .setBodyType(BodyDef.BodyType.StaticBody))
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

        // Scaled to half a world unit
        staticBox.setScale(0.5f, 0.5f);
        return staticBox;
    }

    /**
     * Creates a dynamic (moveable) box entity.
     * <p>
     * The box currently displays as a blue square, scaled to half a game unit.
     * <p>
     * Its dynamic body type makes it moveable.  The player can automatically push the box with
     * its body, or interact with the box to lift it or drop it.  The player can move with the
     * box whilst it is lifted.
     * @return A new moveable box Entity
     */
    public static Entity createMoveableBox() {
        Entity moveableBox = new Entity()
                .addComponent(new TextureRenderComponent("images/box_blue.png"))
                .addComponent(new PhysicsComponent()
                        .setBodyType(BodyDef.BodyType.DynamicBody))
                .addComponent(new ColliderComponent()
                        .setLayer(PhysicsLayer.OBSTACLE)
                        .setDensity(1f)
                        .setRestitution(0.1f)
                        .setFriction(0.8f))
                .addComponent(new MoveableBoxComponent());

        moveableBox.setScale(0.5f, 0.5f);
        return moveableBox;
    }

    /**
     * Creates a kinematic (autonomous) box entity that can be used as moving game obstacles.
     * <p>
     * The box currently displays as an orange square, scaled to half a game unit.
     * <p>
     * The box's kinematic nature means it will not fall due to gravity or respond to collision
     * forces.  It can be set to continuously travel along a horizontal path at a set speed
     * and for a set distance, reversing direction when reaching each boundary.
     * @param leftX the left boundary
     * @param rightX the right boundary
     * @param speed the current speed
     * @return A new autonomous box Entity
     */
    public static Entity createAutonomousBox(float leftX, float rightX, float speed) {
        Entity autonomousBox = new Entity()
                .addComponent(new TextureRenderComponent("images/box_orange.png"))
                .addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.KinematicBody))
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
                .addComponent(new AutonomousBoxComponent());

        autonomousBox.setScale(0.5f, 0.5f);

        AutonomousBoxComponent autonomousBoxComponent
                = autonomousBox.getComponent(AutonomousBoxComponent.class);
        autonomousBoxComponent.setBounds(leftX, rightX);
        autonomousBoxComponent.setSpeed(speed);
        return autonomousBox;
    }
}
