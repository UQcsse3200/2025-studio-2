package com.csse3200.game.components.computerterminal;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class CaptchaResultTest {
    @Test
    void holdsData() {
        var r = new CaptchaResult(true, Set.of(1,2), Set.of(1,2));
        assertTrue(r.success());
        assertEquals(Set.of(1,2), r.selected());
        assertEquals(Set.of(1,2), r.correct());
    }
}
