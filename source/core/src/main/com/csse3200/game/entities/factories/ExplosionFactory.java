package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.npc.ExplosionAnimationController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Factory to create explosion entities.
 */
public class ExplosionFactory {

    /**
     * Creates an explosion entity. The entity will play its animation and then dispose of itself.
     * @param position The position where the explosion should occur.
     * @param radius The radius of the explosion, used to calculate the visual size of the animation.
     * @return A new explosion entity.
     */
    public static Entity createExplosion(Vector2 position, float radius) {
        // Get the drone picture album because the explosion animation is inside
        AnimationRenderComponent animator = new AnimationRenderComponent(
                ServiceLocator.getResourceService().getAsset("images/drone.atlas", TextureAtlas.class));

        // Add bomb_effect animation, set to Animation.PlayMode.NORMAL to make sure it plays only once
        animator.addAnimation("bomb_effect", 0.05f, Animation.PlayMode.NORMAL);

        Entity explosion = new Entity()
                .addComponent(animator)
                .addComponent(new ExplosionAnimationController()); // This controller will destroy the entity after the animation is over

        float visualScale = radius * 0.5f; // Adjust this coefficient to match the actual explosion range
        explosion.setScale(visualScale, visualScale);

        // Set the visual size of the animation according to the explosion radius
        // explosion.getComponent(AnimationRenderComponent.class).scaleEntity();

        // Set the explosion center point at the bomb's position
        explosion.setPosition(position.x, position.y);

        /*Vector2 bottomLeftPos = explosion.getCenterPosition().cpy().sub(visualScale / 2f, visualScale / 2f);
        explosion.setPosition(bottomLeftPos);*/

        return explosion;
    }
}