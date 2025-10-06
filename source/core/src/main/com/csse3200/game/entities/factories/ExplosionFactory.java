package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.npc.ExplosionAnimationController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class ExplosionFactory {

    public static Entity createExplosion(Vector2 position, float radius) {
        TextureAtlas droneAtlas = ServiceLocator.getResourceService()
                .getAsset("images/drone.atlas", TextureAtlas.class);

        if (droneAtlas == null) {
            throw new RuntimeException("drone.atlas not loaded");
        }

        AnimationRenderComponent animator = new AnimationRenderComponent(droneAtlas);
        // All explosions use the same animation name from the atlas
        animator.addAnimation("bomb_effect", 0.05f, Animation.PlayMode.NORMAL);

        Entity explosion = new Entity()
                .addComponent(animator)
                .addComponent(new ExplosionAnimationController());

        float visualScale = radius * 0.5f;
        explosion.setScale(visualScale, visualScale);

        Vector2 bottomLeftPos = getExplosionPosition(position, visualScale);
        explosion.setPosition(bottomLeftPos);

        return explosion;
    }

    public static Vector2 getExplosionPosition(Vector2 position, float visualScale) {
        float halfSize = visualScale / 2f;
        return new Vector2(position.x - halfSize, position.y - halfSize);
    }
}