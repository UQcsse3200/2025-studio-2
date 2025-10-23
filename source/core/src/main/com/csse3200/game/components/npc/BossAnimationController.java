package com.csse3200.game.components.npc;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class BossAnimationController extends Component {
    AnimationRenderComponent animator;
    private String currentAnimation = "";
    // Generate the "minimum display time" of the animation（Fine-tune as needed 0.3~0.6）
    private float generateHold = 0f;
    // If the generation has just finished, but is still in the "display window", suspend and return to the chase
    private boolean pendingChase = false;
    private static final String BOSS_ANIM_TAG = "BossAnim";

    @Override
    public void create() {
        super.create();
        animator = this.entity.getComponent(AnimationRenderComponent.class);
        entity.getEvents().addListener("generateDroneStart", this::animateGenerateDrone);
        entity.getEvents().addListener("droneSpawned", this::animateGenerateDrone);
        entity.getEvents().addListener("chaseStart", this::animateChase);
        entity.getEvents().addListener("touchKillStart", this::animateTouchKill);
        entity.getEvents().addListener("shootLaserStart", this::animateShootLaser);
    }

    @Override
    public void update() {
        if (animator == null) return;

        // Handle generateHold timer
        if (generateHold > 0f) {
            float dt = ServiceLocator.getTimeSource().getDeltaTime();
            generateHold -= dt;

            if (generateHold <= 0f && pendingChase) {
                animateChase();
            }
        }

        // Check if non-looping animations have finished and return to chase
        if (animator.isFinished() &&
                (currentAnimation.equals("bossShootLaser") ||
                        currentAnimation.equals("bossTouchKill"))) {
                Gdx.app.log(BOSS_ANIM_TAG, currentAnimation + " finished, returning to chase");
                animateChase();
        }
    }

    void animateChase() {
        setAnimation("bossChase");
        // Avoid external switching back to chase while still holding
        pendingChase = false;
        generateHold = 0f;
    }


    void animateGenerateDrone() {
        setAnimation("bossGenerateDrone");
        Gdx.app.log(BOSS_ANIM_TAG, "generateDroneStart");
        // Each time you receive "Start Generating", reset the display window
        generateHold = 1f; // If you want it to be more obvious, turn it up, e.g 0.6f
        pendingChase = true; // Set flag to return to chase after hold expires
    }

    void animateTouchKill() {
        setAnimation("bossTouchKill");
        pendingChase = false;
        generateHold = 0f;
    }

    void animateShootLaser() {
        setAnimation("bossShootLaser");
        pendingChase = false;
        generateHold = 0f;
    }

    /**
     * setAnimation: to avoid repeated startup of the same animations
     */
    private void setAnimation(String animationName) {
        if (!animationName.equals(currentAnimation)) {
            animator.startAnimation(animationName);
            currentAnimation = animationName;
            Gdx.app.log(BOSS_ANIM_TAG, "setAnimation -> " + animationName);
        }
    }
}