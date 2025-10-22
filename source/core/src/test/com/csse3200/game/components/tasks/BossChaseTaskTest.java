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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class BossChaseTaskTest {
    @BeforeEach
    void beforeEach() {
        ServiceLocator.clear();

        // Render service
        RenderService renderService = new RenderService();
        renderService.setDebug(mock(DebugRenderer.class));
        ServiceLocator.registerRenderService(renderService);

        // Physics
        ServiceLocator.registerPhysicsService(new PhysicsService());

        // GameTime
        GameTime gameTime = mock(GameTime.class);
        ServiceLocator.registerTimeSource(gameTime);
    }

    @Test
    void bossChase_priorityReflectsActivation() {
        Entity player = createTarget(new Vector2(0,0));
        Entity boss = createBoss(new Vector2(10,10));
        Vector2 stopPoint = new Vector2(5, 10);

        BossChaseTask bossChase = new BossChaseTask(player, stopPoint);
        boss.getComponent(AITaskComponent.class).addTask(bossChase);

        // Assert active and inactive priorities are correct
        bossChase.deactivate();
        assertEquals(-1, bossChase.getPriority(), "Inactive -> priority -1");

        bossChase.activate();
        assertEquals(10, bossChase.getPriority(), "Active -> priority 10");

        bossChase.deactivate();
        assertEquals(-1, bossChase.getPriority(), "Deactivated -> priority -1");
    }

    @Test
    void bossChase_start_activeFiresEvent() {
        Entity player = createTarget(new Vector2(0, 0));
        Entity boss = createBoss(new Vector2(10, 10));
        Vector2 stopPoint = new Vector2(50, 10);

        BossChaseTask bossChase = new BossChaseTask(player, stopPoint);
        boss.getComponent(AITaskComponent.class).addTask(bossChase);

        EventListener0 onStart = mock(EventListener0.class);
        boss.getEvents().addListener("bossChaseStart", onStart);

        bossChase.activate();
        bossChase.start();
        verify(onStart, times(1)).handle();
    }

    @Test
    void bossChase_start_inactiveDoesNotFireEvent() {
        Entity player = createTarget(new Vector2(0, 0));
        Entity boss = createBoss(new Vector2(10, 10));
        Vector2 stopPoint = new Vector2(50, 10);

        BossChaseTask bossChase = new BossChaseTask(player, stopPoint);
        boss.getComponent(AITaskComponent.class).addTask(bossChase);

        EventListener0 onStart = mock(EventListener0.class);
        boss.getEvents().addListener("bossChaseStart", onStart);

        bossChase.deactivate();
        bossChase.start();
        verify(onStart, times(0)).handle();
    }

    @Test
    void bossChase_stopFiresEvent() {
        Entity player = createTarget(new Vector2(0, 0));
        Entity boss = createBoss(new Vector2(10, 10));
        Vector2 stopPoint = new Vector2(50, 10);

        BossChaseTask bossChase = new BossChaseTask(player, stopPoint);
        boss.getComponent(AITaskComponent.class).addTask(bossChase);

        EventListener0 onEnd = mock(EventListener0.class);
        boss.getEvents().addListener("bossChaseEnd", onEnd);

        bossChase.activate();
        bossChase.start();
        bossChase.stop();
        verify(onEnd, times(1)).handle();
    }

    @Test
    void bossChase_targetsPlayer() {
        Entity player = createTarget(new Vector2(0,0));
        Entity boss = createBoss(new Vector2(10,0));
        Vector2 stopPoint = new Vector2(50,0);

        BossChaseTask bossChase = new BossChaseTask(player, stopPoint);
        boss.getComponent(AITaskComponent.class).addTask(bossChase);

        bossChase.activate();
        boss.earlyUpdate();
        boss.update();

        Vector2 target = bossChase.getCurrentTarget();
        assertEquals(player.getPosition().x, target.x);
        assertEquals(player.getPosition().y, target.y);
    }

    @Test
    void bossChase_targetsStopPoint() {
        Entity player = createTarget(new Vector2(0, 0));
        Entity boss = createBoss(new Vector2(10, 0));
        Vector2 stopPoint = new Vector2(5, 0);

        BossChaseTask bossChase = new BossChaseTask(player, stopPoint);
        boss.getComponent(AITaskComponent.class).addTask(bossChase);

        bossChase.activate();
        boss.update();

        // Cross the threshold
        player.setPosition(6f, 0f);
        boss.update();

        Vector2 target = bossChase.getCurrentTarget();
        assertEquals(stopPoint.x, target.x);
        assertEquals(stopPoint.y, target.y);
    }

    @Test
    void bossChase_finishesAtStopPoint() {
        // Player already past stopX at start so boss moves to stopPoint
        Entity player = createTarget(new Vector2(100, 0));
        Entity boss = createBoss(new Vector2(0, 0));
        Vector2 stopPoint = new Vector2(10, 0);

        BossChaseTask bossChase = new BossChaseTask(player, stopPoint);
        boss.getComponent(AITaskComponent.class).addTask(bossChase);

        bossChase.activate();
        bossChase.start();

        boss.setPosition(10, 0);
        boss.update();

        assertTrue(boss.getCenterPosition().dst2(stopPoint) <= 0.5f,
                "Boss should arrive at stop point");

        assertEquals(-1, bossChase.getPriority(), "Task should deactivate once stop point reached");
    }

    @Test
    void bossChase_playerStartsAtStopPoint() {
        // Edge case: boss already at stopPoint and player past stopX
        Entity player = createTarget(new Vector2(100, 0));
        Entity boss = createBoss(new Vector2(10, 0));
        Vector2 stopPoint = new Vector2(10, 0);

        BossChaseTask bossChase = new BossChaseTask(player, stopPoint);
        boss.getComponent(AITaskComponent.class).addTask(bossChase);

        bossChase.activate();
        bossChase.start();

        boss.update();

        assertEquals(-1, bossChase.getPriority(), "Already at stop point -> should deactivate");
    }


    /** Helper function creates entity with position */
    private Entity createTarget(Vector2 pos) {
        Entity e = new Entity();
        e.setPosition(pos);
        e.create();
        return e;
    }

    /** Helper function creates mock boss entity */
    private Entity createBoss(Vector2 pos) {
        Entity e = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new PhysicsMovementComponent())
                .addComponent(new AITaskComponent());
        e.setPosition(pos);
        e.create();
        return e;
    }
}