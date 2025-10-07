package com.csse3200.game.entities.spawn;

public enum EntitySubtype {
    // Platforms
    VOLATILE(EntityType.PLATFORM),
    STATIC_PLATFORM(EntityType.PLATFORM),
    MOVING(EntityType.PLATFORM),

    // Collectables
    KEY(EntityType.COLLECTABLE),
    POTION(EntityType.COLLECTABLE),

    // Boxes
    MOVEABLE(EntityType.BOX),
    WEIGHTED(EntityType.BOX),
    REFLECTABLE(EntityType.BOX),

    // Floors
    GROUND(EntityType.FLOOR),
    STATIC_FLOOR(EntityType.FLOOR),
    DECORATIVE(EntityType.FLOOR);

    private final EntityType type;
    EntitySubtype(EntityType type) { this.type = type; }
    public EntityType getType() { return type; }

    public static EntitySubtype fromString(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return EntitySubtype.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown subtype: " + s, ex);
        }
    }

    /** Ensure subtype belongs to its parent type. */
    public static void validateMatch(EntityType type, EntitySubtype subtype) {
        if (subtype != null && subtype.getType() != type) {
            throw new IllegalArgumentException("Subtype " + subtype + " is not valid for type " + type);
        }
    }

    @Override public String toString() { return name().toLowerCase(); }
}
