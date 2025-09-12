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
        TextureAtlas droneAtlas = ServiceLocator.getResourceService().getAsset("images/drone.atlas", TextureAtlas.class);
        TextureAtlas SelfDestructAtlas = ServiceLocator.getResourceService().getAsset("images/SelfDestructDrone.atlas", TextureAtlas.class);

        if (droneAtlas == null) {
            throw new RuntimeException("drone.atlas not loaded");
        }
        else if (SelfDestructAtlas == null || SelfDestructAtlas.findRegion("self_destruct")==null) {
            throw new RuntimeException("SelfDestruct.atlas not loaded");
        }

        AnimationRenderComponent animator = new AnimationRenderComponent(droneAtlas);
        // Add bomb_effect animation, set to Animation.PlayMode.NORMAL to make sure it plays only once
        animator.addAnimation("bomb_effect", 0.05f, Animation.PlayMode.NORMAL);
        animator.addAnimation("self_destruct", 0.08f, Animation.PlayMode.NORMAL);

        Entity explosion = new Entity()
                .addComponent(animator)
                .addComponent(new ExplosionAnimationController()); // This controller will destroy the entity after the animation is over

        float visualScale = radius * 0.5f; // Adjust this coefficient to match the actual explosion range
        explosion.setScale(visualScale, visualScale);

        // The 'position' argument is the desired CENTER of the explosion.
        // We need to calculate the correct bottom-left position.
        Vector2 bottomLeftPos = getExplosionPosition(position, visualScale);
        explosion.setPosition(bottomLeftPos);

        return explosion;
    }

    public static Vector2 getExplosionPosition(Vector2 position, float visualScale) {
        Vector2 centerPos = position;
        float halfSize = visualScale / 2f;
        Vector2 bottomLeftPos = new Vector2(centerPos.x - halfSize, centerPos.y - halfSize);
        return bottomLeftPos;
    }
}