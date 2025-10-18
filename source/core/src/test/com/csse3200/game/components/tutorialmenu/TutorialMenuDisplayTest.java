package com.csse3200.game.components.tutorialmenu;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.Keymap;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class TutorialMenuDisplayTest {

  private TutorialMenuDisplay tutorialMenuDisplay;
  private Stage mockStage;
  private RenderService mockRenderService;
  private GameTime mockGameTime;
  private ResourceService mockResourceService;

  @BeforeEach
  void setUp() {
    GdxGame mockGame = mock(GdxGame.class);
    Entity mockEntity = mock(Entity.class);
    mockStage = mock(Stage.class);
    mockRenderService = mock(RenderService.class);
    mockResourceService = mock(ResourceService.class);
    mockGameTime = mock(GameTime.class);

    // Register services in ServiceLocator
    ServiceLocator.registerRenderService(mockRenderService);
    ServiceLocator.registerResourceService(mockResourceService);
    ServiceLocator.registerTimeSource(mockGameTime);

    // Mock the render service to return our mock stage
    when(mockRenderService.getStage()).thenReturn(mockStage);
    when(mockGameTime.getDeltaTime()).thenReturn(0.016f); // 60 FPS

    // Mock texture resources
    when(mockResourceService.getAsset(anyString(), eq(Texture.class)))
        .thenReturn(mock(Texture.class));
    
    // Mock TextureAtlas with animation frames
    TextureAtlas mockAtlas = mock(TextureAtlas.class);
    Array<TextureAtlas.AtlasRegion> mockFrames = new Array<>();
    
    // Create mock atlas regions for animations
    TextureAtlas.AtlasRegion mockRegion = mock(TextureAtlas.AtlasRegion.class);
    mockFrames.add(mockRegion);
    
    // Mock the findRegions method to return our mock frames
    when(mockAtlas.findRegions("LEFT")).thenReturn(mockFrames);
    when(mockAtlas.findRegions("RIGHT")).thenReturn(mockFrames);
    when(mockAtlas.findRegions("CROUCH")).thenReturn(mockFrames);
    when(mockAtlas.findRegions("JUMP")).thenReturn(mockFrames);
    
    when(mockResourceService.getAsset(anyString(), eq(TextureAtlas.class)))
        .thenReturn(mockAtlas);

    tutorialMenuDisplay = new TutorialMenuDisplay(mockGame);
    tutorialMenuDisplay.setEntity(mockEntity);
  }

  @AfterEach
  void tearDown() {
    ServiceLocator.clear();
  }

  @Test
  void shouldCreateTutorialMenu() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {
      // Mock keymap actions
      keymap.when(() -> Keymap.getActionKeyCode(anyString())).thenReturn(65); // 'A' key

      assertDoesNotThrow(() -> tutorialMenuDisplay.create());

      verify(mockRenderService).register(tutorialMenuDisplay);
    }
  }

  @Test
  void shouldHandleBasicsSection() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {
      keymap.when(() -> Keymap.getActionKeyCode(anyString())).thenReturn(65);

      assertDoesNotThrow(() -> tutorialMenuDisplay.create());

      // Verify that the tutorial menu was created and registered
      verify(mockRenderService).register(tutorialMenuDisplay);
    }
  }

  @Test
  void shouldDisposeCorrectly() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {
      keymap.when(() -> Keymap.getActionKeyCode(anyString())).thenReturn(65);

      tutorialMenuDisplay.create();
      assertDoesNotThrow(() -> tutorialMenuDisplay.dispose());

      verify(mockRenderService).unregister(tutorialMenuDisplay);
    }
  }

  @Test
  void shouldUpdateCorrectly() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {
      keymap.when(() -> Keymap.getActionKeyCode(anyString())).thenReturn(65);

      tutorialMenuDisplay.create();
      assertDoesNotThrow(() -> tutorialMenuDisplay.update());
    }
  }

  @Test
  void shouldHandlePlayerKeybinds() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {
      // Mock different keybinds for different actions
      keymap.when(() -> Keymap.getActionKeyCode("PlayerLeft")).thenReturn(65);
      keymap.when(() -> Keymap.getActionKeyCode("PlayerRight")).thenReturn(68);
      keymap.when(() -> Keymap.getActionKeyCode("PlayerJump")).thenReturn(32);
      keymap.when(() -> Keymap.getActionKeyCode("PlayerCrouch")).thenReturn(67);

      assertDoesNotThrow(() -> tutorialMenuDisplay.create());
      verify(mockRenderService).register(tutorialMenuDisplay);
    }
  }
}
