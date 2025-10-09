package com.csse3200.game.components.computerterminal;

import org.junit.jupiter.api.Test;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleCaptchaBankTest {
    @Test
    void throwsWhenEmpty() {
        SimpleCaptchaBank bank = new SimpleCaptchaBank();
        assertThrows(IllegalStateException.class, () -> bank.random(new Random(1)));
    }

    @Test
    void returnsAddedSpecs() {
        SimpleCaptchaBank bank = new SimpleCaptchaBank()
                .add(new SpritesheetSpec("images/puzzles/waldo_4x4.png", 4, 4, Set.of(3), "Find Waldo"))
                .add(new SpritesheetSpec("images/puzzles/tutor_1x2.png", 1, 2, Set.of(0), "Pick tutor"));

        var rng = new Random(1234);
        CaptchaSpecLike s1 = bank.random(rng);
        CaptchaSpecLike s2 = bank.random(rng);
        assertNotNull(s1);
        assertNotNull(s2);
        assertTrue((s1 instanceof SpritesheetSpec) && (s2 instanceof SpritesheetSpec));
    }
}
