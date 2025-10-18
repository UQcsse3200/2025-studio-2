package com.csse3200.game.entities.factories;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.ActionIndicatorComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.Keymap;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class ActionIndicatorFactoryTest {

  private Texture jumpTexture;
  private Texture doubleJumpTexture;

  @BeforeEach
  void setUp() {
    jumpTexture = mock(Texture.class);
    doubleJumpTexture = mock(Texture.class);

    ResourceService mockResourceService = mock(ResourceService.class);
    when(mockResourceService.getAsset("images/tutorials/jump.png", Texture.class)).thenReturn(jumpTexture);
    when(mockResourceService.getAsset("images/tutorials/double_jump.png", Texture.class)).thenReturn(doubleJumpTexture);
    ServiceLocator.registerResourceService(mockResourceService);
  }

  @Test
  void shouldCreateJumpTutorial() throws Exception {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {
      keymap.when(() -> Keymap.getActionKeyCode("PlayerJump")).thenReturn(Input.Keys.SPACE);

      Entity tutorial = ActionIndicatorFactory.createJumpTutorial();
      assertNotNull(tutorial);

      ActionIndicatorComponent component = tutorial.getComponent(ActionIndicatorComponent.class);
      assertNotNull(component);

      assertEquals(jumpTexture, getPrivateField(component, "texture"));
      assertEquals("Space", getPrivateField(component, "keyText"));
      assertEquals(new Vector2(0.5f, 0.5f), tutorial.getScale());
    }
  }

  @Test
  void shouldCreateDoubleJumpTutorial() throws Exception {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {
      keymap.when(() -> Keymap.getActionKeyCode("PlayerJump")).thenReturn(Input.Keys.SPACE);

      Entity tutorial = ActionIndicatorFactory.createDoubleJumpTutorial();
      assertNotNull(tutorial);

      ActionIndicatorComponent component = tutorial.getComponent(ActionIndicatorComponent.class);
      assertNotNull(component);

      assertEquals(doubleJumpTexture, getPrivateField(component, "texture"));
      assertEquals("Space", getPrivateField(component, "keyText"));
    }
  }

  @Test
  void constructorShouldBePrivateAndThrow() {
    Exception exception = assertThrows(InvocationTargetException.class, () -> {
      Constructor<ActionIndicatorFactory> constructor = ActionIndicatorFactory.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
    });
    assertInstanceOf(IllegalStateException.class, exception.getCause());
  }

  @SuppressWarnings("unchecked")
  private <T> T getPrivateField(Object obj, String fieldName) throws Exception {
    Class<?> currentClass = obj.getClass();
    Field field = null;
    while (currentClass != null) {
      try {
        field = currentClass.getDeclaredField(fieldName);
        break;
      } catch (NoSuchFieldException e) {
        currentClass = currentClass.getSuperclass();
      }
    }
    if (field == null) {
      throw new NoSuchFieldException("Field '" + fieldName + "' not found in class hierarchy.");
    }
    field.setAccessible(true);
    return (T) field.get(obj);
  }
}
