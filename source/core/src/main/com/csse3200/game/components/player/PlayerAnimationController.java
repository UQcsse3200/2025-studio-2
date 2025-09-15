package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;

public class PlayerAnimationController extends Component {
    AnimationRenderComponent animator;
    PlayerActions actions;
    private String currentAnimation = "";
    private int xDirection = 1;
    private int oldPlayerHealth;

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
        entity.getEvents().addListener("hurt", this::animateHurt);

        oldPlayerHealth = entity.getComponent(CombatStatsComponent.class).getHealth();
    }

    void animateStop() {
        if (xDirection == 1) {
            if (actions.getIsCrouching()) {
                setAnimation("CROUCHMOVE");
            } else {
                animator.startAnimation("IDLE");
            }
        } else if (xDirection == -1) {
            if (actions.getIsCrouching()) {
                setAnimation("CROUCHMOVELEFT");
            } else {
                animator.startAnimation("IDLELEFT");
            }
        }
    }

    void animateJump() {
        if (xDirection == 1) {
            setAnimation("JUMP");
        } else if (xDirection == -1) {
            setAnimation("JUMPLEFT");
        }
    }

    void animateWalk(Vector2 direction) {
        if (direction.x > 0f) {
            if (actions.getIsCrouching()) {
                setAnimation("CROUCHMOVE");
            } else {
                setAnimation("RIGHT");
            }
            xDirection = 1;
        } else if (direction.x < 0f) {
            if (actions.getIsCrouching()) {
                setAnimation("CROUCHMOVELEFT");
            } else {
                setAnimation("LEFT");
            }
            xDirection = -1;
        }
    }

    void animateCrouching() {
        if (!actions.getIsCrouching()) {
            if (xDirection == 1) {
                setAnimation("CROUCH");
            } else if (xDirection == -1) {
                setAnimation("CROUCHLEFT");
            }
        } else {
            if (xDirection == 1) {
                setAnimation("IDLE");
            } else if (xDirection == -1) {
                setAnimation("IDLELEFT");
            }
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
        if (xDirection == 1) {
            setAnimation("DASH");
        } else {
            // Make DASHLEFT when bruce adds it to the atlas
            setAnimation("DASH");
//            animator.startAnimation("DASHLEFT");
        }

//        System.out.println("IDLING");
//        animator.startAnimation("IDLE");
    }

    void animateHurt() {
        if (xDirection == 1) {
            setAnimation("HURT");
        } else {
            // Make HURTLEFT when bruce adds it to the atlas
            setAnimation("HURT");
//            animator.startAnimation("HURTLEFT");
        }
    }
}
