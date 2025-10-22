package com.csse3200.game.components.collectables.effects;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry mapping effect type strings to their corresponding handlers.
 * Initialise once with {@link #registerDefaults()} at game startup.
 */
public final class ItemEffectRegistry {
    private static final Map<String, ItemEffectHandler> HANDLERS = new HashMap<>();
    private ItemEffectRegistry() {}

    /** Register or replace a handler for a type (e.g. "potion:heal"). */
    public static void register(String type, ItemEffectHandler handler) {
        HANDLERS.put(type, handler);
    }

    /** Get the handler for a type, or null if none registered. */
    public static ItemEffectHandler get(String type) {
        return HANDLERS.get(type);
    }

    /** Clear all handlers */
    public static void clear() {
        HANDLERS.clear();
    }

    /** Register built-in handlers. */
    public static void registerDefaults() {
        register("heal", new HealEffect());
        register("buff_speed", new BuffSpeedEffect());
        register("upgrade", new AddUpgrade());
        register("objective", new AddObjective());
        register("misc", new AddHardware());
        register("remove_objective", new RemoveObjective());
    }
}