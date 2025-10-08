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
import org.mockito.Mockito;

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
    LaserShowerComponent emitter;
    LaserRenderComponent render;
    SpriteBatch batch;
    TextureRegion pixel;

    @BeforeEach
    void setUp() throws Exception {
        laser = new Entity();
        emitter = mock(LaserShowerComponent.class);
        render = new  LaserRenderComponent();
        batch = mock(SpriteBatch.class);
        pixel = mock(TextureRegion.class);

        laser.addComponent(emitter);
        laser.addComponent(render);

        // forcefully register emitter with renderer to avoid create()
        Field fEmitter = LaserRenderComponent.class.getDeclaredField("emitter");
        fEmitter.setAccessible(true);
        fEmitter.set(render, emitter);

        // set texture region to verify function calls correctly
        Field fPixel = LaserRenderComponent.class.getDeclaredField("pixel");
        fPixel.setAccessible(true);
        fPixel.set(render, pixel);


    }

    @Test
    void draw_shouldDrawCoreProperly() {
        // setup fake position list with starting pos and end collision
        List<Vector2> pos = List.of(new Vector2(0f, 0f), new Vector2(4f, 4f));

        when(emitter.getPositions()).thenReturn(pos);

        render.render(batch);

        // same math as whats in draw() method
        Vector2 a = pos.get(0);
        Vector2 b = pos.get(1);

        float dx = b.x - a.x;
        float dy = b.y - a.y;
        float len = (float) Math.hypot(dx, dy);

        float angleDeg = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

        // verify minimal calls for core rendering
        verify(batch, atLeastOnce()).flush();
        verify(batch).setColor(color);

        float originX = 0f;
        float originY = THICKNESS / 2f;
        verify(batch).draw(
                pixel,
                a.x, a.y - originY,
                originX, originY,
                len, THICKNESS,
                1f, 1f,
                angleDeg
        );

        verify(batch).setColor(1f, 1f, 1f, 1f);
    }

    @Test
    void draw_shouldDoGlowPassBeforeCore() {
        List<Vector2> pos = List.of(new Vector2(0f, 0f), new Vector2(3f, 2f));
        when(emitter.getPositions()).thenReturn(pos);

        render.render(batch);

        // in order: flush -> additive -> [glow draws] -> flush -> normal -> [core draw]
        InOrder inOrder = Mockito.inOrder(batch);

        // flush
        inOrder.verify(batch).flush();
        // additive
        inOrder.verify(batch).setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        // same math done in the draw() method
        Vector2 a =  pos.get(0);
        Vector2 b =  pos.get(1);

        float dx = b.x - a.x;
        float dy = b.y - a.y;
        float len = (float) Math.hypot(dx, dy);

        float angleDeg = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

        // ======== glow draws
        for (int s = GLOW_STEPS; s >= 1; s--) {
            float t = THICKNESS * (1f + (GLOW_MULT - 1f) * (s / (float) GLOW_STEPS));
            float aGlow = GLOW_ALPHA * (s / (float) GLOW_STEPS);
            inOrder.verify(batch).setColor(glowColor.r, glowColor.g, glowColor.b, aGlow);

            float originX = 0f;
            float originY = t / 2f;
            inOrder.verify(batch).draw(
                    pixel,
                    a.x, a.y - originY,
                    originX, originY,
                    len, t,
                    1f, 1f,
                    angleDeg
            );
        }

        // flush
        inOrder.verify(batch).flush();
        // after glow restore normal blending
        inOrder.verify(batch).setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // dont care about core render in this test
    }

    @Test
    void draw_shouldDoMultipleCoreDraws() {
        // setup fake position list with starting pos, reflection and end collision
        List<Vector2> pos = List.of(new Vector2(0f, 0f), new Vector2(4f, 4f), new Vector2(3f, 2f));
        when(emitter.getPositions()).thenReturn(pos);

        render.render(batch);

        // check that it renders in the correct order
        InOrder inOrder = Mockito.inOrder(batch);

        // 2 flushes per segment, 4 total
        verify(batch, times(4)).flush();

        for (int i = 0; i < pos.size() - 1; i++) {
            Vector2 a =  pos.get(i);
            Vector2 b =  pos.get(i + 1);

            float dx = b.x - a.x;
            float dy = b.y - a.y;
            float len = (float) Math.hypot(dx, dy);
            if (len < 1e-4f) continue;

            float angleDeg = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

            // verify pixel drawing
            inOrder.verify(batch).setColor(color);
            float originX = 0f;
            float originY = THICKNESS / 2f;
            inOrder.verify(batch).draw(
                    pixel,
                    a.x, a.y - originY,
                    originX, originY,
                    len, THICKNESS,
                    1f, 1f,
                    angleDeg
            );
        }

        // reset batch color at end
        inOrder.verify(batch).setColor(1f, 1f, 1f, 1f);
    }
}