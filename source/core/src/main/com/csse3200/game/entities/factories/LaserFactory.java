package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.lasers.LaserEmitterComponent;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.lighting.LightingDefaults;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.LaserRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * A class full of static methods which construct different laser entity types
 */
public class LaserFactory {

    private static final int ATTACK_DAMAGE = 20;

    /**
     * Creates a new laser emitter entity which is rotated by {@code dir} degrees.
     * The laser is able to damage players and also reflect off of colliders with the
     * {@code PhysicsLayer.LASER_REFLECTOR} layer.
     *
     * @param dir direction for the initial laser beam to face in degrees
     * @return the newly created laser emitter entity
     */
    public static Entity createLaserEmitter(float dir) {
        // setup animations
        TextureAtlas atlas = ServiceLocator.getResourceService().getAsset("images/laser.atlas", TextureAtlas.class);
        AnimationRenderComponent animator = new AnimationRenderComponent(atlas);
        if (atlas != null) {
            animator.addAnimation("laser-on", 0.1f, Animation.PlayMode.LOOP);
            animator.addAnimation("laser-off", 0.1f, Animation.PlayMode.LOOP);
            animator.addAnimation("laser-turning-off", 0.1f, Animation.PlayMode.NORMAL);
            animator.addAnimation("laser-turning-on", 0.1f, Animation.PlayMode.NORMAL);
        }
        animator.setOrigin(0.5f, 0.5f);
        animator.setRotation(dir);
        animator.setLayer(3);

        // give soft glow on emitter
        ConeLightComponent light = new ConeLightComponent(
                ServiceLocator.getLightingService().getEngine().getRayHandler(),
                LightingDefaults.RAYS,
                Color.RED,
                1f,
                0f,
                180f
        );

        // construct entity
        Entity e = new Entity()
                .addComponent(new LaserEmitterComponent(dir))
                .addComponent(new LaserRenderComponent())
                .addComponent(new CombatStatsComponent(1, ATTACK_DAMAGE))
                .addComponent(animator)
                .addComponent(light);

        // start in "on" state
        animator.startAnimation("laser-on");
        return e;
    }
}
