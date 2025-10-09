package com.csse3200.game.components.enemy;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;

/**
 * Tracks unexploded bombs spawned by the owner and handles their disposal.
 * When a bomb finishes (explodes) it emits "bomb:disposeRequested". This component handles bomb disposal on the
 * next update tick.
 * The dispose method for this component ensures that all tracked bombs are removed on level reset events.
 * Bombs must emit two events:
 * - "bomb:disposeRequested": The bomb is scheduled for disposal on next update
 * - "bomb:disposed": Fired by the bomb during its own disposal, the tracker removes it from the active list.
 */
public class BombTrackerComponent extends Component {
    /**
     * Creates a bomb tracker component that manages the lifecycle of bombs spawned by the owner entity.
     */
    public BombTrackerComponent() {}

    private final Array<Entity> activeBombs = new Array<>();
    private final Array<Entity> pendingDisposals = new Array<>();

    /**
     * Register a newly spawned bomb for lifecycle management.
     * Ignored if bomb is null or already tracked.
     * @param bomb entity being to be tracked
     */
    public void trackBomb(Entity bomb) {
        if (bomb == null || activeBombs.contains(bomb, true)) return;
        activeBombs.add(bomb);

        // Bomb requests disposal (normal explosion flow)
        bomb.getEvents().addListener("bomb:disposeRequested", () -> {
            if (!pendingDisposals.contains(bomb, true)) {
                pendingDisposals.add(bomb);
            }
        });

        // Stop tracking after bomb is disposed and fires event
        bomb.getEvents().addListener("bomb:disposed", () -> {
            activeBombs.removeValue(bomb, true);
        });
    }

    /**
     * Processes any pending disposal requests from bombs. Ensures that calls to bomb.dispose()
     * occur outside the frame in which the request was raised.
     */
    @Override
    public void update() {
        if (pendingDisposals.size == 0) return;
        Array<Entity> toDispose = new Array<>(pendingDisposals);
        pendingDisposals.clear();
        for (Entity bomb : toDispose) {
            if (bomb != null && activeBombs.contains(bomb, true)) {
                bomb.dispose();
            }
        }
    }

    /**
     * Dispose of all tracked bombs at once.
     */
    @Override
    public void dispose() {
        Array<Entity> active = new Array<>(activeBombs);
        for (Entity bomb : active) {
            if (bomb != null) bomb.dispose();
        }
        activeBombs.clear();
        pendingDisposals.clear();
    }

    /**
     * Number of bombs currently tracked (unexploded)
     * @return int number of active bombs spawned by the entity
     */
    public int getNumTracked() {
        return activeBombs.size;
    }
}
