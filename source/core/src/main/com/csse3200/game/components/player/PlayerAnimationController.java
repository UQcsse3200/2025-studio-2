package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;

public class PlayerAnimationController extends Component {
    AnimationRenderComponent animator;
    PlayerActions actions;
    private String currentAnimation = "";
    private Vector2 direction;

    @Override
    public void create() {
        super.create();
        animator = this.entity.getComponent(AnimationRenderComponent.class);
        actions = this.entity.getComponent(PlayerActions.class);
        entity.getEvents().addListener("jump", this::animateJump);
        entity.getEvents().addListener("walk", this::animateWalk);
        entity.getEvents().addListener("crouch", this::animateCrouching);
        entity.getEvents().addListener("landed", this::animateStop);
        entity.getEvents().addListener("walkStop", this::animateStop);
        entity.getEvents().addListener("dash", this::animateDash);
    }

    void animateStop() {
        animator.stopAnimation();
    }

    void animateJump() {
        setAnimation("JUMP");
    }

    void animateWalk(Vector2 direction) {
        if (direction.x > 0f) {
            setAnimation("RIGHT");
        } else if (direction.x < 0f) {
            setAnimation("LEFT");
        }
    }

    void animateCrouching() {
        if (actions.getIsCrouching()) {
            animator.startAnimation("CROUCHING");
        } else {
            animator.stopAnimation();
        }
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

    void animateDash() {
        // DO STUFF HERE
    }
}
