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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
class ChaseTaskTest {
  @BeforeEach
  void beforeEach() {
    // Mock rendering, physics, game time
    RenderService renderService = new RenderService();
    renderService.setDebug(mock(DebugRenderer.class));
    ServiceLocator.registerRenderService(renderService);

    GameTime gameTime = mock(GameTime.class);
    when(gameTime.getDeltaTime()).thenReturn(20f / 1000);
    ServiceLocator.registerTimeSource(gameTime);

    ServiceLocator.registerPhysicsService(new PhysicsService());
  }

  @Test
  void shouldMoveTowardsTarget() {
    Entity target = new Entity();
    target.setPosition(2f, 2f);

    AITaskComponent ai = new AITaskComponent().addTask(new ChaseTask(target, 10, 5, 10));
    Entity entity = makePhysicsEntity().addComponent(ai);
    entity.create();
    entity.setPosition(0f, 0f);

    float initialDistance = entity.getPosition().dst(target.getPosition());

    // Run the game for a few cycles
    for (int i = 0; i < 3; i++) {
      entity.earlyUpdate();
      entity.update();
      ServiceLocator.getPhysicsService().getPhysics().update();
    }

    float newDistance = entity.getPosition().dst(target.getPosition());
    assertTrue(newDistance < initialDistance, "Entity should move closer to target");
  }

  @Test
  void shouldChaseOnlyWhenInDistance() {
    Entity target = new Entity();
    target.setPosition(0f, 6f);

    Entity entity = makePhysicsEntity();
    entity.create();
    entity.setPosition(0f, 0f);

    ChaseTask chaseTask = new ChaseTask(target, 10, 5, 10);
    chaseTask.create(() -> entity);

    // Not active, target too far
    assertTrue(chaseTask.getPriority() < 0, "Should not chase before activation");

    // Move within viewDistance
    target.setPosition(0f, 4f);
    assertEquals(10, chaseTask.getPriority(), "Should chase within view distance");

    // Start task
    chaseTask.start();
    target.setPosition(0f, 8f);
    assertEquals(10, chaseTask.getPriority(), "Should chase within max chase distance");

    // Target moves outside maxChaseDistance
    target.setPosition(0f, 12f);
    assertTrue(chaseTask.getPriority() < 0, "Should stop chasing outside max chase distance");
  }

  @Test
  void shouldOnlyChaseAfterActivation() {
    Entity target = new Entity();
    target.setPosition(0f, 6f);

    Entity entity = makePhysicsEntity();
    entity.create();
    entity.setPosition(0f, 0f);

    ChaseTask chaseTask = new ChaseTask(target, 10, 5, 10);
    chaseTask.create(() -> entity);

    // Before activation
    assertTrue(chaseTask.getPriority() < 0, "Should not chase before activation");

    // Activate the task (simulate player triggering light)
    chaseTask.activate();
    assertEquals(10, chaseTask.getPriority(), "Should chase immediately after activation");

    // Target beyond maxChaseDistance
    target.setPosition(50f, 50f);
    chaseTask.start();
    assertTrue(chaseTask.getPriority() < 0, "Should stop chasing beyond max distance");

    // Back within maxChaseDistance
    target.setPosition(2f, 2f);
    assertEquals(10, chaseTask.getPriority(), "Should resume chasing within max distance");
  }

  @Test
  void shouldTriggerMaxChaseOnlyWhenClose() {
    Entity target = new Entity();
    target.setPosition(0f, 6f);

    Entity entity = makePhysicsEntity();
    entity.create();
    entity.setPosition(0f, 0f);

    ChaseTask chaseTask = new ChaseTask(target, 10, 5, 10);
    chaseTask.create(() -> entity);

    chaseTask.activate();
    chaseTask.start();

    // Initially far, priority still high
    assertEquals(10, chaseTask.getPriority(), "Should chase after activation even if far initially");

    // Move drone within trigger distance
    entity.setPosition(1f, 1f);
    assertEquals(10, chaseTask.getPriority(), "Should continue chasing when close");

    // Move drone outside maxChaseDistance
    entity.setPosition(20f, 20f);
    assertTrue(chaseTask.getPriority() < 0, "Should stop chasing after exceeding maxChaseDistance");
  }

  private Entity makePhysicsEntity() {
    return new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new PhysicsMovementComponent());
  }
}
