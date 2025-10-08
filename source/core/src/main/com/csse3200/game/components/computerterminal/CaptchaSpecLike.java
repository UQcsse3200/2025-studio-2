package com.csse3200.game.components.computerterminal;


import java.util.List;
import java.util.Random;

public interface CaptchaBank {
    /** Return a random CaptchaSpec. */
    CaptchaSpec random(Random rng);
    /** Optional: list all (useful for tests). */
    List<CaptchaSpec> all();
}
