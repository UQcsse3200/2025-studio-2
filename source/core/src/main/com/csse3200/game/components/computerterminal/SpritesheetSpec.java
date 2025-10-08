package com.csse3200.game.components.computerterminal;

import java.util.Set;

public record SpritesheetSpec(
        String texturePath,  // e.g. "images/puzzles/waldo_4x4.png"
        int rows,
        int cols,
        Set<Integer> correct,
        String prompt
) implements CaptchaSpecLike {
    @Override public ImageSource source() { return ImageSource.SPRITESHEET; }
    @Override public String prompt() { return prompt; }
    @Override public int rows() { return rows; }
    @Override public int cols() { return cols; }
    @Override public Set<Integer> correct() { return correct; }
    public String texturePath() { return texturePath; }
}