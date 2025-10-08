package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.BossLaserAttack;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.lasers.LaserEmitterComponent;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.lighting.LightingDefaults;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.LaserRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.BossLaserAttack;

import java.lang.annotation.Target;

/**
 * A class full of static methods which construct different laser entity types
 */
public class BossLaserFactory {

    private static final int ATTACK_DAMAGE = 10;

    /**
     * Creates a new laser emitter entity rotated by {@code dir} degrees.
     * The laser can damage players and interact with reflectors.
     *
     * @param target the entity the laser will target
     * @param dir direction for the initial laser beam to face in degrees
     * @return the newly created laser emitter entity
     */
    public static Entity createLaserEmitter(Entity target, float dir) {
        // setup animation
        TextureAtlas atlas = ServiceLocator.getResourceService()
                .getAsset("images/boss.atlas", TextureAtlas.class);
        AnimationRenderComponent animator = new AnimationRenderComponent(atlas);

        if (atlas != null) {
            animator.addAnimation("bossShootLaser", 0.1f, Animation.PlayMode.LOOP);
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

        Entity laser = new Entity()
                .addComponent(new LaserEmitterComponent(dir))
                .addComponent(new BossLaserAttack(target))
                .addComponent(new LaserRenderComponent())
                .addComponent(new CombatStatsComponent(1, ATTACK_DAMAGE))
                .addComponent(animator)
                .addComponent(light);

        // start in "shoot-laser" state
        animator.startAnimation("bossShootLaser");

        return laser;
    }
}