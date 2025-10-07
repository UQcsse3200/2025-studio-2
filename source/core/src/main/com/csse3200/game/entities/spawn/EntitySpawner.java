package com.csse3200.game.entities.spawn;

import com.csse3200.game.entities.Entity;

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
    Entity spawn(Args a);

    final class Args {
        public eType type;
        public Subtype subtype;
        public String linked;
        public String target;
        public String tooltip;
        public String id;
        public String extra;
        public String direction;

        public float speed = 0;
        public float rotation = 0;
        public float delay = 1.5f;

        public float sx = 1;
        public float sy = 1;
        public float dx = 0;
        public float dy = 0;
        public float safeX = 0;
        public float safeY = 0;

        public int x;
        public int y;
    }

    enum eType {
        COLLECTABLE,
        DOOR,
        PLATFORM,
        FLOOR,
        PRESSURE_PLATE,
        BOX,
        CAMERA,
        TRAP,
        LASER,
        BUTTON;

        @Override
        public String toString() {
            return name().toLowerCase();
        }

        public static eType fromString(String s) {
            if (s == null) {
                throw new IllegalArgumentException("Type is null");
            }
            try {
                return eType.valueOf(s.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown type: " + s, e);
            }
        }

    }
    enum Subtype {
        // Platforms
        VOLATILE(eType.PLATFORM),
        STATIC_PLATFORM(eType.PLATFORM),
        MOVING(eType.PLATFORM),

        // Collectables
        KEY(eType.COLLECTABLE),
        POTION(eType.COLLECTABLE),

        // Boxes
        MOVEABLE(eType.BOX),
        WEIGHTED(eType.BOX),
        REFLECTABLE(eType.BOX),

        // Floors
        GROUND(eType.FLOOR),
        STATIC_FLOOR(eType.FLOOR),
        DECORATIVE(eType.FLOOR);

        private final eType type;

        Subtype(eType type) {
            this.type = type;
        }

        public eType getType() {
            return type;
        }

        public static Subtype fromString(String s) {
            try {
                return Subtype.valueOf(s.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown subtype: " + s, e);
            }
        }

        /** Make sure a subtype belongs to its parent type */
        public static void validateMatch(eType type, Subtype subtype) {
            if (subtype != null && subtype.getType() != type) {
                throw new IllegalArgumentException(
                        "Subtype " + subtype + " is not valid for type " + type
                );
            }
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
