package com.csse3200.game.components.computerterminal;



import java.util.Set;

public interface CaptchaSpecLike {
    enum ImageSource { ATLAS, SPRITESHEET }

    ImageSource source();
    String prompt();
    int rows();
    int cols();
    Set<Integer> correct();
}