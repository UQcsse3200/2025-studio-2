package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class HoverEffectHelperTest {

    private Skin skin;

    @BeforeEach
    void setup() {
        skin = new Skin();
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = new com.badlogic.gdx.graphics.g2d.BitmapFont(); // dummy font
        skin.add("default", style);
    }

    @Test
    @DisplayName("Idle pulse attaches looping action")
    void idlePulseAddsAction() {
        TextButton button = new TextButton("Pulse", skin);
        HoverEffectHelper.applyIdlePulse(button, Color.RED, Color.DARK_GRAY, 0.5f);

        assertFalse(button.getActions().isEmpty(), "Idle pulse should add actions");
    }

    @Test
    @DisplayName("Fade-in sets alpha to 0 initially")
    void fadeInSetsAlpha() {
        TextButton button = new TextButton("Fade", skin);
        HoverEffectHelper.applyFadeIn(button, 0.5f, 1f);

        assertEquals(0f, button.getColor().a, 0.01, "Fade-in should set alpha to 0");
    }

    @Test
    @DisplayName("Subtle pulse attaches looping action")
    void subtlePulseAddsAction() {
        TextButton button = new TextButton("Subtle", skin);
        HoverEffectHelper.applySubtlePulse(button, 1.05f, 0.6f);

        assertFalse(button.getActions().isEmpty(), "Subtle pulse should add actions");
    }

    @Test
    @DisplayName("Interruptible pulse attaches listener and action")
    void hoverInterruptiblePulseAddsListenerAndAction() {
        TextButton button = new TextButton("Interruptible", skin);
        HoverEffectHelper.applyHoverInterruptiblePulse(button, 1.05f, 0.6f);

        assertFalse(button.getListeners().isEmpty(), "Should add a listener");
        assertFalse(button.getActions().isEmpty(), "Should add initial pulse action");
    }
}
