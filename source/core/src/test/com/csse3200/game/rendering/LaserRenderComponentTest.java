package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.lasers.LaserShowerComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;

import java.lang.reflect.Field;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class LaserRenderComponentTest {
    private static final float THICKNESS  = 0.05f; // core beam thickness
    private static final int   GLOW_STEPS = 4;     // glow smoothness
    private static final float GLOW_MULT  = 2.7f;  // outer glow thickness multiplier
    private static final float GLOW_ALPHA = 0.35f; // max glow alpha
    private final Color color = new Color(1f, 0f, 0f, 1f);
    private final Color glowColor = new Color(1f, 0.32f, 0.32f, 1f);

    Entity laser;
    LaserShowerComponent showerEmitter;
    LaserRenderComponent render;
    SpriteBatch batch;
    TextureRegion pixel;

    @BeforeEach
    void setUp() throws Exception {
        laser = new Entity();
        showerEmitter = mock(LaserShowerComponent.class);
        render = new LaserRenderComponent();
        batch = mock(SpriteBatch.class);
        pixel = mock(TextureRegion.class);

        laser.addComponent(showerEmitter);
        laser.addComponent(render);

        // inject mock showerEmitter into the correct field
        Field fShower = LaserRenderComponent.class.getDeclaredField("showerEmitter");
        fShower.setAccessible(true);
        fShower.set(render, showerEmitter);

        // set texture region to verify function calls correctly
        Field fPixel = LaserRenderComponent.class.getDeclaredField("pixel");
        fPixel.setAccessible(true);
        fPixel.set(render, pixel);
    }

    @Test
    void draw_shouldDrawCoreProperly() {
        List<Vector2> pos = List.of(new Vector2(0f, 0f), new Vector2(4f, 4f));
        when(showerEmitter.getPositions()).thenReturn(pos);

        render.render(batch);

        Vector2 a = pos.get(0);
        Vector2 b = pos.get(1);

        float dx = b.x - a.x;
        float dy = b.y - a.y;
        float len = (float) Math.hypot(dx, dy);
        float angleDeg = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

        verify(batch, atLeastOnce()).flush();
        verify(batch).setColor(color);

        float originX = 0f;
        float originY = THICKNESS / 2f;
        verify(batch).draw(pixel, a.x, a.y - originY, originX, originY, len, THICKNESS, 1f, 1f, angleDeg);
        verify(batch).setColor(1f, 1f, 1f, 1f);
    }

    @Test
    void draw_shouldDoGlowPassBeforeCore() {
        List<Vector2> pos = List.of(new Vector2(0f, 0f), new Vector2(3f, 2f));
        when(showerEmitter.getPositions()).thenReturn(pos);

        render.render(batch);

        InOrder inOrder = inOrder(batch);

        inOrder.verify(batch).flush();
        inOrder.verify(batch).setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        Vector2 a = pos.get(0);
        Vector2 b = pos.get(1);

        float dx = b.x - a.x;
        float dy = b.y - a.y;
        float len = (float) Math.hypot(dx, dy);
        float angleDeg = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

        for (int s = GLOW_STEPS; s >= 1; s--) {
            float t = THICKNESS * (1f + (GLOW_MULT - 1f) * (s / (float) GLOW_STEPS));
            float aGlow = GLOW_ALPHA * (s / (float) GLOW_STEPS);
            inOrder.verify(batch).setColor(glowColor.r, glowColor.g, glowColor.b, aGlow);

            float originX = 0f;
            float originY = t / 2f;
            inOrder.verify(batch).draw(pixel, a.x, a.y - originY, originX, originY, len, t, 1f, 1f, angleDeg);
        }

        inOrder.verify(batch).flush();
        inOrder.verify(batch).setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Test
    void draw_shouldDoMultipleCoreDraws() {
        List<Vector2> pos = List.of(new Vector2(0f, 0f), new Vector2(4f, 4f), new Vector2(3f, 2f));
        when(showerEmitter.getPositions()).thenReturn(pos);

        render.render(batch);

        InOrder inOrder = inOrder(batch);
        verify(batch, times(4)).flush(); // 2 flushes per segment

        for (int i = 0; i < pos.size() - 1; i++) {
            Vector2 a = pos.get(i);
            Vector2 b = pos.get(i + 1);

            float dx = b.x - a.x;
            float dy = b.y - a.y;
            float len = (float) Math.hypot(dx, dy);
            if (len < 1e-4f) continue;

            float angleDeg = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

            inOrder.verify(batch).setColor(color);
            float originX = 0f;
            float originY = THICKNESS / 2f;
            inOrder.verify(batch).draw(pixel, a.x, a.y - originY, originX, originY, len, THICKNESS, 1f, 1f, angleDeg);
        }

        inOrder.verify(batch).setColor(1f, 1f, 1f, 1f);
    }
}