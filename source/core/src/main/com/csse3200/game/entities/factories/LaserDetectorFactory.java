package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.lasers.LaserDetectorComponent;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.lighting.LightingDefaults;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * This is just a class of static methods used to assemble new laser detector
 * entities. They're able to detect when a laser is hitting them and then trigger
 * an event when the state is changed.
 */
public class LaserDetectorFactory {

    /**
     * Creates a new laser detector entity facing upright (unrotated)
     *
     * @return the newly created laser detector entity
     */
    public static Entity createLaserDetector() {
        Entity e = new Entity();
        e.setScale(16f/ 25f, 0.5f);

        e.addComponent(new TextureRenderComponent("images/laser-detector-off.png"));
        ColliderComponent collider = new ColliderComponent().setLayer(PhysicsLayer.LASER_DETECTOR);
        e.addComponent(collider);

        float scaleX = e.getScale().x;
        float scaleY = e.getScale().y;
        float unitsPerPxX = scaleX / 32f;
        float unitsPerPxY = scaleY / 25f;

        Vector2 newScale = new Vector2(14 * unitsPerPxX, 18 * unitsPerPxY);
        Vector2 newPos   = e.getCenterPosition().cpy().add(0f, 2f * unitsPerPxY);

        collider.setAsBox(newScale, newPos);

        e.addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody));

        ConeLightComponent light = new ConeLightComponent(
                ServiceLocator.getLightingService().getEngine().getRayHandler(),
                LightingDefaults.RAYS,
                Color.RED,
                1f,
                0f,
                180f
        );
        light.setFollowEntity(false);
        e.addComponent(light);

        e.addComponent(new LaserDetectorComponent());

        return e;
    }

    /**
     * Creates a new laser detector entity with a rotation of {@code dir}.
     *
     * @param dir the rotation in degrees
     * @return the newly created laser detector entity
     */
    public static Entity createLaserDetector(float dir) {
        Entity e = createLaserDetector();

        TextureRenderComponent texture = e.getComponent(TextureRenderComponent.class);
        texture.setOrigin(0f, 0f);
        texture.setRotation(dir);

        return e;
    }
}
