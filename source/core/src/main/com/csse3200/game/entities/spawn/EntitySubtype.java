package com.csse3200.game.entities.spawn;

/** Entity subtypes, each bound to a parent {@link EntityType}. */
public enum EntitySubtype {
    // Platforms
    VOLATILE(EntityType.PLATFORM),
    STATIC_PLATFORM(EntityType.PLATFORM),
    MOVING(EntityType.PLATFORM),
    PLATE(EntityType.PLATFORM),
    BUTTON(EntityType.PLATFORM),

    // Collectables
    KEY(EntityType.COLLECTABLE),
    POTION(EntityType.COLLECTABLE),
    UPGRADE(EntityType.COLLECTABLE),

    // Boxes
    MOVEABLE(EntityType.BOX),
    WEIGHTED(EntityType.BOX),
    REFLECTABLE(EntityType.BOX),

    // Floors
    GROUND(EntityType.FLOOR),
    STATIC_FLOOR(EntityType.FLOOR),
    DECORATIVE(EntityType.FLOOR),

    // Upgrades
    JETPACK(EntityType.UPGRADE),

    // Door
    STATIC(EntityType.DOOR),
    TRANSITION(EntityType.DOOR),

    // Enemies
    AUTO_BOMBER(EntityType.ENEMY),
    SELF_DESTRUCT(EntityType.ENEMY),

    // Tutorials
    JUMP(EntityType.TUTORIAL),
    DOUBLE_JUMP(EntityType.TUTORIAL),
    DASH(EntityType.TUTORIAL),

    // Pressure Plates
    LADDER(EntityType.PRESSURE_PLATE),
    NORMAL(EntityType.PRESSURE_PLATE),;

    /** Parent type for this subtype. */
    private final EntityType type;
    EntitySubtype(EntityType type) { this.type = type; }
    public EntityType getType() { return type; }

    /** Case-insensitive parse; returns null for null/blank. */
    public static EntitySubtype fromString(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return EntitySubtype.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown subtype: " + s, ex);
        }
    }

    /** Throws if subtype (when non-null) doesn’t belong to type. */
    public static void validateMatch(EntityType type, EntitySubtype subtype) {
        if (subtype != null && subtype.getType() != type) {
            throw new IllegalArgumentException("Subtype " + subtype + " is not valid for type " + type);
        }
    }

    @Override public String toString() { return name().toLowerCase(); }
}
