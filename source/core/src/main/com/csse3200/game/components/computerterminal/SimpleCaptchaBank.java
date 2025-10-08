package com.csse3200.game.components.computerterminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Minimal in-memory collection of CAPTCHA specifications.
 * Intended to be constructed at UI creation time and populated with
 * spritesheet. Selection is random.
 */
public class SimpleCaptchaBank {
    private final List<CaptchaSpecLike> specs = new ArrayList<>();

    /**
     * Adds a spec to the bank.
     *
     * @param spec specification to add
     * @return this bank for chaining
     */
    public SimpleCaptchaBank add(CaptchaSpecLike spec) {
        specs.add(spec);
        return this;
    }

    /**
     * Returns a  random spec from the bank.
     * Throws if no specs have been added.
     *
     * @param rng random source
     * @return chosen spec
     * @throws IllegalStateException if the bank is empty
     */
    public CaptchaSpecLike random(Random rng) {
        if (specs.isEmpty()) throw new IllegalStateException("No captcha specs added");
        return specs.get(rng.nextInt(specs.size()));
    }
}
