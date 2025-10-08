package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.enemy.BombTrackerComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that makes an entity automatically drop bombs at regular intervals.
 * This runs independently of the AI task system.
 */
public class AutoBombDropComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(AutoBombDropComponent.class);

    private final Entity target;
    private final float cooldown;
    private final GameTime timeSource;
    private long lastDropTime;

    /**
     * Create an automatic bomb dropping component.
     * @param target reference entity (usually player) for bomb targeting
     * @param cooldown time between drops in seconds
     */
    public AutoBombDropComponent(Entity target, float cooldown) {
        this.target = target;
        this.cooldown = cooldown;
        this.timeSource = ServiceLocator.getTimeSource();
    }

    @Override
    public void create() {
        super.create();
        lastDropTime = timeSource.getTime();
    }

    @Override
    public void update() {
        // Check if enough time has passed
        if (timeSource.getTimeSince(lastDropTime) >= cooldown * 1000) {
            dropBomb();
            lastDropTime = timeSource.getTime();
        }
    }

    /**
     * Create and register a bomb entity (deferred to avoid nested iteration)
     */
    private void dropBomb() {
        Vector2 dronePos = entity.getPosition().cpy();
        Vector2 bombSpawnPos = new Vector2(
                dronePos.x + entity.getScale().x / 2,
                dronePos.y - 0.5f
        );

        Vector2 targetPos = target != null ? target.getPosition().cpy() :
                new Vector2(bombSpawnPos.x, bombSpawnPos.y - 5f);

        Entity bomb = ProjectileFactory.createBomb(
                entity,
                bombSpawnPos,
                targetPos,
                2.0f,
                2f,
                30
        );
        ServiceLocator.getEntityService().register(bomb);
        BombTrackerComponent bomberComp = entity.getComponent(BombTrackerComponent.class);
        if (bomberComp != null) {
            bomberComp.trackBomb(bomb);
        }
        logger.debug("Auto-bomb dropped at {}", bombSpawnPos);
    }
}