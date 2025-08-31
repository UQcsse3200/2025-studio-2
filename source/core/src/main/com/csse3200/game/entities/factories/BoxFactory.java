package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.BoxComponent;
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
     * Its texture is currently a placeholder and can be replaced with a pixel image.
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
     * Its texture is currently a placeholder and can be replaced with a pixel image.
     * <p>
     * Its dynamic body type makes it moveable.
     * [Not finished:  The player can push (implemented), pull, pick up, and throw the box.
     * The box will fall if pushed or thrown through platform gaps.  When thrown, the box can
     * be used to damage enemies.  It can also be destroyed.]
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
                .addComponent(new BoxComponent());

        moveableBox.setScale(0.5f, 0.5f);
        return moveableBox;
    }
}
