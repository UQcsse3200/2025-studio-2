package com.csse3200.game.components.enemy;

import com.csse3200.game.components.projectiles.BombComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(GameExtension.class)
public class BombTrackerComponentTest {
    @BeforeEach
    void setup() {
        ServiceLocator.clear();
        ServiceLocator.registerTimeSource(new GameTime());
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerPhysicsService(new PhysicsService());
    }

    private Entity makeBombEntity(float explosionDelay, float explosionRadius, short targetLayer) {
        Entity e = new Entity();
        e.addComponent(new BombComponent(explosionDelay, explosionRadius, targetLayer));
        e.create();
        return e;
    }

    @Test
    public void trackOnDispose() {
        BombTrackerComponent tracker = new BombTrackerComponent();
        new Entity().addComponent(tracker); // owner
        Entity bomb = makeBombEntity(2.0f, 2.5f, (short) 0);

        tracker.trackBomb(bomb);
        assertEquals(1, tracker.getNumTracked(), "Bomb should be tracked after trackBomb()");

        // Direct disposal should emit "bomb:disposed" and remove from tracker
        bomb.dispose();
        assertEquals(0, tracker.getNumTracked(), "Tracker should untrack on bomb:disposed");
    }

    @Test
    public void deferDispose() {
        BombTrackerComponent tracker = new BombTrackerComponent();
        new Entity().addComponent(tracker);
        Entity bomb = makeBombEntity(2.0f, 2.5f, (short) 0);

        tracker.trackBomb(bomb);
        assertEquals(1, tracker.getNumTracked(), "Bomb should be tracked");

        // Request disposal — should not remove immediately
        bomb.getEvents().trigger("bomb:disposeRequested");
        assertEquals(1, tracker.getNumTracked(), "Disposal is deferred; still tracked this frame");

        // Next tick processes the queue
        tracker.update();
        assertEquals(0, tracker.getNumTracked(), "Bomb should be disposed/untracked on next update()");
    }

    @Test
    public void dupTrack_doesNotAddTwice() {
        BombTrackerComponent tracker = new BombTrackerComponent();
        new Entity().addComponent(tracker);
        Entity bomb = makeBombEntity(2.0f, 2.5f, (short) 0);

        tracker.trackBomb(bomb);
        tracker.trackBomb(bomb); // Dup
        assertEquals(1, tracker.getNumTracked(), "Duplicate track should be ignored");
    }

    @Test
    public void noDuplicateDispose() {
        BombTrackerComponent tracker = new BombTrackerComponent();
        new Entity().addComponent(tracker);
        Entity bomb = makeBombEntity(2.0f, 2.5f, (short) 0);

        tracker.trackBomb(bomb);
        assertEquals(1, tracker.getNumTracked());

        // Fire disposeRequested twice; tracker should enqueue once due to de-dupe
        bomb.getEvents().trigger("bomb:disposeRequested");
        bomb.getEvents().trigger("bomb:disposeRequested");

        // Process queue once; bomb should now be gone
        tracker.update();
        assertEquals(0, tracker.getNumTracked(), "Only one dispose should occur despite duplicate requests");
    }

    @Test
    void bulkDispose_removesAllBombs() {
        BombTrackerComponent tracker = new BombTrackerComponent();
        new Entity().addComponent(tracker);

        Entity b1 = makeBombEntity(2.0f, 2.5f, (short) 0);
        Entity b2 = makeBombEntity(2.0f, 2.5f, (short) 0);
        tracker.trackBomb(b1);
        tracker.trackBomb(b2);
        assertEquals(2, tracker.getNumTracked(), "Both bombs should be tracked");

        // Simulate level reset: owner's dispose() calls tracker.dispose()
        tracker.dispose();
        assertEquals(0, tracker.getNumTracked(), "All tracked bombs should be disposed synchronously");
    }

    @Test
    void requestThenReset_noDoubleDispose() {
        BombTrackerComponent tracker = new BombTrackerComponent();
        new Entity().addComponent(tracker);
        Entity bomb = makeBombEntity(2.0f, 2.5f, (short) 0);

        tracker.trackBomb(bomb);
        bomb.getEvents().trigger("bomb:disposeRequested"); // queued

        // Reset path disposes immediately and clears internal queues
        tracker.dispose();
        assertEquals(0, tracker.getNumTracked(), "Tracker should be empty after reset-time dispose");

        // A later update() must not attempt to dispose again (no errors, no state changes)
        tracker.update();
        assertEquals(0, tracker.getNumTracked(), "Update after reset should be a no-op");
    }
}
