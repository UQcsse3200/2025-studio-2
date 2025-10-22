package com.csse3200.game.entities.spawn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EntityTypeTest {
    @Test
    void fromString_parsesKnownValues_caseInsensitive() {
        assertEquals(EntityType.COLLECTABLE, EntityType.fromString("collectable"));
        assertEquals(EntityType.COLLECTABLE, EntityType.fromString("CoLlEcTaBlE"));
    }

    @Test
    void fromString_unknown() {
        assertThrows(IllegalArgumentException.class, () -> EntityType.fromString("unknown"));
    }

    @Test
    void fromString_null() {
        assertThrows(IllegalArgumentException.class, () -> EntityType.fromString(null));
    }

    @Test
    void toString_notNullOrBlank() {
        for (EntityType type : EntityType.values()) {
            String s = type.toString();
            assertNotNull(s, "toString() should not return null");
            assertFalse(s.isBlank(), "toString() should not be blank for " + type.name());
        }
    }

    @Test
    void toString_matchesName() {
        for (EntityType type : EntityType.values()) {
            assertEquals(type.name().toLowerCase(), type.toString(),
                    "Default toString() should equal enum constant name");
        }
    }
}
