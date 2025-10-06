package com.csse3200.game.components;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

public class SelfDestructComponent extends Component {
    private final Entity target;
    private boolean exploded = false;

    private static final String EXPLOSION_SOUND = "sounds/explosion.mp3";
    private static final float MAX_DISTANCE = 30f;
    private static final float COLLISION_RADIUS = 1.3f; // radius for explosion contact

    public SelfDestructComponent(Entity target) {
        this.target = target;
    }

    @Override
    public void update() {
        if (exploded || target == null) return;

        // Explode immediately if touching player
        if (isTouchingPlayer()) {
            explode();
            return;
        }

        // Explode if too far from player
        if (entity.getCenterPosition().dst(target.getCenterPosition()) > MAX_DISTANCE) {
            explode();
        }
    }

    private void explode() {
        if (exploded) return;
        exploded = true;

        CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
        if (targetStats != null) {
            targetStats.setHealth(Math.max(0, targetStats.getHealth() - 2));
        }

        AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);
        if (animator != null) {
            animator.startAnimation("bomb_effect");
        }

        Sound explosionSound = ServiceLocator.getResourceService().getAsset(EXPLOSION_SOUND, Sound.class);
        if (explosionSound != null) {
            long soundId = explosionSound.play(1.0f);
            fadeOutSound(explosionSound, soundId, 0.5f);
        }

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (animator != null) {
                    animator.stopAnimation();
                    entity.removeComponent(animator);
                }
                PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
                if (physics != null) {
                    if (physics.getBody() != null) physics.getBody().setActive(false);
                    entity.removeComponent(physics);
                }
                entity.getEvents().trigger("destroy");
                entity.removeComponent(SelfDestructComponent.this);
            }
        }, 0.5f);
    }

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

    private boolean isTouchingPlayer() {
        Vector2 dronePos = entity.getCenterPosition();
        Vector2 playerPos = target.getCenterPosition();
        return dronePos.dst(playerPos) <= COLLISION_RADIUS;
    }
}