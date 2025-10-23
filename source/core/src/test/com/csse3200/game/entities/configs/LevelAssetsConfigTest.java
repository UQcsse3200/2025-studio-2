package com.csse3200.game.entities.configs;

import com.badlogic.gdx.utils.Json;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LevelAssetsConfigTest {
    private final Json json = new Json();

    @Test
    void deserializes_assetLists() {
        String s = """
      {
        "textures": ["images/empty.png","images/ui.png"],
        "atlases":  ["sprites/main.atlas"],
        "sounds":   ["sfx/click.wav"],
        "music":    ["music/track.ogg"]
      }
      """;

        LevelAssetsConfig a = json.fromJson(LevelAssetsConfig.class, s);
        assertEquals(List.of("images/empty.png","images/ui.png"), a.textures);
        assertEquals(List.of("sprites/main.atlas"), a.atlases);
        assertEquals(List.of("sfx/click.wav"), a.sounds);
        assertEquals(List.of("music/track.ogg"), a.music);
    }
}
