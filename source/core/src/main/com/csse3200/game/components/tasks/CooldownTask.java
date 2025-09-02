package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.components.enemy.PatrolRouteComponent;
import com.csse3200.game.components.enemy.SpawnPositionComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;

/**
 * A cooldown task that runs after a chase. The entity waits for a short duration,
 * then teleports back to the start of its patrol route.
 */
public class CooldownTask extends DefaultTask implements PriorityTask {
    private final float waitTime;
    private float timer;
    private PatrolRouteComponent route;
    private boolean active = false;

    public CooldownTask(float waitTime) {

        this.waitTime = waitTime;
    }
    /** Called (via event) to make this task runnable */
    public void activate() {
        active = true;
        status = Status.INACTIVE;
    }


    @Override
    public void start() {
        super.start();
        timer = 0f;
        Entity entity = this.owner.getEntity();



        PhysicsComponent physics = owner.getEntity().getComponent(PhysicsComponent.class);
        if (physics != null) {
            physics.getBody().setGravityScale(0f);
            physics.getBody().setLinearVelocity(0f, 0f); // stop falling instantly
        }

        // Trigger event so animations/sfx can be implemented
        owner.getEntity().getEvents().trigger("cooldownStart");
    }

    @Override
    public void update() {
        float delta = com.badlogic.gdx.Gdx.graphics.getDeltaTime();
        timer += delta;
        if (timer >= waitTime) {
            Entity entity = owner.getEntity();
            Vector2 resetPos = null;

            PatrolRouteComponent patrol = entity.getComponent(PatrolRouteComponent.class);
            if (patrol != null) {
                resetPos = patrol.patrolStart();
            } else {
                SpawnPositionComponent spawn = entity.getComponent(SpawnPositionComponent.class);
                if (spawn != null) {
                    resetPos = spawn.getSpawnPos();
                }
            }

            if (resetPos != null) {
                entity.setPosition(resetPos);
            }
            // Re-enable gravity after teleport
            PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
            if (physics != null) {
                physics.getBody().setGravityScale(1f);
            }



            owner.getEntity().getEvents().trigger("cooldownEnd");

            status = Status.FINISHED;
            active = false;

        }
    }

    @Override
    public int getPriority() {
        return active ? 2 : -1;

    }

    @Override
    public void stop() {
        super.stop();
        timer = 0f;
    }
}
