package com.csse3200.game.entities.spawn;

import com.csse3200.game.entities.Entity;
import java.util.HashMap;
import java.util.Map;

/**
 * A global registry for {@link EntitySpawner} instances.
 * <p>
 * The {@code SpawnRegistry} maps string identifiers to spawner functions,
 * allowing entities to be created dynamically by ID. This is used
 * for loading entities from configuration files.
 * </p>
 */
public final class SpawnRegistry {
    private static final Map<String, EntitySpawner> Registry = new HashMap<>();

    /** ♡ Prevent instantiation ♡ */
    private SpawnRegistry() {}

    /**
     * Registers a spawner under a given type identifier.
     * @param type the types identifier string
     * @param spawner the spawner function for creating entities
     */
    public static void register(String type, EntitySpawner spawner){
        Registry.put(type, spawner);
    }

    public static Entity build(String type, EntitySpawner.Args a){
        var spawner = Registry.get(type);
        if (spawner == null) {
            throw new IllegalArgumentException("No spawner: " + type);
        }
        return spawner.spawn(a);
    }

    /**
     * Removes a spawner from the registry.
     * @param type the type identifier of the spawner to remove
     */
    public static void unregister(String type) {
        Registry.remove(type);
    }
}
