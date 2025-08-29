package com.csse3200.game.components.npc;

import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;

/**
 * This class listens to events relevant to a drone entity's state and plays the animation when one
 * of the events is triggered.
 */
public class DroneAnimationController extends Component {
    AnimationRenderComponent animator;

    @Override
    public void create() {
        super.create();
        animator = this.entity.getComponent(AnimationRenderComponent.class);
        entity.getEvents().addListener("wanderStart", this::animateWander);
        entity.getEvents().addListener("chaseStart", this::animateChase);
        entity.getEvents().addListener("dropStart", this::animateDrop);
    }

    void animateWander() {
        animator.startAnimation("float");
    }

    void animateChase() {
        animator.startAnimation("angry_float");
    }

    void animateDrop() {
        // Play attack animation (you'll need to add this animation to the atlas)
        animator.startAnimation("drop");
    }
}