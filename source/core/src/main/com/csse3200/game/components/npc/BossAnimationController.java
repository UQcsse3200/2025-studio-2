package com.csse3200.game.components.npc;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

public class BossAnimationController extends Component {
    AnimationRenderComponent animator;
    private String currentAnimation = "";
    // Generate the "minimum display time" of the animation（Fine-tune as needed 0.3~0.6）
    private float generateHold = 0f;
    // If the generation has just finished, but is still in the "display window", suspend and return to the chase
    private boolean pendingChase = false;

    @Override
    public void create() {
        super.create();
        animator = this.entity.getComponent(AnimationRenderComponent.class);
        entity.getEvents().addListener("generateDroneStart", this::animateGenerateDrone);
        entity.getEvents().addListener("droneSpawned", (Entity d) -> onDroneSpawned());
        entity.getEvents().addListener("chaseStart", this::animateChase);
        entity.getEvents().addListener("touchKillStart", this::animateTouchKill);
        entity.getEvents().addListener("shootLaserStart", this::animateShootLaser);

    }
    @Override
    public void update() {
        // In the "Generate Animation Display Window", count down the timer. When the timer is up,
        // the process will be suspended and the chase will be returned.
        if (generateHold > 0f) {
            generateHold -= ServiceLocator.getTimeSource().getDeltaTime();
            if (generateHold <= 0f && pendingChase) {
                setAnimation("bossChase");
                pendingChase = false;
            }
        }
    }private void onDroneSpawned() {
        Gdx.app.log("BossAnim", "droneSpawned");
        if (generateHold > 0f) {
            // Still in the animation display period: Don't switch yet,
            // wait until the display period is over before returning to the cruise
            pendingChase = true;
        } else {
            // back to chase
            setAnimation("bossChase");
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
        Gdx.app.log("BossAnim", "generateDroneStart");
        // Each time you receive "Start Generating", reset the display window
        generateHold = 0.8f; // If you want it to be more obvious, turn it up, e.g 0.6f
        pendingChase = false; // A new round of generation, clean up the previous round of suspension
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
        }
        Gdx.app.log("BossAnim", "setAnimation -> " + animationName);
    }
}
