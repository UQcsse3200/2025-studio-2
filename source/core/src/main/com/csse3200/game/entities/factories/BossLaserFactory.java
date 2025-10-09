package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.BossLaserAttack;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.lasers.LaserEmitterComponent;
import com.csse3200.game.components.lasers.LaserShowerComponent;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.lighting.LightingDefaults;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.LaserRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * A class full of static methods which construct different laser entity types
 */
public class BossLaserFactory {

    private static final int ATTACK_DAMAGE = 1;

    /**
     * Creates a new laser emitter entity rotated by {@code dir} degrees.
     * The laser can damage players and interact with reflectors.
     *
     * @param target the entity the laser will target
     * @return the newly created laser emitter entity
     */
    public static Entity createBossLaser(Entity target, float dir) {
        // setup animation
        AnimationRenderComponent animator = new AnimationRenderComponent(
                ServiceLocator.getResourceService().getAsset("images/laser.atlas", TextureAtlas.class));
        animator.addAnimation("laser-on", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("laser-off", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("laser-turning-off", 0.1f, Animation.PlayMode.NORMAL);
        animator.addAnimation("laser-turning-on", 0.1f, Animation.PlayMode.NORMAL);
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
                .addComponent(new LaserShowerComponent(dir))
                .addComponent(new LaserEmitterComponent(dir))
                .addComponent(new LaserRenderComponent())
                .addComponent(new CombatStatsComponent(10, ATTACK_DAMAGE))
                .addComponent(animator)
                .addComponent(light);

        // start in "on" state
        animator.startAnimation("shootLaser");
        return e;
    }
}