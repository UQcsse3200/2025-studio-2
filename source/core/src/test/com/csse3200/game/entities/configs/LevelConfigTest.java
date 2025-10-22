package com.csse3200.game.entities.configs;

import com.badlogic.gdx.utils.Json;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LevelConfigTest {
    private final Json json = new Json();

    @Test
    void defaults_areInitialized() {
        LevelConfig c = new LevelConfig();
        assertNull(c.name);
        assertNull(c.mapSize);
        assertNull(c.playerSpawn);
        assertNull(c.miniMap);
        assertNull(c.music);
        assertNotNull(c.walls);
        assertEquals(0.1f, c.walls.thickness, 1e-6);
        assertNotNull(c.entities);
        assertTrue(c.entities.isEmpty());
    }

    @Test
    void deserializes_fullLevel() {
        String s = """
      {
        "name":"Surface Level",
        "mapSize":[100,40],
        "playerSpawn":[3,4],
        "miniMap":"images/minimap.png",
        "music":"sounds/theme.ogg",
        "walls":{"thickness":0.25},
        "entities":[
          {
            "type":"collectable","subtype":"potion","id":"p1",
            "linked":"plate-1","target":"health","tooltip":"Heals",
            "extra":"toggle","direction":"right",
            "x":8,"y":30,"sx":1.2,"sy":0.8,"speed":2.5,"range":6.0,
            "rotation":45.0,"dx":1.0,"dy":-1.0,
            "safeX":2.0,"safeY":5.0,
            "centerX":false,"centerY":true,"isVisible":false,
            "height":5,"offset":2
          }
        ]
      }
      """;

        LevelConfig c = json.fromJson(LevelConfig.class, s);
        assertEquals("Surface Level", c.name);
        assertArrayEquals(new int[]{100,40}, c.mapSize);
        assertArrayEquals(new int[]{3,4}, c.playerSpawn);
        assertEquals("images/minimap.png", c.miniMap);
        assertEquals("sounds/theme.ogg", c.music);
        assertEquals(0.25f, c.walls.thickness, 1e-6);
        assertEquals(1, c.entities.size());

        LevelConfig.E e = c.entities.getFirst();
        assertEquals("collectable", e.type);
        assertEquals("potion", e.subtype);
        assertEquals("p1", e.id);
        assertEquals("plate-1", e.linked);
        assertEquals("health", e.target);
        assertEquals("Heals", e.tooltip);
        assertEquals("toggle", e.extra);
        assertEquals("right", e.direction);
        assertEquals(8, e.x);
        assertEquals(30, e.y);
        assertEquals(1.2f, e.sx, 1e-6);
        assertEquals(0.8f, e.sy, 1e-6);
        assertEquals(2.5f, e.speed, 1e-6);
        assertEquals(6.0f, e.range, 1e-6);
        assertEquals(45.0f, e.rotation, 1e-6);
        assertEquals(1.0f, e.dx, 1e-6);
        assertEquals(-1.0f, e.dy, 1e-6);
        assertEquals(2.0f, e.safeX, 1e-6);
        assertEquals(5.0f, e.safeY, 1e-6);
        assertEquals(Boolean.FALSE, e.centerX);
        assertEquals(Boolean.TRUE, e.centerY);
        assertEquals(Boolean.FALSE, e.isVisible);
        assertEquals(5, e.height);
        assertEquals(2, e.offset);
    }
}
