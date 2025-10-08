package com.csse3200.game.components;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/**
 * Handles logic for a drone that self-destructs within collision radius of player.
 */
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
    public void create() {
        // Handle drone clean up after explosion
        entity.getEvents().addListener("bomb_effectEnd", this::disable);
    }

    private void disable() {
        // Make sure no more interactions after explosions
        PhysicsComponent phys = entity.getComponent(PhysicsComponent.class);
        if (phys != null && phys.getBody() != null) {
            var body = phys.getBody();
            body.setLinearVelocity(0f, 0f);
            body.setAngularVelocity(0f);
            body.setActive(false);
        }

        // Stop explosion anim
        AnimationRenderComponent arc = entity.getComponent(AnimationRenderComponent.class);
        if (arc != null) {
            arc.stopAnimation();
        }
        entity.setEnabled(false);
    }

    @Override
    public void update() {
        if (target == null) return;

        if (exploded) return;

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

        entity.getEvents().trigger("selfExplosion");

        var rs = ServiceLocator.getResourceService();
        if (rs != null) {
            Sound explosionSound = rs.getAsset(EXPLOSION_SOUND, Sound.class);
            if (explosionSound != null) {
                long soundId = explosionSound.play(1.0f);
                fadeOutSound(explosionSound, soundId, 0.5f);
            }
        }
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