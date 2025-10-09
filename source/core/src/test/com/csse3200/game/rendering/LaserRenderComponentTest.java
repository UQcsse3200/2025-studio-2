package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.lasers.LaserEmitterComponent;
import com.csse3200.game.components.lasers.LaserShowerComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

    LaserRenderComponent render;
    SpriteBatch batch;
    TextureRegion pixel;

    @BeforeEach
    void setupCommon() throws Exception {
        render = new LaserRenderComponent();
        batch = mock(SpriteBatch.class);
        pixel = mock(TextureRegion.class);

        // inject pixel
        Field fPixel = LaserRenderComponent.class.getDeclaredField("pixel");
        fPixel.setAccessible(true);
        fPixel.set(render, pixel);
    }

    private void injectEmitter(Object emitter, String fieldName) throws Exception {
        Field fEmitter = LaserRenderComponent.class.getDeclaredField(fieldName);
        fEmitter.setAccessible(true);
        fEmitter.set(render, emitter);
    }

    private void verifySegmentDraw(Vector2 a, Vector2 b) {
        float dx = b.x - a.x;
        float dy = b.y - a.y;
        float len = (float) Math.hypot(dx, dy);
        if (len < 1e-4f) return;

        float angleDeg = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
        verify(batch, atLeastOnce()).draw(pixel, a.x, a.y - THICKNESS / 2f,
                0f, THICKNESS / 2f, len, THICKNESS, 1f, 1f, angleDeg);
    }

    abstract class BaseLaserTest<T extends com.csse3200.game.components.Component> {
        Entity laser;
        T emitter;

        void setupEmitter(Class<T> emitterClass) throws Exception {
            laser = new Entity();
            emitter = mock(emitterClass);
            laser.addComponent(emitter);
            laser.addComponent(render);

            String fieldName = (emitter instanceof LaserEmitterComponent) ? "mainEmitter" : "showerEmitter";
            injectEmitter(emitter, fieldName);
        }

        void verifyCoreDraw(List<Vector2> positions) {
            for (int i = 0; i < positions.size() - 1; i++) {
                verifySegmentDraw(positions.get(i), positions.get(i + 1));
            }
            verify(batch, atLeastOnce()).setColor(1f, 1f, 1f, 1f);
        }
    }

    @Nested
    class LaserShowerTests extends BaseLaserTest<LaserShowerComponent> {

        @BeforeEach
        void setUp() throws Exception {
            setupEmitter(LaserShowerComponent.class);
        }

        @Test
        void draw_shouldDrawCoreProperly() {
            List<Vector2> pos = List.of(new Vector2(0, 0), new Vector2(4, 4));
            when(emitter.getPositions()).thenReturn(pos);

            render.render(batch);

            verify(batch, atLeastOnce()).setColor(color);
            verifyCoreDraw(pos);
        }

        @Test
        void draw_shouldDoGlowPassBeforeCore() {
            List<Vector2> pos = List.of(new Vector2(0, 0), new Vector2(3, 2));
            when(emitter.getPositions()).thenReturn(pos);

            render.render(batch);

            InOrder inOrder = inOrder(batch);
            inOrder.verify(batch).flush();
            inOrder.verify(batch).setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

            Vector2 a = pos.get(0), b = pos.get(1);
            for (int s = GLOW_STEPS; s >= 1; s--) {
                float t = THICKNESS * (1f + (GLOW_MULT - 1f) * (s / (float) GLOW_STEPS));
                float aGlow = GLOW_ALPHA * (s / (float) GLOW_STEPS);
                inOrder.verify(batch).setColor(glowColor.r, glowColor.g, glowColor.b, aGlow);

                float originY = t / 2f;
                inOrder.verify(batch).draw(pixel, a.x, a.y - originY, 0f, originY,
                        (float) Math.hypot(b.x - a.x, b.y - a.y), t, 1f, 1f,
                        MathUtils.atan2(b.y - a.y, b.x - a.x) * MathUtils.radiansToDegrees);
            }

            inOrder.verify(batch).flush();
            inOrder.verify(batch).setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }

        @Test
        void draw_shouldDoMultipleCoreDraws() {
            List<Vector2> pos = List.of(new Vector2(0, 0), new Vector2(4, 4), new Vector2(3, 2));
            when(emitter.getPositions()).thenReturn(pos);

            render.render(batch);
            verifyCoreDraw(pos);
        }
    }

    @Nested
    class LaserEmitterTests extends BaseLaserTest<LaserEmitterComponent> {

        @BeforeEach
        void setUp() throws Exception {
            setupEmitter(LaserEmitterComponent.class);
        }

        @Test
        void draw_shouldDrawCoreProperly() {
            List<Vector2> pos = List.of(new Vector2(0, 0), new Vector2(4, 4));
            when(emitter.getPositions()).thenReturn(pos);

            render.render(batch);

            verify(batch, atLeastOnce()).setColor(color);
            verifyCoreDraw(pos);
        }

        @Test
        void draw_shouldDoGlowPassBeforeCore() {
            List<Vector2> pos = List.of(new Vector2(0, 0), new Vector2(3, 2));
            when(emitter.getPositions()).thenReturn(pos);

            render.render(batch);

            InOrder inOrder = inOrder(batch);
            inOrder.verify(batch).flush();
            inOrder.verify(batch).setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

            Vector2 a = pos.get(0), b = pos.get(1);
            for (int s = GLOW_STEPS; s >= 1; s--) {
                float t = THICKNESS * (1f + (GLOW_MULT - 1f) * (s / (float) GLOW_STEPS));
                float aGlow = GLOW_ALPHA * (s / (float) GLOW_STEPS);
                inOrder.verify(batch).setColor(glowColor.r, glowColor.g,glowColor.b, aGlow);

                float originY = t / 2f;
                inOrder.verify(batch).draw(pixel, a.x, a.y - originY, 0f, originY,
                        (float) Math.hypot(b.x - a.x, b.y - a.y), t, 1f, 1f,
                        MathUtils.atan2(b.y - a.y, b.x - a.x) * MathUtils.radiansToDegrees);
            }

            inOrder.verify(batch).flush();
            inOrder.verify(batch).setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }

        @Test
        void draw_shouldDoMultipleDraws() {
            List<Vector2> pos = List.of(new Vector2(0, 0), new Vector2(4, 4),
                    new Vector2(6, 2), new Vector2(8, 5));
            when(emitter.getPositions()).thenReturn(pos);

            render.render(batch);
            verifyCoreDraw(pos);
        }
    }
}

