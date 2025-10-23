package com.csse3200.game.components.npc;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LaserAnimationController extends Component {
    private static final Logger logger = LoggerFactory.getLogger(LaserAnimationController.class);

    private AnimationRenderComponent animator;
    private boolean cleanedUp = false;

    @Override
    public void create() {
        super.create();
        animator = entity.getComponent(AnimationRenderComponent.class);
        if (animator != null) {
            animator.startAnimation("laserEffect");
            logger.debug("Started laser animation for entity {}", entity.getId());
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
        logger.debug("Cleaning up laser for entity {}", entity.getId());
        if (animator != null) {
            animator.stopAnimation();
        }
        // Defer disposal to the next frame to avoid nested iteration
        Gdx.app.postRunnable(() -> entity.dispose());
    }
}
