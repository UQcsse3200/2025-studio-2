package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
class PixelPerfectPlacerTest {

  @Mock
  private Texture mockTexture;
  @Mock
  private Actor mockActor1;
  @Mock
  private Actor mockActor2;

  private PixelPerfectPlacer placer;
  private final int TEXTURE_WIDTH = 800;
  private final int TEXTURE_HEIGHT = 600;

  @BeforeEach
  void setUp() {
    when(mockTexture.getWidth()).thenReturn(TEXTURE_WIDTH);
    when(mockTexture.getHeight()).thenReturn(TEXTURE_HEIGHT);
    placer = new PixelPerfectPlacer(mockTexture);
  }

  @Test
  void shouldInitializeCorrectlyWithValidTexture() {
    assertEquals(2, placer.getChildren().size, "Placer should have two children after creation.");
    assertInstanceOf(Image.class, placer.getChildren().get(0), "First child should be the background Image.");
    assertInstanceOf(Group.class, placer.getChildren().get(1), "Second child should be the overlay Group.");
  }

  @Test
  void shouldAddActorAndTransformCoordinatesCorrectly() throws NoSuchFieldException, IllegalAccessException {
    PixelPerfectPlacer.Rect rect = new PixelPerfectPlacer.Rect(100, 50, 200, 150);
    placer.addOverlay(mockActor1, rect);

    Field overlaysField = PixelPerfectPlacer.class.getDeclaredField("overlays");
    overlaysField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Array<Object> overlays = (Array<Object>) overlaysField.get(placer);

    assertEquals(1, overlays.size, "Overlay should be added to the internal array.");

    Object constraint = overlays.get(0);
    Field rectField = constraint.getClass().getDeclaredField("rect");
    rectField.setAccessible(true);
    PixelPerfectPlacer.Rect transformedRect = (PixelPerfectPlacer.Rect) rectField.get(constraint);

    int expectedY = TEXTURE_HEIGHT - (rect.y() + rect.height());
    assertEquals(100, transformedRect.x(), "X coordinate should be unchanged.");
    assertEquals(expectedY, transformedRect.y(), "Y coordinate should be transformed from top-left to bottom-left.");
    assertEquals(200, transformedRect.width(), "Width should be unchanged.");
    assertEquals(150, transformedRect.height(), "Height should be unchanged.");
  }

  @Test
  void shouldHandleNullActorOrRectGracefully() {
    PixelPerfectPlacer.Rect validRect = new PixelPerfectPlacer.Rect(0, 0, 10, 10);
    assertThrows(IllegalArgumentException.class, () -> placer.addOverlay(null, validRect));
    assertThrows(NullPointerException.class, () -> placer.addOverlay(mockActor1, null));
  }

  @Test
  void shouldSetBoundsCorrectlyWithUniformScaling() {
    PixelPerfectPlacer.Rect rect = new PixelPerfectPlacer.Rect(100, 50, 200, 100);
    placer.addOverlay(mockActor1, rect);

    float scale = 2.0f;
    placer.setSize(TEXTURE_WIDTH * scale, TEXTURE_HEIGHT * scale);
    placer.layout();

    // Transformed Y = 600 - (50 + 100) = 450
    // Expected: x=100*2, y=450*2, w=200*2, h=100*2
    verify(mockActor1).setBounds(200f, 900f, 400f, 200f);
  }

  @Test
  void shouldSetBoundsCorrectlyWithNonUniformScaling() {
    PixelPerfectPlacer.Rect rect = new PixelPerfectPlacer.Rect(50, 100, 100, 50);
    placer.addOverlay(mockActor1, rect);

    float scaleX = 2.0f;
    float scaleY = 0.5f;
    placer.setSize(TEXTURE_WIDTH * scaleX, TEXTURE_HEIGHT * scaleY);
    placer.layout();

    // Transformed Y = 600 - (100 + 50) = 450
    // Expected: x=50*2, y=450*0.5, w=100*2, h=50*0.5
    verify(mockActor1).setBounds(100f, 225f, 200f, 25f);
  }

  @Test
  void shouldHandleZeroScaleWithoutCrashing() {
    PixelPerfectPlacer.Rect rect = new PixelPerfectPlacer.Rect(100, 100, 100, 100);
    placer.addOverlay(mockActor1, rect);

    placer.setSize(0, 0);
    placer.layout();

    verify(mockActor1).setBounds(0f, 0f, 0f, 0f);
  }

  @Test
  void shouldUpdateLayoutForMultipleOverlays() {
    PixelPerfectPlacer.Rect rect1 = new PixelPerfectPlacer.Rect(10, 20, 30, 40);
    PixelPerfectPlacer.Rect rect2 = new PixelPerfectPlacer.Rect(200, 300, 50, 60);
    placer.addOverlay(mockActor1, rect1);
    placer.addOverlay(mockActor2, rect2);

    float scale = 1.5f;
    placer.setSize(TEXTURE_WIDTH * scale, TEXTURE_HEIGHT * scale);
    placer.layout();

    // Transformed Y = 600 - (20 + 40) = 540
    // Expected: x=10*1.5, y=540*1.5, w=30*1.5, h=40*1.5
    verify(mockActor1).setBounds(15f, 810f, 45f, 60f);

    // Transformed Y = 600 - (300 + 60) = 240
    // Expected: x=200*1.5, y=240*1.5, w=50*1.5, h=60*1.5
    verify(mockActor2).setBounds(300f, 360f, 75f, 90f);
  }
}