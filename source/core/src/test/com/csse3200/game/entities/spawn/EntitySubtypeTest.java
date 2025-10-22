package com.csse3200.game.entities.spawn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EntitySubtypeTest {
    @Test
    void fromString_known() {
        assertEquals(EntitySubtype.POTION, EntitySubtype.fromString("potion"));
        assertEquals(EntitySubtype.POTION, EntitySubtype.fromString("poTioN"));
    }

    @Test
    void fromString_unknown() {
        assertThrows(IllegalArgumentException.class, () -> EntitySubtype.fromString("spooky"));
    }


    @Test
    void fromString_null_returnsNull() {
        assertNull(EntitySubtype.fromString(null));
    }

    @Test
    void fromString_blank_returnsNull() {
        assertNull(EntitySubtype.fromString(""));
        assertNull(EntitySubtype.fromString("   "));
        assertNull(EntitySubtype.fromString("\n\t"));
    }

    @Test
    void fromString_trimsWhitespace() {
        assertEquals(EntitySubtype.POTION, EntitySubtype.fromString("  potion  "));
        assertEquals(EntitySubtype.POTION, EntitySubtype.fromString("\tPoTiOn\n"));
    }


    @Test
    void toString_returnsName() {
        for (EntitySubtype subtype : EntitySubtype.values()) {
            assertEquals(subtype.name().toLowerCase(), subtype.toString(),
                    "toString() should return the name for " + subtype.name());
        }
    }

    @Test
    void fromString_matchesToString() {
        for (EntitySubtype subtype : EntitySubtype.values()) {
            String text = subtype.toString();
            assertEquals(subtype, EntitySubtype.fromString(text),
                    "fromString should return the same subtype for " + text);
        }
    }

    @Test
    void validateMatch_acceptsMatchingTypeAndSubtype() {
        for (EntitySubtype subtype : EntitySubtype.values()) {
            EntityType owningType = subtype.getType();
            assertDoesNotThrow(() -> EntitySubtype.validateMatch(owningType, subtype));
        }
    }

    @Test
    void validateMatch_allowsNullSubtype() {
        assertDoesNotThrow(() -> EntitySubtype.validateMatch(EntityType.COLLECTABLE, null));
    }

    @Test
    void validateMatch_mismatch() {
        EntitySubtype subtype = EntitySubtype.MOVEABLE;
        EntityType wrongType = EntityType.COLLECTABLE;
        assertThrows(IllegalArgumentException.class, () -> EntitySubtype.validateMatch(wrongType, subtype));
    }
}