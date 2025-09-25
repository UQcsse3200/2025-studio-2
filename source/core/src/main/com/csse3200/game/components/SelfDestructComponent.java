package com.csse3200.game.components;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/**
 * Component for self-destruction drones.
 * Drone activates and teleports only when player enters its light cone.
 * Explodes immediately upon contact with the player.
 */
public class SelfDestructComponent extends Component {
    private final Entity target;
    private boolean exploded = false;
    private boolean isChasing = false;

    private static final String EXPLOSION_SOUND = "sounds/explosion.mp3";
    private static final float MAX_DISTANCE = 11f;
    private static final float TELEPORT_OFFSET = 1.5f;
    private static final float COLLISION_RADIUS = 1.3f; // radius for explosion contact

    public SelfDestructComponent(Entity target) {
        this.target = target;
    }

    private void teleportNearPlayer() {
        Vector2 position = target.getCenterPosition();
        float randomAngle = (float) (Math.random() * Math.PI * 2);
        Vector2 offset = new Vector2(
                (float) Math.cos(randomAngle) * TELEPORT_OFFSET,
                (float) Math.sin(randomAngle) * TELEPORT_OFFSET
        );
        Vector2 newPos = position.cpy().add(offset);
        entity.setPosition(newPos);
    }


    @Override
    public void update() {
        if (exploded || target == null) return;

        ConeLightComponent light = entity.getComponent(ConeLightComponent.class);

        // Activate chasing only if player enters light cone
        if (!isChasing && light != null && isPlayerInLight(target.getCenterPosition(), light)) {
            isChasing = true;

            // Remove/hide the cone light
            light.dispose();
            entity.removeComponent(light);
        }

        if (!isChasing) return;

        // Explode immediately if touching player
        if (isTouchingPlayer()) {
            explode();
            return;
        }
        // Teleport if far from player
        if (entity.getCenterPosition().dst(target.getCenterPosition())> MAX_DISTANCE) {
            teleportNearPlayer();
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

    /**
     * Checks if the player is inside the drone's light cone.
     */
    private boolean isPlayerInLight(Vector2 playerPos, ConeLightComponent light) { Vector2 toPlayer = playerPos.cpy().sub(entity.getCenterPosition());
        if (toPlayer.len() > light.getDistance()) return false;
        float lightDirRad = (float) Math.toRadians(light.getDirectionDeg());
        Vector2 lightDir = new Vector2((float) Math.cos(lightDirRad), (float) Math.sin(lightDirRad));
        return toPlayer.angleDeg(lightDir) <= light.getConeDegree() / 2f;
    }

    /**
     * Checks if the drone is in contact with the player.
     * Uses a circular collision radius to detect contact in all directions.
     */
    private boolean isTouchingPlayer() {
        Vector2 dronePos = entity.getCenterPosition();
        Vector2 playerPos = target.getCenterPosition();
        return dronePos.dst(playerPos) <= COLLISION_RADIUS;
    }
}
