package com.csse3200.game.entities.spawn;

/** High-level entity types used for spawning. */
public enum EntityType {
    COLLECTABLE,
    DOOR,
    PLATFORM,
    FLOOR,
    PRESSURE_PLATE,
    BOX,
    CAMERA,
    TRAP,
    LASER,
    BUTTON,
    LASER_DETECTOR;

    @Override public String toString() { return name().toLowerCase(); }

    /** Case-insensitive parse; throws on null/unknown. */
    public static EntityType fromString(String s) {
        if (s == null) throw new IllegalArgumentException("Type is null");
        try {
            return EntityType.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown type: " + s, ex);
        }
    }
}
