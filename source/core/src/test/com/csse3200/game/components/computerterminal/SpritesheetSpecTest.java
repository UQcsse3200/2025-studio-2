package com.csse3200.game.components.computerterminal;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class SpritesheetSpecTest {
    @Test
    void fieldsExposeCorrectly() {
        SpritesheetSpec spec = new SpritesheetSpec("p.png", 5, 5, Set.of(0, 6, 24), "Prompt");
        assertEquals("p.png", spec.texturePath());
        assertEquals(5, spec.rows());
        assertEquals(5, spec.cols());
        assertEquals(Set.of(0, 6, 24), spec.correct());
        assertEquals("Prompt", spec.prompt());
        assertEquals(CaptchaSpecLike.ImageSource.SPRITESHEET, spec.source());
    }
}
