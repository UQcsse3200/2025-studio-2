package com.csse3200.game.components.player;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.badlogic.gdx.utils.Timer;


public class PlayerAnimationController extends Component {
    AnimationRenderComponent animator;
    PlayerActions actions;
    private GameTime timer = new GameTime();

    // ChatGPT Basic model helped with testing the timer 17/09/25
    public java.util.function.BiConsumer<Runnable, Float> scheduleTask = (runnable, delay) -> Timer.schedule(new Timer.Task() {
        @Override
        public void run() {
            runnable.run();
        }
    }, delay);

    public String currentAnimation = "";

    private int xDirection = 1;
    private long hurtTime = -1000;
    private float hurtDelay = 0.5f;
    private float dashDelay = 0.3f;
    private float jumpDelay = 0.8f;

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
    }

    public void setAnimator(AnimationRenderComponent animator) {
        this.animator = animator;
    }

    public void setTimer(GameTime timer) {
        this.timer = timer;
    }

    /**
     * stop the player's current animation and return to idle
     */
    public void animateStop() {
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

    /**
     * starts the player's jump animation
     */
    public void animateJump() {
        if (xDirection == 1) {
            setAnimation("JUMP");
        } else if (xDirection == -1) {
            setAnimation("JUMPLEFT");
        }

        // After delay stop the dash animation - ChatGPT basic helped with this code 17/09/25
        scheduleTask.accept(() -> setAnimation("IDLE"), jumpDelay);
    }

    /**
     * starts the player's walk animation
     */
    public void animateWalk(Vector2 direction) {
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

    /**
     * starts the player's crouching animation
     */
    public void animateCrouching() {
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
    public void setAnimation(String animationName) {
        if ((!animationName.equals(currentAnimation) || animationName.equals("JUMP"))
                // Don't cancel hurt animation
                && timer.getTimeSince(hurtTime) > hurtDelay * 900) {
            animator.startAnimation(animationName);
            currentAnimation = animationName;
        }
    }

    /**
     * starts the player's dash animation
     */
    public void animateDash() {
        if (xDirection == 1) {
            setAnimation("DASH");
        } else {
            setAnimation("DASHLEFT");
        }

        // After delay stop the dash animation - ChatGPT basic helped with this code 17/09/25
        scheduleTask.accept(() -> setAnimation("IDLE"), dashDelay);
    }

    /**
     * starts the player's hurt animation
     */
    public void animateHurt() {
        if (xDirection == 1) {
            setAnimation("HURT");
        } else {
            setAnimation("HURTLEFT");
        }
        hurtTime = timer.getTime();

        // After delay stop the hurt animation - ChatGPT basic helped with this code 17/09/25
        scheduleTask.accept(() -> setAnimation("IDLE"), hurtDelay);
    }
}
