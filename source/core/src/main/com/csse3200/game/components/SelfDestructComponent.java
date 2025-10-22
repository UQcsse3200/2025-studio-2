package com.csse3200.game.components;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/**
 * Handles logic for a drone that self-destructs within collision radius of player.
 */
public class SelfDestructComponent extends Component {
    private final Entity target;// The player entity or object that drone targets
    private boolean exploded = false;// Tracks if drone has already exploded

    private static final String EXPLOSION_SOUND = "sounds/explosion.mp3";// sound to play on explosion
    private static final float MAX_DISTANCE = 30f;// max distance from target before forced explosion
    private static final float COLLISION_RADIUS = 1.3f; // radius for explosion contact

    /**
     * Constructor takes the entity to target (usually the player).
     */
    public SelfDestructComponent(Entity target) {
        this.target = target;
    }

    /**
     * Called when the component is created.
     * Sets up event listeners for handling cleanup after explosion.
     */
    @Override
    public void create() {
        // Handle drone clean up after explosion
        entity.getEvents().addListener("bomb_effectEnd", this::disable);
    }

    /**
     * Stops all drone physics, disables animations and marks entity as inactive.
     */
    private void disable() {
        // Make sure no more interactions after explosions
        PhysicsComponent phys = entity.getComponent(PhysicsComponent.class);
        if (phys != null && phys.getBody() != null) {
            var body = phys.getBody();
            body.setLinearVelocity(0f, 0f);// stop movement
            body.setAngularVelocity(0f); // stop rotation
            body.setActive(false);// deactivate physics body
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
        if (target == null) return; // no target? do nothing

        if (exploded) return; // already exploded? do nothing

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

    /**
     * Handles explosion logic: damage, event triggering, and sound playback.
     */
    private void explode() {
        if (exploded) return;// prevent multiple explosions
        exploded = true;

        // Reduce player's health by 2
        CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);
        if (targetStats != null) {
            targetStats.setHealth(Math.max(0, targetStats.getHealth() - 2));
        }

        // Trigger custom event so other components (like effects) can respond
        entity.getEvents().trigger("selfExplosion");

        // Play explosion sound (if resource service exists)
        var rs = ServiceLocator.getResourceService();
        if (rs != null) {
            Sound explosionSound = rs.getAsset(EXPLOSION_SOUND, Sound.class);
            if (explosionSound != null) {
                long soundId = explosionSound.play(1.0f);
                fadeOutSound(explosionSound, soundId);
            }
        }
    }

    /**
     * Gradually reduces sound volume over 0.5 seconds before stopping it.
     */
    private void fadeOutSound(Sound sound, long soundId) {
        final int steps = 10;
        final float interval = (float) 0.5 / steps; // 0.5 seconds divided by number of steps

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
    /**
     * Checks if drone is within collision radius of the player.
     *
     * @return true if drone touches player, false otherwise
     */
    private boolean isTouchingPlayer() {
        Vector2 dronePos = entity.getCenterPosition();
        Vector2 playerPos = target.getCenterPosition();
        return dronePos.dst(playerPos) <= COLLISION_RADIUS;
    }
}