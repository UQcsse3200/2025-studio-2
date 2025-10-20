package com.csse3200.game.services;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.csse3200.game.components.minimap.MinimapDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class MinimapServiceTest {
  private Texture mockTexture;
  private MinimapDisplay mockMinimapDisplay;
  private MinimapService minimapService;

  @BeforeEach
  void setUp() {
    mockMinimapDisplay = mock(MinimapDisplay.class);
    mockTexture = mock(Texture.class);

    minimapService = new MinimapService(mockTexture, new Vector2(100, 100), Vector2.Zero);
    minimapService.setDisplay(mockMinimapDisplay);
  }

  @Test
  void shouldUpdateMarker() {
    Entity entity = new Entity();
    Image oldMarker = new Image(mockTexture);
    Image newMarker = new Image(mockTexture);

    minimapService.trackEntity(entity, oldMarker);
    minimapService.setMarker(entity, newMarker);

    assertEquals(newMarker, minimapService.getTrackedEntities().get(entity));
    verify(mockMinimapDisplay).removeMarker(oldMarker);
    verify(mockMinimapDisplay).addMarker(newMarker);
  }

  @Test
  void shouldNotTrackSameEntityTwice() {
    Entity entity = new Entity();
    Image marker1 = new Image(mockTexture);
    Image marker2 = new Image(mockTexture);

    minimapService.trackEntity(entity, marker1);
    minimapService.trackEntity(entity, marker2);

    assertEquals(1, minimapService.getTrackedEntities().size());
    assertEquals(marker1, minimapService.getTrackedEntities().get(entity));
    verify(mockMinimapDisplay, times(1)).addMarker(any(Image.class));
  }

  @Test
  void shouldTrackAndUntrackEntity() {
    Entity entity = new Entity();
    Image marker = new Image(mockTexture);

    minimapService.trackEntity(entity, marker);

    assertTrue(minimapService.getTrackedEntities().containsKey(entity));
    assertEquals(marker, minimapService.getTrackedEntities().get(entity));
    verify(mockMinimapDisplay).addMarker(marker);

    minimapService.stopTracking(entity);

    assertFalse(minimapService.getTrackedEntities().containsKey(entity));
    verify(mockMinimapDisplay).removeMarker(marker);
  }

  @Test
  void shouldSetMarkerColor() {
    Entity entity = new Entity();
    Image marker = spy(new Image(mockTexture));

    minimapService.trackEntity(entity, marker);
    minimapService.setMarkerColor(entity, Color.RED);

    verify(marker).setColor(Color.RED);
  }

  @Test
  void shouldHandleUntrackedEntityGracefully() {
    Entity entity = new Entity();
    Image marker = new Image(mockTexture);

    assertDoesNotThrow(() -> minimapService.stopTracking(entity));
    assertDoesNotThrow(() -> minimapService.setMarker(entity, marker));
    assertDoesNotThrow(() -> minimapService.setMarkerColor(entity, Color.BLUE));

    verify(mockMinimapDisplay, never()).removeMarker(any(Image.class));
  }

  @Test
  void shouldClearEntitiesOnDispose() {
    Entity entity1 = new Entity();
    Entity entity2 = new Entity();
    minimapService.trackEntity(entity1, new Image(mockTexture));
    minimapService.trackEntity(entity2, new Image(mockTexture));

    assertEquals(2, minimapService.getTrackedEntities().size());

    minimapService.dispose();

    assertTrue(minimapService.getTrackedEntities().isEmpty());
  }
}
