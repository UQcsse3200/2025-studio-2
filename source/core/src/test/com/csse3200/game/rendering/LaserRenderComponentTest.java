package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.lasers.LaserEmitterComponent;
import com.csse3200.game.components.lasers.LaserShowerComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for LaserRenderComponent.
 * Verifies color initialization, core beam drawing, and glow pass behavior for both
 * LaserEmitter and LaserShower components.
 */
@ExtendWith(GameExtension.class)
class LaserRenderComponentTest {
    // max glow alpha
    private final Color emitterColor = new Color(1f, 0f, 0f, 1f);
    private final Color emitterGlowColor = new Color(1f, 0.32f, 0.32f, 1f);
    private final Color showerColor = new Color(0.05f, 0.35f, 0.6f, 1.0f);
    private final Color showerGlowColor = new Color(0.15f, 0.5f, 0.8f, 1.0f);

    private LaserRenderComponent render;
    private SpriteBatch batch;
    private TextureRegion pixel;

    // Mocks for static dependencies that need to be managed
    private static MockedStatic<ServiceLocator> mockedServiceLocator;
    private static MockedConstruction<Pixmap> mockedPixmap;
    private static MockedConstruction<Texture> mockedTexture;
    private static MockedConstruction<TextureRegion> mockedTextureRegion;

    @BeforeAll
    static void setupAll() {
        mockedServiceLocator = mockStatic(ServiceLocator.class);
        mockedPixmap = mockConstruction(Pixmap.class);
        mockedTexture = mockConstruction(Texture.class);
        mockedTextureRegion = mockConstruction(TextureRegion.class);
    }
    @AfterAll
    static void cleanupAll() {
        if (mockedServiceLocator != null) mockedServiceLocator.close();
        if (mockedPixmap != null) mockedPixmap.close();
        if (mockedTexture != null) mockedTexture.close();
        if (mockedTextureRegion != null) mockedTextureRegion.close();
    }

    @BeforeEach
    void setup() {
        RenderService mockRenderService = mock(RenderService.class);
        mockedServiceLocator.when(ServiceLocator::getRenderService).thenReturn(mockRenderService);

        render = new LaserRenderComponent();
        batch = mock(SpriteBatch.class);
        pixel = mock(TextureRegion.class);
    }


    /**
     * Helper to inject the Emitter component into the LaserRenderComponent using reflection.
     */
    private void injectEmitter(Object emitter, String fieldName) throws NoSuchFieldException, IllegalAccessException{
        Field fEmitter = LaserRenderComponent.class.getDeclaredField(fieldName);
        fEmitter.setAccessible(true);
        fEmitter.set(render, emitter);
    }

    /**
     * Helper to verify the core (innermost) laser beam segment draw call.
     * This relies on the 'pixel' mock being injected and used by the component.
     */
    private void verifyCoreDraw(List<Vector2> positions) {
        for (int i = 0; i < positions.size() - 1; i++) {
            Vector2 a = positions.get(i);
            Vector2 b = positions.get(i + 1);
            float len = (float) Math.hypot(b.x - a.x, b.y - a.y);
            if (len < 1e-4f) continue;

            verify(batch, atLeastOnce()).draw(
                    pixel,
                    a.x, a.y - 0.025f, 0f, 0.025f,
                    len, 0.05f, 1f, 1f,
                    (float) Math.toDegrees(Math.atan2(b.y - a.y, b.x - a.x))
            );
        }
        verify(batch, atLeastOnce()).setColor(1f, 1f, 1f, 1f);
    }

    /**
     * Base class for setting up a test environment with an Entity and Emitter.
     */
    abstract class BaseLaserTest<T extends com.csse3200.game.components.Component> {
        Entity laser;
        T emitter;

        void setupEmitter(Class<T> emitterClass) throws NoSuchFieldException, IllegalAccessException {
            laser = new Entity();
            emitter = mock(emitterClass);
            laser.addComponent(emitter);
            render.setEntity(laser);
            // Manually call create() now that static mocks are ready
            // NOTE: This call overwrites the 'pixel' field with a mock TextureRegion instance
            // from the mockedTextureRegion construction block, but we immediately overwrite it back.
            render.create();

            if (emitter instanceof LaserEmitterComponent) {
                when(((LaserEmitterComponent) emitter).getEnable()).thenReturn(true);
            }

            // Inject the mock 'pixel' AFTER create() has run (to be used for verification)
            Field fPixel = LaserRenderComponent.class.getDeclaredField("pixel");
            fPixel.setAccessible(true);
            fPixel.set(render, pixel);

            // Inject the emitter component for the component's internal logic
            String fieldName = (emitter instanceof LaserEmitterComponent) ? "mainEmitter" : "showerEmitter";
            injectEmitter(emitter, fieldName);
        }
    }

    /**
     * Tests specific to LaserShowerComponent rendering
     */
    @Nested
    class LaserShowerTests extends BaseLaserTest<LaserShowerComponent> {

        @BeforeEach
        void setUp() throws NoSuchFieldException, IllegalAccessException  {
            setupEmitter(LaserShowerComponent.class);
        }

        @Test
        void color_shouldBeShowerColor() {
            // Verify the color fields were correctly initialized in create()
            assertEquals(showerColor, render.getColor());
            assertEquals(showerGlowColor, render.getGlowColor());
        }

        @Test
        void draw_shouldDrawCoreProperly() {
            List<Vector2> pos = List.of(new Vector2(0, 0), new Vector2(4, 4));
            when(emitter.getPositions()).thenReturn(pos);

            render.draw(batch);

            verify(batch, atLeastOnce()).setColor(showerColor);
            verifyCoreDraw(pos);
        }

        @Test
        void draw_shouldDoGlowPassBeforeCore() {
            List<Vector2> pos = List.of(new Vector2(0, 0), new Vector2(3, 2));
            when(emitter.getPositions()).thenReturn(pos);

            render.draw(batch);

            InOrder inOrder = inOrder(batch);
            inOrder.verify(batch).flush();
            inOrder.verify(batch).setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

            for (int s = 4; s >= 1; s--) {
                float aGlow = 0.35f * (s / 4f);
                inOrder.verify(batch).setColor(showerGlowColor.r, showerGlowColor.g, showerGlowColor.b, aGlow);
            }

            inOrder.verify(batch).flush();
            inOrder.verify(batch).setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    /**
     * Tests specific to LaserEmitterComponent rendering
     */
    @Nested
    class LaserEmitterTests extends BaseLaserTest<LaserEmitterComponent> {

        @BeforeEach
        void setUp() throws NoSuchFieldException, IllegalAccessException {
            setupEmitter(LaserEmitterComponent.class);
        }

        @Test
        void color_shouldBeEmitterColor() {
            // Verify the color fields were correctly initialized in create()
            assertEquals(emitterColor, render.getColor());
            assertEquals(emitterGlowColor, render.getGlowColor());
        }


        @Test
        void draw_shouldDrawCoreProperly() {
            List<Vector2> pos = List.of(new Vector2(0, 0), new Vector2(4, 4));
            when(emitter.getPositions()).thenReturn(pos);

            render.draw(batch);

            verify(batch, atLeastOnce()).setColor(emitterColor);
            verifyCoreDraw(pos);
        }

        @Test
        void draw_shouldDoGlowPassBeforeCore() {
            List<Vector2> pos = List.of(new Vector2(0, 0), new Vector2(3, 2));
            when(emitter.getPositions()).thenReturn(pos);

            render.draw(batch);

            InOrder inOrder = inOrder(batch);
            inOrder.verify(batch).flush();
            inOrder.verify(batch).setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

            for (int s = 4; s >= 1; s--) {
                float aGlow = 0.35f * (s / 4f);
                inOrder.verify(batch).setColor(emitterGlowColor.r, emitterGlowColor.g, emitterGlowColor.b, aGlow);
            }

            inOrder.verify(batch).flush();
            inOrder.verify(batch).setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }

    }
}

