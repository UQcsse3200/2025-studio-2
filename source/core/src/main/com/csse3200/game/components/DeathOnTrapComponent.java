package com.csse3200.game.components;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.obstacles.TrapComponent;
import com.csse3200.game.components.DeathZoneComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.npc.DroneAnimationController;

/**
 * Component that destroys drones when they touch traps or death zones.
 * - Plays a "bomb_effect" animation when triggered.
 * - Removes the drone from the EntityService after a short delay.
 * - disposes the drone entity after removal.
 *
 */
public class DeathOnTrapComponent extends Component {

    private boolean triggered = false;
    private boolean disposed = false;
    private static final float ANIMATION_DURATION = 0.5f;

    @Override
    public void create() {

        // Listen for collisions
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);

        // Listen for level reset
        entity.getEvents().addListener("reset", this::onReset);

        // Add explosion animation
        AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

        if (animator != null) {
            if (!animator.hasAnimation("bomb_effect")) {
                animator.addAnimation("bomb_effect", 0.05f, Animation.PlayMode.NORMAL);
            }
        }
    }

    private void onCollisionStart(Fixture me, Fixture other) {

        if (triggered) {
            return;
        }

        if (disposed) {
            return;
        }

        if (other == null) {
            return;
        }

        if (other.getBody() == null) {
            return;
        }

        Object userData = other.getBody().getUserData();

        if (!(userData instanceof BodyUserData)) {
            return;
        }

        BodyUserData bodyData = (BodyUserData) userData;
        Entity otherEntity = bodyData.entity;

        if (otherEntity == null) {
            return;
        }

        TrapComponent trapComp = otherEntity.getComponent(TrapComponent.class);
        DeathZoneComponent deathZoneComp = otherEntity.getComponent(DeathZoneComponent.class);
        DroneAnimationController droneAnim = entity.getComponent(DroneAnimationController.class);

        if (trapComp != null) {
            if (droneAnim != null) {
                triggerDeath();
            }
        } else {
            if (deathZoneComp != null) {
                if (droneAnim != null) {
                    triggerDeath();
                }
            }
        }
    }

    private void triggerDeath() {

        if (triggered) {
            return;
        }

        if (disposed) {
            return;
        }

        triggered = true;

        // Disable collider
        ColliderComponent collider = entity.getComponent(ColliderComponent.class);

        if (collider != null) {
            collider.setEnabled(false);
        }

        // Play explosion animation
        AnimationRenderComponent animator = entity.getComponent(AnimationRenderComponent.class);

        if (animator != null) {
            if (animator.hasAnimation("bomb_effect")) {
                animator.startAnimation("bomb_effect");
            }
        }

        // Schedule removal after animation
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {

                if (disposed) {
                    return;
                }

                if (entity == null) {
                    return;
                }

                // Remove from entity service
                ServiceLocator.getEntityService().unregister(entity);

                // Destroy physics body if it exists
                PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);

                if (physics != null) {
                    if (physics.getBody() != null) {
                        if (!physics.getBody().getWorld().isLocked()) {
                            physics.getBody().getWorld().destroyBody(physics.getBody());
                        }
                    }
                }

                disposed = true;

                // Dispose entity
                entity.dispose();
            }
        }, ANIMATION_DURATION);
    }

    private void onReset() {

        if (disposed) {
            return;
        }

        if (entity == null) {
            return;
        }

        if (triggered) {
            return;
        }

        triggered = false;

        ColliderComponent collider = entity.getComponent(ColliderComponent.class);

        if (collider != null) {
            collider.setEnabled(true);
        }
    }
}
