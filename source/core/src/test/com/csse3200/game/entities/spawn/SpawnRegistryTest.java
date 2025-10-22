package com.csse3200.game.entities.spawn;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.LevelConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpawnRegistryTest {

    /** mock spawner that gets args and returns a fixed entity ♡ */
    static class MockSpawner implements EntitySpawner {
        LevelConfig.E args;
        Entity toReturn = new Entity();

        @Override
        public Entity spawn(LevelConfig.E a) {
            this.args = a;
            return toReturn;
        }
    }

    @BeforeEach
    @AfterEach
    void clean() {
        SpawnRegistry.clear();
    }

    @Test
    void register_then_build_callsSpawner_and_returnsEntity() {
        MockSpawner spawner = new MockSpawner();
        SpawnRegistry.register("collectable", spawner);

        LevelConfig.E args = new LevelConfig.E();
        Entity out = SpawnRegistry.build("collectable", args);

        assertSame(spawner.toReturn, out, "build should return spawner result");
        assertSame(args, spawner.args, "build should forward the exact same LevelConfig.E instance");
    }

    @Test
    void build_unknownType() {
        LevelConfig.E args = new LevelConfig.E();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> SpawnRegistry.build("missing", args));
        assertTrue(ex.getMessage().contains("No spawner"));
    }

    @Test
    void unregister_removesOnlyUnregistered() {
        MockSpawner a = new MockSpawner();
        MockSpawner b = new MockSpawner();

        SpawnRegistry.register("enemy", a);
        SpawnRegistry.register("floor", b);

        // remove one
        SpawnRegistry.unregister("enemy");

        // verify remaining key still works
        Entity out = SpawnRegistry.build("floor", new LevelConfig.E());
        assertSame(b.toReturn, out);

        // removed key now throws
        assertThrows(IllegalArgumentException.class, () -> SpawnRegistry.build("enemy", new LevelConfig.E()));
    }

    @Test
    void clear_removesAllKeys() {
        SpawnRegistry.register("a", new MockSpawner());
        SpawnRegistry.register("b", new MockSpawner());

        SpawnRegistry.clear();

        assertThrows(IllegalArgumentException.class, () -> SpawnRegistry.build("a", new LevelConfig.E()));
        assertThrows(IllegalArgumentException.class, () -> SpawnRegistry.build("b", new LevelConfig.E()));
    }

    @Test
    void duplicateRegistration_mostRecentPrevailsMwahah() {
        MockSpawner a = new MockSpawner();
        MockSpawner b = new MockSpawner();
        Entity entity = new Entity();
        b.toReturn = entity;

        SpawnRegistry.register("duplicate", a);
        SpawnRegistry.register("duplicate", b); // overwrite

        Entity out = SpawnRegistry.build("duplicate", new LevelConfig.E());
        assertSame(entity, out, "second registration should overwrite the first");
    }

    @Test
    void privateConstructor_forCoverage() throws Exception {
        var ctor = SpawnRegistry.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object instance = ctor.newInstance();
        assertNotNull(instance);
    }
}
