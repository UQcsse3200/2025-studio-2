package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.npc.DroneAnimationController;
import com.csse3200.game.components.obstacles.TrapComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Component that handles drone death when touching traps or death zones.
 * Plays an explosion animation and removes relevant components
 * instead of disposing the entity directly.
 */
public class DeathOnTrapComponent extends Component {
    private boolean triggered = false;
    private static final float ANIMATION_DURATION = 0.5f;
    private static final String EXPLOSION_SOUND = "sounds/explosion.mp3";

    @Override
    public void create() {
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        entity.getEvents().addListener("reset", this::onReset);

        AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);
        if (animator != null && !animator.hasAnimation("bomb_effect")) {
            animator.addAnimation("bomb_effect", 0.05f, Animation.PlayMode.NORMAL);
        }
    }
    /**
     * Handles collisions between the drone and other entities.
     * If the drone collides with a trap or death zone, the explosion sequence is triggered.
     *
     * @param me    The fixture of this entity.
     * @param other The fixture of the colliding entity.
     */

    private void onCollisionStart(Fixture me, Fixture other) {
        if (triggered || other == null || other.getBody() == null) return;

        Object userData = other.getBody().getUserData();
        if (!(userData instanceof BodyUserData bodyData)) return;

        Entity otherEntity = bodyData.entity;
        if (otherEntity == null) return;

        boolean hitTrap = otherEntity.getComponent(TrapComponent.class) != null;
        boolean hitDeathZone = otherEntity.getComponent(DeathZoneComponent.class) != null;
        DroneAnimationController droneAnim = entity.getComponent(DroneAnimationController.class);

        if ((hitTrap || hitDeathZone) && droneAnim != null) {
            explode();
        }
    }

    /**
     * Initiates the explosion sequence, disabling physics and collider components,
     * playing explosion effects, and cleaning up the entity safely after a delay.
     */

    private void explode() {
        if (triggered) return;
        triggered = true;

        // Disable collider
        ColliderComponent collider = entity.getComponent(ColliderComponent.class);
        if (collider != null) collider.setEnabled(false);

        // Play explosion animation
        AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);
        if (animator != null && animator.hasAnimation("bomb_effect")) {
            animator.startAnimation("bomb_effect");
        }

        // Play sound
        Sound explosionSound = ServiceLocator.getResourceService().getAsset(EXPLOSION_SOUND, Sound.class);
        if (explosionSound != null) {
            long soundId = explosionSound.play(UserSettings.get().masterVolume);
            fadeOutSound(explosionSound, soundId, 0.5f);
        }

        // Cleanup components after delay
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                try {
                    if (animator != null) {
                        animator.stopAnimation();
                        entity.removeComponent(animator);
                    }

                    PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
                    if (physics != null) {
                        if (physics.getBody() != null) physics.getBody().setActive(false);
                        entity.removeComponent(physics);
                    }

                    // 🔥 Unregister from entity service (so it's fully gone)
                    ServiceLocator.getEntityService().unregister(entity);

                    entity.getEvents().trigger("destroy");
                    entity.removeComponent(DeathOnTrapComponent.this);
                } catch (Exception e) {
                    Gdx.app.error("DeathOnTrapComponent", "Error during cleanup: " + e.getMessage());
                }
            }
        }, ANIMATION_DURATION);
    }

    /**
     * Gradually fades out the explosion sound over a specified duration.
     *
     * @param sound    The sound instance to fade.
     * @param soundId  The specific sound playback ID.
     * @param duration The total fade-out duration in seconds.
     */

        private void fadeOutSound(Sound sound, long soundId, float duration) {
        final int steps = 10;
        final float interval = duration / steps;

        for (int i = 0; i < steps; i++) {
            final float volume = 1.0f - (i / (float) steps);
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    sound.setVolume(soundId, volume);
                    if (volume <= 0f) sound.stop(soundId);
                }
            }, i * interval);
        }
    }

    private void onReset() {
        if (!triggered) return;
        triggered = false;

        ColliderComponent collider = entity.getComponent(ColliderComponent.class);
        if (collider != null) collider.setEnabled(true);
    }
}
