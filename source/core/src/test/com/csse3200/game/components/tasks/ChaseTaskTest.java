package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.listeners.EventListener0;
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
import static org.mockito.Mockito.*;

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
  void chase_priorityReflectsActivation() {
      ChaseTask chase = new ChaseTask(new Entity());
      assertEquals(-1, chase.getPriority(),
              "Chase has default -1 priority (inactive)");

      chase.activate();
      assertEquals(10, chase.getPriority(),
              "Activating chase makes priority 10");

      chase.deactivate();
      assertEquals(-1, chase.getPriority(),
              "Deactivating chase makes priority -1");
  }

  @Test
  void chase_start_activeFiresEvent() {
      Entity target = createEntityWithPosition();
      Entity e = makePhysicsEntity();
      AITaskComponent ai = new AITaskComponent();
      ChaseTask chase = new ChaseTask(target);
      ai.addTask(chase);
      e.addComponent(ai);
      e.create();

      EventListener0 callback = mock(EventListener0.class);
      e.getEvents().addListener("chaseStart", callback);

      chase.activate();
      chase.start();
      verify(callback, times(1)).handle();
  }

  @Test
  void chase_start_inactiveDoesNotFireEvent() {
      Entity target = createEntityWithPosition();
      Entity e = makePhysicsEntity();
      AITaskComponent ai = new AITaskComponent();
      ChaseTask chase = new ChaseTask(target);
      ai.addTask(chase);
      e.addComponent(ai);
      e.create();

      EventListener0 callback = mock(EventListener0.class);
      e.getEvents().addListener("chaseStart", callback);

      chase.start();
      verify(callback, times(0)).handle();
  }

  @Test
  void chase_stopFiresEvent() {
      Entity target = createEntityWithPosition();
      Entity e = makePhysicsEntity();
      AITaskComponent ai = new AITaskComponent();
      ChaseTask chase = new ChaseTask(target);
      ai.addTask(chase);
      e.addComponent(ai);
      e.create();

      EventListener0 callback = mock(EventListener0.class);
      e.getEvents().addListener("chaseEnd", callback);

      chase.activate();
      chase.start();
      chase.stop();
      verify(callback, times(1)).handle();
  }

  @Test
  void chase_shouldMoveTowardsTarget() {
      Entity target = createEntityWithPosition();
      Entity e = makePhysicsEntity();
      AITaskComponent ai = new AITaskComponent();
      ChaseTask chase = new ChaseTask(target);
      ai.addTask(chase);
      e.addComponent(ai);
      e.create();
      e.setPosition(0, 0);

      chase.activate();
      chase.start();

      float initialDistance = e.getPosition().dst(target.getPosition());

      for (int i = 0; i < 3; i++) {
        e.earlyUpdate();
        e.update();
        ServiceLocator.getPhysicsService().getPhysics().update();
      }

      float newDistance = e.getPosition().dst(target.getPosition());
      assertTrue(newDistance < initialDistance, "Entity should move closer when chasing");
  }


  private Entity createEntityWithPosition() {
      Entity e = new Entity();
      e.setPosition(new Vector2(1, 1));
      e.create();
      return e;
  }

  private Entity makePhysicsEntity() {
      return new Entity()
              .addComponent(new PhysicsComponent())
              .addComponent(new PhysicsMovementComponent());
  }

//  Commented out since current chase is based on player detection in light (no proximity)
//  @Test
//  void shouldStopChasingAfterExceedingMaxChaseDistance() {
//    Entity target = new Entity();
//    target.setPosition(0f, 0f); // start at origin
//
//    Entity entity = new Entity()
//            .addComponent(new PhysicsComponent())
//            .addComponent(new PhysicsMovementComponent());
//    entity.create();
//    entity.setPosition(0f, 0f);
//
//    ChaseTask chaseTask = new ChaseTask(target, 10, 5, 10);
//    chaseTask.create(() -> entity);
//
//    // Activate the task
//    chaseTask.activate();
//    chaseTask.start();
//
//    // Move entity within triggerDistance (2f) to enable maxChaseDistance enforcement
//    entity.setPosition(1f, 1f); // distance ~1.4, less than triggerDistance
//    assertEquals(10, chaseTask.getPriority(), "Should chase within triggerDistance");
//
//    // Move target far away now
//    target.setPosition(50f, 50f);
//    assertTrue(chaseTask.getPriority() < 0, "Should stop chasing after exceeding maxChaseDistance");
//  }

}
