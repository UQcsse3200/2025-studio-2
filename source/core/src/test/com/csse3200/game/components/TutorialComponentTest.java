package com.csse3200.game.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
class TutorialComponentTest {

  @Mock
  private SpriteBatch batch;
  @Mock
  private Texture texture;
  @Mock
  private BitmapFont font;
  @Mock
  private BitmapFont.BitmapFontData fontData = new BitmapFont.BitmapFontData();

  @BeforeEach
  void setUp() {
    ServiceLocator.registerRenderService(new RenderService());
    ResourceService mockResourceService = mock(ResourceService.class);
    ServiceLocator.registerResourceService(mockResourceService);

    when(mockResourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);
  }

  @Test
  void createShouldLoadAndConfigureFont() {
    try (MockedConstruction<Skin> mockedSkin = mockConstruction(Skin.class, (mock, context) -> {
      when(mock.getFont(anyString())).thenReturn(font);
    })) {
      TutorialComponent component = new TutorialComponent("test.png", "A");
      new Entity().addComponent(component).create();

      assertEquals(1, mockedSkin.constructed().size());
      Skin constructedSkin = mockedSkin.constructed().getFirst();

      verify(constructedSkin).getFont("commodore-64");
      verify(font).setUseIntegerPositions(false);
    }
  }

  @Test
  void drawShouldRenderImageAndText() {
    when(font.getData()).thenReturn(fontData);
    TutorialComponent component = new TutorialComponent("test.png", "A");
    Entity entity = new Entity().addComponent(component);
    entity.setPosition(10f, 20f);
    entity.setScale(2f, 1f);

    setPrivateField(component, "font", font);

    component.draw(batch);

    verify(batch).draw(texture, 10f, 20f, 2f, 1f);
    verify(fontData).setScale(2f / 40f);
    float expectedTextX = 10f - 2f / 2; // for pos.x - scale.x / 2
    float expectedTextY = 20f - 0.2f;   // for pos.y - 0.2f
    float expectedTargetWidth = 2f * 2; // for scale.x * 2
    verify(font).draw(batch, "A", expectedTextX, expectedTextY, expectedTargetWidth, Align.center, false);
  }

  @Test
  void drawShouldNotRenderTextIfFontIsNull() {
    TutorialComponent component = new TutorialComponent("test.png", "A");
    new Entity().addComponent(component);

    component.draw(batch);

    verify(batch).draw(any(Texture.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
    verify(font, never()).getData();
    verify(font, never()).draw(any(SpriteBatch.class), anyString(), anyFloat(), anyFloat(), anyFloat(), anyInt(), anyBoolean());
  }

  /*
  @Test
  void disposeShouldDisposeSkinResource() {
    Skin mockSkin = mock(Skin.class);
    TutorialComponent component = new TutorialComponent("test.png", "A");
    setPrivateField(component, "skin", mockSkin);

    component.dispose();
    verify(mockSkin).dispose();
  }

   */

  private void setPrivateField(Object object, String fieldName, Object value) {
    try {
      java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(object, value);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set private field '" + fieldName + "'", e);
    }
  }
}
