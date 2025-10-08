package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class TimerSignFactory {

    private static final int NUM_FRAMES = 25;

    public static Entity createTimer(float time, Vector2 position) {
        Entity sign = new Entity();

        TextureAtlas atlas = ServiceLocator.getResourceService().getAsset("images/timer.atlas", TextureAtlas.class);
        AnimationRenderComponent animator = new AnimationRenderComponent(atlas);
        if (atlas != null) {
            animator.addAnimation("timer", time / NUM_FRAMES, Animation.PlayMode.NORMAL);
            animator.addAnimation("timer-end", 1f,  Animation.PlayMode.LOOP);
        }
        // render behind everything
        animator.setLayer(0);

        sign.addComponent(animator);
        sign.setPosition(position.cpy().add(0f, 0.5f));

        animator.startAnimation("timer");

        return sign;
    }
}
