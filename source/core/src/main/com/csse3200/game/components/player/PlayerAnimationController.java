package com.csse3200.game.components.player;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.services.ServiceLocator;


public class PlayerAnimationController extends Component {
    AnimationRenderComponent animator;
    PlayerActions actions;
    private GameTime timer = new GameTime();
    private PlayerActions playerActions;

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
    private float hurtDelay = 0.3f;
    private float dashDelay = 0.3f;
    private float jumpDelay = 0.8f;

    public PlayerAnimationController(PlayerActions playerActions) {
        super();
        this.playerActions = playerActions;
    }

    @Override
    public void create() {
        super.create();
        animator = this.entity.getComponent(AnimationRenderComponent.class);
        actions = this.entity.getComponent(PlayerActions.class);
        entity.getEvents().addListener("jump", this::animateJump);
        entity.getEvents().addListener("walk", this::animateWalk);
        entity.getEvents().addListener("crouch", this::animateCrouching);
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

            // After delay stop the dash animation - ChatGPT basic helped with this code 17/09/25
            scheduleTask.accept(this::revertAnimation, jumpDelay);
        } else if (xDirection == -1) {
            setAnimation("JUMPLEFT");

            // After delay stop the dash animation - ChatGPT basic helped with this code 17/09/25
            scheduleTask.accept(this::revertAnimation, jumpDelay);
        }

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
        System.out.println(1);
        if (actions.getIsCrouching()) {
            System.out.println(2);
            if (xDirection == 1) {
                setAnimation("CROUCH");
            } else if (xDirection == -1) {
                setAnimation("CROUCHLEFT");
            }
        } else {
            System.out.println(3);
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
    public void revertAnimation() {
        String animationName;
        boolean stationary = playerActions.getWalkDirection().equals(Vector2.Zero.cpy());

        if (xDirection == 1) { // Facing Right
            if (actions.getIsCrouching()) {
                animationName = "CROUCH";
                System.out.println(1);
            } else if (stationary) {
                animationName = "IDLE";
                System.out.println(2);
            } else {
                animationName = "RIGHT";
                System.out.println(3);
            }
        } else { // Facing Left
            if (actions.getIsCrouching()) {
                animationName = "CROUCHLEFT";
                System.out.println(4);
            } else if (stationary) {
                animationName = "IDLELEFT";
                System.out.println(5);
            } else {
                animationName = "LEFT";
                System.out.println(6);
            }
        }

        // Don't cancel hurt animation
        if (timer.getTimeSince(hurtTime) > hurtDelay * 900) {
            animator.startAnimation(animationName);
            currentAnimation = animationName;
        }
    }

    /**
     * setAnimation: to avoid repeated startup of the same animations
     */
    public void setAnimation(String animationName) {
        // Don't cancel hurt animation
        if (timer.getTimeSince(hurtTime) > hurtDelay * 900) {
            System.out.println(animationName);
            animator.startAnimation(animationName);
            currentAnimation = animationName;
        }
    }

    /**
     * starts the player's dash animation
     */
    public void animateDash() {
        if (!playerActions.getIsCrouching()) {
            if (xDirection == 1) {
                setAnimation("DASH");
                // After delay stop the dash animation - ChatGPT basic helped with this code 17/09/25
                scheduleTask.accept(this::revertAnimation, dashDelay);
            } else {
                setAnimation("DASHLEFT");
                // After delay stop the hurt animation - ChatGPT basic helped with this code 17/09/25
                scheduleTask.accept(this::revertAnimation, hurtDelay);
            }
        }
    }

    /**
     * starts the player's hurt animation
     */
    public void animateHurt() {
        Sound damageSound = ServiceLocator.getResourceService().getAsset(
                "sounds/damagesound.mp3", Sound.class);
        damageSound.play(UserSettings.get().masterVolume);

        if (xDirection == 1) {
            setAnimation("HURT");
            // After delay stop the hurt animation - ChatGPT basic helped with this code 17/09/25
            scheduleTask.accept(this::revertAnimation, hurtDelay);
        } else {
            setAnimation("HURTLEFT");
            // After delay stop the hurt animation - ChatGPT basic helped with this code 17/09/25
            scheduleTask.accept(this::revertAnimation, hurtDelay);
        }
        hurtTime = timer.getTime();

    }

    public void setXDirection(int i) {
        xDirection = i;
    }
}
