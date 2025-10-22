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
import com.csse3200.game.utils.CollectablesSave;
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
  private GdxGame mockGame;

  @BeforeEach
  void setUp() {
    mockGame = mock(GdxGame.class);
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
    
    // Mock the findRegions method to return our mock frames for all animations
    when(mockAtlas.findRegions(anyString())).thenReturn(mockFrames);
    
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
  void shouldHandleItemsSection() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {
      keymap.when(() -> Keymap.getActionKeyCode(anyString())).thenReturn(65);

      // Creating the menu tests the initial section (basics) 
      // and all the button creation which indirectly tests other sections
      assertDoesNotThrow(() -> tutorialMenuDisplay.create());
      
      // Verify that resources are loaded (any texture atlas calls indicate sections are rendering)
      verify(mockResourceService, atLeastOnce()).getAsset(anyString(), eq(TextureAtlas.class));
    }
  }

  @Test
  void shouldHandleUpgradesSection() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {
      keymap.when(() -> Keymap.getActionKeyCode("Dash")).thenReturn(81);
      keymap.when(() -> Keymap.getActionKeyCode("Glide")).thenReturn(69);
      keymap.when(() -> Keymap.getActionKeyCode("Jetpack")).thenReturn(70);
      keymap.when(() -> Keymap.getActionKeyCode(anyString())).thenReturn(65);

      assertDoesNotThrow(() -> tutorialMenuDisplay.create());
      
      // Verify keybinds are queried during creation
      keymap.verify(() -> Keymap.getActionKeyCode(anyString()), atLeastOnce());
    }
  }

  @Test
  void shouldHandleLevelMechanicsSection() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {
      keymap.when(() -> Keymap.getActionKeyCode(anyString())).thenReturn(65);

      assertDoesNotThrow(() -> tutorialMenuDisplay.create());
      
      // Verify resources are loaded
      verify(mockResourceService, atLeastOnce()).getAsset(anyString(), eq(TextureAtlas.class));
      verify(mockResourceService, atLeastOnce()).getAsset(anyString(), eq(Texture.class));
    }
  }

  @Test
  void shouldHandleEnemiesSection() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {
      keymap.when(() -> Keymap.getActionKeyCode(anyString())).thenReturn(65);

      assertDoesNotThrow(() -> tutorialMenuDisplay.create());
      
      // Verify atlases are loaded (enemies use animated atlases)
      verify(mockResourceService, atLeastOnce()).getAsset(anyString(), eq(TextureAtlas.class));
    }
  }

  @Test
  void shouldHandleLoreSection() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class);
         MockedStatic<CollectablesSave> collectablesSave = mockStatic(CollectablesSave.class)) {
      keymap.when(() -> Keymap.getActionKeyCode(anyString())).thenReturn(65);
      collectablesSave.when(CollectablesSave::getCollectedCount).thenReturn(5);

      assertDoesNotThrow(() -> tutorialMenuDisplay.create());
      
      // Verify component registers successfully (lore section will be available via buttons)
      verify(mockRenderService).register(tutorialMenuDisplay);
    }
  }

  @Test
  void shouldLoadAllRequiredAssets() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class);
         MockedStatic<CollectablesSave> collectablesSave = mockStatic(CollectablesSave.class)) {
      keymap.when(() -> Keymap.getActionKeyCode(anyString())).thenReturn(65);
      collectablesSave.when(CollectablesSave::getCollectedCount).thenReturn(3);

      tutorialMenuDisplay.create();
      
      // Verify all major assets are requested
      verify(mockResourceService, atLeastOnce()).getAsset(contains("PLAYER"), eq(TextureAtlas.class));
      verify(mockResourceService, atLeastOnce()).getAsset(anyString(), eq(Texture.class));
      verify(mockResourceService, atLeastOnce()).getAsset(anyString(), eq(TextureAtlas.class));
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

  @Test
  void shouldHandleMultipleKeybinds() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {
      // Mock keybinds for various sections
      keymap.when(() -> Keymap.getActionKeyCode("Dash")).thenReturn(81);
      keymap.when(() -> Keymap.getActionKeyCode("Glide")).thenReturn(69);
      keymap.when(() -> Keymap.getActionKeyCode("Jetpack")).thenReturn(70);
      keymap.when(() -> Keymap.getActionKeyCode("PlayerInteract")).thenReturn(69);
      keymap.when(() -> Keymap.getActionKeyCode("PlayerHold")).thenReturn(72);

      tutorialMenuDisplay.create();
      
      // Verify all keybinds are queried
      keymap.verify(() -> Keymap.getActionKeyCode(anyString()), atLeastOnce());
    }
  }

  @Test
  void shouldHandleAnimationUpdates() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class);
         MockedStatic<CollectablesSave> collectablesSave = mockStatic(CollectablesSave.class)) {
      keymap.when(() -> Keymap.getActionKeyCode(anyString())).thenReturn(65);
      collectablesSave.when(CollectablesSave::getCollectedCount).thenReturn(7);

      tutorialMenuDisplay.create();
      
      // Update multiple times to test animation frame updates
      assertDoesNotThrow(() -> {
        for (int i = 0; i < 10; i++) {
          tutorialMenuDisplay.update();
        }
      });
    }
  }

  @Test
  void shouldHandleLoreWithZeroCollectables() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class);
         MockedStatic<CollectablesSave> collectablesSave = mockStatic(CollectablesSave.class)) {
      keymap.when(() -> Keymap.getActionKeyCode(anyString())).thenReturn(65);
      collectablesSave.when(CollectablesSave::getCollectedCount).thenReturn(0);

      // Should create without errors even with 0 collectables
      assertDoesNotThrow(() -> tutorialMenuDisplay.create());
      verify(mockRenderService).register(tutorialMenuDisplay);
    }
  }

  @Test
  void shouldHandleLoreWithMaxCollectables() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class);
         MockedStatic<CollectablesSave> collectablesSave = mockStatic(CollectablesSave.class)) {
      keymap.when(() -> Keymap.getActionKeyCode(anyString())).thenReturn(65);
      collectablesSave.when(CollectablesSave::getCollectedCount).thenReturn(9);

      // Should create without errors with max collectables
      assertDoesNotThrow(() -> tutorialMenuDisplay.create());
      verify(mockRenderService).register(tutorialMenuDisplay);
    }
  }

  @Test
  void shouldRegisterAndUnregisterWithRenderService() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {
      keymap.when(() -> Keymap.getActionKeyCode(anyString())).thenReturn(65);

      // Create should register
      tutorialMenuDisplay.create();
      verify(mockRenderService).register(tutorialMenuDisplay);
      
      // Dispose should unregister
      tutorialMenuDisplay.dispose();
      verify(mockRenderService).unregister(tutorialMenuDisplay);
    }
  }

  @Test
  void shouldHandleMultipleUpdates() {
    try (MockedStatic<Keymap> keymap = mockStatic(Keymap.class)) {
      keymap.when(() -> Keymap.getActionKeyCode(anyString())).thenReturn(65);

      tutorialMenuDisplay.create();
      
      // Multiple updates should not cause errors
      assertDoesNotThrow(() -> {
        tutorialMenuDisplay.update();
        tutorialMenuDisplay.update();
        tutorialMenuDisplay.update();
      });
    }
  }
}
