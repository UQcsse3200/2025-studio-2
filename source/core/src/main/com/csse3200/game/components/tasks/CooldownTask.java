package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.components.enemy.PatrolRouteComponent;

/**
 * A cooldown task that runs after a chase. The entity waits for a short duration,
 * then teleports back to the start of its patrol route.
 */
public class CooldownTask extends DefaultTask implements PriorityTask {
    private final float waitTime;
    private float timer;
    private PatrolRouteComponent route;

    public CooldownTask(float waitTime) {
        this.waitTime = waitTime;
    }

    @Override
    public void start() {
        super.start();
        timer = 0f;

        route = owner.getEntity().getComponent(PatrolRouteComponent.class);
        if (route == null) {
            throw new IllegalStateException("CooldownTask requires PatrolRouteComponent");
        }

        // Trigger event so animations/sfx can be implemented
        owner.getEntity().getEvents().trigger("cooldownStart");
    }

    @Override
    public void update() {
        float delta = com.badlogic.gdx.Gdx.graphics.getDeltaTime();
        timer += delta;
        if (timer >= waitTime) {
            Vector2 startPos = route.patrolStart();
            owner.getEntity().setPosition(startPos);

            owner.getEntity().getEvents().trigger("cooldownEnd");

            status = Status.FINISHED;

        }
    }

    @Override
    public int getPriority() {
        return 2; // Between Chase (higher) and Patrol (lower) ??
    }

    @Override
    public void stop() {
        super.stop();
        timer = 0f;
    }
}
