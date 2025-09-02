package com.csse3200.game.components.npc;

import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Controls the explosion animation and disposes the entity after the animation is finished.
 */
public class ExplosionAnimationController extends Component {
    private AnimationRenderComponent animator;

    @Override
    public void create() {
        super.create();
        animator = this.entity.getComponent(AnimationRenderComponent.class);
        entity.getEvents().addListener("animationFinished", this::onAnimationFinished);

        animator.startAnimation("bomb_effect");
    }

    /**
     * Called when the animation playback is completed
     * @param animationName the name of the animation completed
     */
    private void onAnimationFinished(String animationName) {
        if ("bomb_effect".equals(animationName)) {
            entity.dispose();
        }
    }
}