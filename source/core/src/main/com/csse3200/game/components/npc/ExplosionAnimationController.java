package com.csse3200.game.components.npc;

import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls the explosion animation and disposes the entity after the animation is finished.
 */
public class ExplosionAnimationController extends Component {
    private static final Logger logger = LoggerFactory.getLogger(ExplosionAnimationController.class);
    private static final String ANIM = "bomb_effect";

    private AnimationRenderComponent animator;
    private boolean cleanedUp = false;

    @Override
    public void create() {
        super.create();
        animator = this.entity.getComponent(AnimationRenderComponent.class);
        animator.startAnimation(ANIM);
    }

    @Override
    public void update() {
        if (cleanedUp || animator == null) return;
        if (animator.isFinished()) {
            logger.info("Explosion animation finished. Cleaned up");
            cleanUp();
        }
    }

    private void cleanUp() {
        cleanedUp = true;
        animator.stopAnimation();
    }
}