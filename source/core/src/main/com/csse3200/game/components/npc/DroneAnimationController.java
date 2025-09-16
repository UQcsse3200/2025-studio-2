package com.csse3200.game.components.npc;

import com.csse3200.game.components.Component;
//import com.csse3200.game.services.GameTime;
//import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.rendering.AnimationRenderComponent;

/**
 * This class listens to events relevant to a drone entity's state and plays the animation when one
 * of the events is triggered.
 */
public class DroneAnimationController extends Component {
    AnimationRenderComponent animator;
    private String currentAnimation = "";
//    private boolean isDropping = false;
//    private float dropAnimationTime = 0f;
//    private static final float DROP_ANIMATION_DURATION = 1.0f; // Duration of drop animation
//    private final GameTime timeSource = ServiceLocator.getTimeSource();

    @Override
    public void create() {
        super.create();
        animator = this.entity.getComponent(AnimationRenderComponent.class);
        entity.getEvents().addListener("wanderStart", this::animateWander);
        entity.getEvents().addListener("chaseStart", this::animateChase);
        entity.getEvents().addListener("patrolStart", this::animatePatrol);
        entity.getEvents().addListener("dropStart", this::animateDrop);
        entity.getEvents().addListener("teleportStart", this::animateTeleport);
    }

    void animateTeleport() {
        setAnimation("teleport");
    }

    void animateWander() {
        setAnimation("float");
    }

    void animateChase() {
        setAnimation("angry_float");
    }

    void animateDrop() {
        setAnimation("drop");
    }

    void animatePatrol() {
        setAnimation("float");
    }

    /**
     * setAnimation: to avoid repeated startup of the same animations
     */
    private void setAnimation(String animationName) {
        if (!animationName.equals(currentAnimation)) {
            animator.startAnimation(animationName);
            currentAnimation = animationName;
        }
    }
}