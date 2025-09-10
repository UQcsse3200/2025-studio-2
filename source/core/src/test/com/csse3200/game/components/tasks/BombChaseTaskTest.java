package com.csse3200.game.components.tasks;

import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
public class BombChaseTaskTest {

    @BeforeEach
    void beforeEach() {
        RenderService renderService = new RenderService();
        renderService.setDebug(mock(DebugRenderer.class));
        ServiceLocator.registerRenderService(renderService);

        GameTime gameTime = mock(GameTime.class);
        when(gameTime.getDeltaTime()).thenReturn(20f / 1000);
        ServiceLocator.registerTimeSource(gameTime);

        ServiceLocator.registerPhysicsService(new PhysicsService());
    }

    @Test
    void shouldMoveTowardsTargetWhenActivated() {
        Entity target = new Entity();
        target.setPosition(2f, 2f);
        float hoverHeight = 3f;

        BombChaseTask task = new BombChaseTask(
                target, 10, 5f, 10f, hoverHeight, 1f, 2f
        );

        Entity entity = makePhysicsEntity();
        entity.addComponent(new AITaskComponent().addTask(task));
        entity.create();
        entity.setPosition(0f, 0f);

        // Activate chase via light trigger
        task.create(() -> entity);
        task.activate();
        task.start();

        float initialDistance = entity.getPosition().dst(target.getPosition().cpy().add(0f, hoverHeight));

        for (int i = 0; i < 3; i++) {
            entity.earlyUpdate();
            entity.update();
            ServiceLocator.getPhysicsService().getPhysics().update();
        }

        float newDistance = entity.getPosition().dst(target.getPosition().cpy().add(0f, hoverHeight));
        assertTrue(newDistance < initialDistance, "Entity should move closer to target after activation");
    }

    @Test
    void shouldNotChaseBeforeActivation() {
        Entity target = new Entity();
        target.setPosition(0f, 4f);

        Entity entity = makePhysicsEntity();
        entity.create();
        entity.setPosition(0f, 0f);

        BombChaseTask task = new BombChaseTask(
                target, 10, 5f, 10f, 2f, 1f, 3f
        );
        task.create(() -> entity);

        // Not activated, priority must be negative
        assertTrue(task.getPriority() < 0, "Task should not chase before activation");
    }

    @Test
    void shouldStopChasingAfterExceedingMaxDistance() {
        Entity target = new Entity();
        target.setPosition(0f, 0f);

        Entity entity = makePhysicsEntity();
        entity.create();
        entity.setPosition(0f, 0f);

        BombChaseTask task = new BombChaseTask(
                target, 10, 5f, 5f, 2f, 1f, 3f
        );
        task.create(() -> entity);
        task.activate();
        task.start();

        // Move target within view distance, chase is active
        target.setPosition(0f, 3f);
        assertEquals(10, task.getPriority(), "Task should chase within view distance");

        // Move target beyond max chase distance
        target.setPosition(0f, 10f);
        assertTrue(task.getPriority() < 0, "Task should stop chasing beyond max chase distance");
    }

    @Test
    void shouldStopChasingInDropZone() {
        Entity target = new Entity();
        target.setPosition(0f, 6f);

        Entity entity = makePhysicsEntity();
        entity.create();
        entity.setPosition(0f, 10f); // Drone above target

        BombChaseTask task = new BombChaseTask(
                target, 10, 5f, 10f, 2f, 1.5f, 3f
        );
        task.create(() -> entity);
        task.activate();
        task.start();

        // Horizontally aligned and above target: drop zone triggers
        assertTrue(task.getPriority() < 0, "Task should stop chasing when target in bomb drop zone");
    }

    private Entity makePhysicsEntity() {
        return new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new PhysicsMovementComponent());
    }
}
