package com.csse3200.game.services;

import com.csse3200.game.entities.configs.CollectablesConfig;
import com.csse3200.game.files.FileLoader;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Simple service to load collectable configs and provide lookup by id.
 * Assumes configs/items.json is valid.
 */
public final class CollectableService {
    private static Map<String, CollectablesConfig> byId;

    private CollectableService() {}

    /** Load configs from a JSON array file */
    public static void load(String path) {
        CollectablesConfig[] items =
                FileLoader.readClass(CollectablesConfig[].class, path);
        byId = Arrays.stream(items).collect(Collectors.toMap(c -> c.id, Function.identity()));
    }

    /** Get config by id (null if not found). */
    public static CollectablesConfig get(String id) {
        return byId.get(id);
    }
}
