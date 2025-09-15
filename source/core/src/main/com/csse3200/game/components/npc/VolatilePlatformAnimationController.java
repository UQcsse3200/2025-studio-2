package com.csse3200.game.components.npc;

import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;

public class VolatilePlatformAnimationController extends Component {
    AnimationRenderComponent animator;
    private String currentAnimation = "";
    @Override
    public void create() {
        super.create();
        animator = this.entity.getComponent(AnimationRenderComponent.class);
        entity.getEvents().addListener("platformBreak", this::animateBreak);
        entity.getEvents().addListener("platformBlank", this::animateBlank);
    }
    void animateStop() {
        animator.stopAnimation();
    }

    void animateBreak() {
        setAnimation("break");
    }
    void animateBlank() {
        setAnimation("blank");
    }

    private void setAnimation(String animationName) {
        if (!animationName.equals(currentAnimation)) {
            animator.startAnimation(animationName);
            currentAnimation = animationName;
        }
    }
}
