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

//    @Test
//    void shouldMoveTowardsTarget() {
//        Entity target = new Entity();
//        target.setPosition(2f, 2f);
//
//        // Hover 3 unit above the target
//        float hoverHeight = 3f;
//
//        AITaskComponent ai = new AITaskComponent().addTask(new BombChaseTask(
//                target,
//                10,
//                5f,
//                10f,
//                hoverHeight,
//                1f,
//                2f
//        ));
//
//        Entity entity = makePhysicsEntity().addComponent(ai);
//        entity.create();
//        entity.setPosition(1f, 0f);
//
//        float initialDistance = entity.getPosition()
//                .dst(target.getPosition().cpy().add(0f, hoverHeight));
//
//        for (int i = 0; i < 3; i++) {
//            entity.earlyUpdate();
//            entity.update();
//            ServiceLocator.getPhysicsService().getPhysics().update();
//        }
//
//        float newDistance = entity.getPosition()
//                .dst(target.getPosition().cpy().add(0f, hoverHeight));
//
//        assertTrue(newDistance < initialDistance, "Entity should move closer to target");
//    }

    @Test
    void onlyChaseOnConditions() {
        Entity target = new Entity();
        target.setPosition(0f, 6f);
        Entity entity = makePhysicsEntity();
        entity.create();
        entity.setPosition(0f, 0f);

        BombChaseTask task = new BombChaseTask(
                target,
                /*priority*/ 10,
                /*viewDistance*/ 5f,
                /*maxChaseDistance*/ 10f,
                /*hoverHeight*/ 2f,
                /*dropRange*/ 1f,
                /*minHeight*/ 3f
        );
        task.create(() -> entity);

        // Not currently active, target is too far, negative priority
        assertTrue(task.getPriority() < 0);

        // Inside view distance, positive priority
        target.setPosition(0f, 4f);
        assertEquals(10, task.getPriority());

        // Active within chase distance, stays active
        target.setPosition(0f, 8f);
        task.start();
        assertEquals(10, task.getPriority());

        // Active and beyond max chase distance, stops chasing
        target.setPosition(0f, 12f);
        assertTrue(task.getPriority() < 0);

        // Drop zone condition: drone high enough above target and horizontally aligned
        entity.setPosition(0f, 10f);
        target.setPosition(0f, 6f);
        assertTrue(task.getPriority() < 0);
    }

    private Entity makePhysicsEntity() {
        return new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new PhysicsMovementComponent());
    }
}
