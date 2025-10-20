package com.csse3200.game.entities.configs;

import com.badlogic.gdx.utils.Json;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CollectablesConfigTest {

    @Test
    void loadFromJson_empty() {
        CollectablesConfig c = new CollectablesConfig();

        assertNull(c.id);
        assertNull(c.name);
        assertNull(c.sprite);
        assertNull(c.description);
        assertFalse(c.autoConsume, "boolean defaults to false");
        assertEquals(0, c.ttl);
        assertNull(c.tags);
        assertNull(c.effects);
        assertNull(c.glowColor);
        assertNull(c.scale);
    }

    @Test
    void loadFromJson_withAllFields() {
        String jsonStr = """
      {
        "id": "potion:health",
        "name": "Health Potion",
        "sprite": "images/items/health.png",
        "description": "Restores a small amount of health",
        "autoConsume": true,
        "ttl": 30,
        "tags": ["potion", "consumable", "healing"],
        "effects": [
          { "type": "heal", "value": 25, "duration": 0 }
        ],
        "glowColor": [255, 0, 0],
        "scale": [1.0, 0.8]
      }
      """;

        Json json = new Json();
        CollectablesConfig c = json.fromJson(CollectablesConfig.class, jsonStr);

        assertEquals("potion:health", c.id);
        assertEquals("Health Potion", c.name);
        assertEquals("images/items/health.png", c.sprite);
        assertEquals("Restores a small amount of health", c.description);
        assertTrue(c.autoConsume);
        assertEquals(30, c.ttl);
        assertEquals(List.of("potion", "consumable", "healing"), c.tags);
        assertNotNull(c.effects);
        assertEquals("heal", c.effects.getFirst().type);
        assertEquals(25, c.effects.getFirst().value);
        assertEquals(0, c.effects.getFirst().duration);
        assertEquals(List.of(255, 0, 0), c.glowColor);
        assertEquals(List.of(1.0f, 0.8f), c.scale);
    }

    @Test
    void missingOptionalFields_defaultsRemain() {
        String jsonStr = """
      {
        "id": "potion:health",
        "name": "Health Potion",
        "sprite": "images/items/potion.png"
      }
      """;

        Json json = new Json();
        CollectablesConfig c = json.fromJson(CollectablesConfig.class, jsonStr);

        assertEquals("potion:health", c.id);
        assertEquals("Health Potion", c.name);
        assertEquals("images/items/potion.png", c.sprite);

        // Defaults
        assertFalse(c.autoConsume);
        assertEquals(0, c.ttl);
        assertNull(c.tags);
        assertNull(c.effects);
        assertNull(c.glowColor);
        assertNull(c.scale);
    }
}
