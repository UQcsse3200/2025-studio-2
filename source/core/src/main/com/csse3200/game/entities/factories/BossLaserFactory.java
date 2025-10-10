package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.BossLaserAttack;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

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
     * @return the newly created laser emitter entity
     */
    public static Entity createBossLaser(Entity target) {
        // setup animation
        TextureAtlas atlas = ServiceLocator.getResourceService()
                .getAsset("images/boss.atlas", TextureAtlas.class);
        AnimationRenderComponent animator = new AnimationRenderComponent(atlas);

        if (atlas != null) {
            animator.addAnimation("bossShootLaser", 0.1f, Animation.PlayMode.LOOP);
        }

        Entity laser = new Entity()
                .addComponent(new BossLaserAttack(target))
                .addComponent(new CombatStatsComponent(1, ATTACK_DAMAGE))
                .addComponent(animator);

        // start in "shoot-laser" state
        animator.startAnimation("bossShootLaser");

        return laser;
    }
}