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

    private AnimationRenderComponent animator;
    private boolean cleanedUp = false;

    @Override
    public void create() {
        super.create();
        animator = entity.getComponent(AnimationRenderComponent.class);
        if(animator!=null){
            animator.startAnimation("explode");
        }
    }

    @Override
    public void update() {
        if (cleanedUp || animator == null) return;
        if (animator.isFinished()) {
            cleanUp();
        }
    }

    private void cleanUp() {
        cleanedUp = true;
        if(animator!=null) animator.stopAnimation();
        entity.dispose();
    }
}