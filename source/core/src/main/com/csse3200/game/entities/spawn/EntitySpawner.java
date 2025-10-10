package com.csse3200.game.entities.spawn;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.LevelConfig;

/**
 * A functional interface for creating or spawning entities.
 * <p>
 * This allows entity creation logic to be passed around as method reference.
 * This way, different factories can provide their own {@code EntitySpawner} implementations.
 * </p>
 */
@FunctionalInterface
public interface EntitySpawner {

    /**
     * Spawns and returns a new {@link Entity}.
     * @return a newly created entity
     */
    Entity spawn(LevelConfig.E e);
}
