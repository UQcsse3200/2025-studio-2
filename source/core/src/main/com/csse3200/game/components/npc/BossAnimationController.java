package com.csse3200.game.components.npc;

import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;

public class BossAnimationController extends Component {
    AnimationRenderComponent animator;
    private String currentAnimation = "";

    @Override
    public void create() {
        super.create();
        animator = this.entity.getComponent(AnimationRenderComponent.class);
        entity.getEvents().addListener("generateDroneStart", this::animateGenerateDrone);
        entity.getEvents().addListener("chaseStart", this::animateChase);
        entity.getEvents().addListener("touchKillStart", this::animateTouchKill);
        entity.getEvents().addListener("shootLaserStart", this::animateShootLaser);
    }


    void animateChase() {
        setAnimation("bossChase");
    }

    void animateGenerateDrone() {
        setAnimation("bossGenerateDrone");
    }

    void animateTouchKill() {
        setAnimation("bossTouchKill");
    }

    void animateShootLaser() {
        setAnimation("bossShootLaser");
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
