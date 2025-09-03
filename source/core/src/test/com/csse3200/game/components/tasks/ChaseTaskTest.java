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
    // Mock rendering, physics, and game time
    RenderService renderService = new RenderService();
    renderService.setDebug(mock(DebugRenderer.class));
    ServiceLocator.registerRenderService(renderService);

    GameTime gameTime = mock(GameTime.class);
    when(gameTime.getDeltaTime()).thenReturn(20f / 1000);
    ServiceLocator.registerTimeSource(gameTime);

    ServiceLocator.registerPhysicsService(new PhysicsService());
  }

  @Test
  void shouldMoveTowardsTargetAfterActivation() {
    Entity target = new Entity();
    target.setPosition(2f, 2f);

    ChaseTask chaseTask = new ChaseTask(target, 10, 5, 10);
    AITaskComponent ai = new AITaskComponent().addTask(chaseTask);

    Entity entity = new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new PhysicsMovementComponent())
            .addComponent(ai);
    entity.create();
    entity.setPosition(0f, 0f);

    chaseTask.activate();
    chaseTask.start();

    float initialDistance = entity.getPosition().dst(target.getPosition());

    for (int i = 0; i < 3; i++) {
      entity.earlyUpdate();
      entity.update();
      ServiceLocator.getPhysicsService().getPhysics().update();
    }

    float newDistance = entity.getPosition().dst(target.getPosition());
    assertTrue(newDistance < initialDistance, "Entity should move closer after activation");
  }

  @Test
  void shouldNotChaseBeforeActivation() {
    Entity target = new Entity();
    target.setPosition(0f, 4f);

    Entity entity = new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new PhysicsMovementComponent());
    entity.create();
    entity.setPosition(0f, 0f);

    ChaseTask chaseTask = new ChaseTask(target, 10, 5, 10);
    chaseTask.create(() -> entity);

    assertTrue(chaseTask.getPriority() < 0, "Should not chase before activation");
  }

  @Test
  void shouldChaseImmediatelyAfterActivation() {
    Entity target = new Entity();
    target.setPosition(0f, 6f);

    Entity entity = new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new PhysicsMovementComponent());
    entity.create();
    entity.setPosition(0f, 0f);

    ChaseTask chaseTask = new ChaseTask(target, 10, 5, 10);
    chaseTask.create(() -> entity);

    chaseTask.activate();
    assertEquals(10, chaseTask.getPriority(), "Should chase immediately after activation");
  }

  @Test
  void shouldStopChasingAfterExceedingMaxChaseDistance() {
    Entity target = new Entity();
    target.setPosition(0f, 0f); // start at origin

    Entity entity = new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new PhysicsMovementComponent());
    entity.create();
    entity.setPosition(0f, 0f);

    ChaseTask chaseTask = new ChaseTask(target, 10, 5, 10);
    chaseTask.create(() -> entity);

    // Activate the task
    chaseTask.activate();
    chaseTask.start();

    // Move entity within triggerDistance (2f) to enable maxChaseDistance enforcement
    entity.setPosition(1f, 1f); // distance ~1.4, less than triggerDistance
    assertEquals(10, chaseTask.getPriority(), "Should chase within triggerDistance");

    // Move target far away now
    target.setPosition(50f, 50f);
    assertTrue(chaseTask.getPriority() < 0, "Should stop chasing after exceeding maxChaseDistance");
  }

}
