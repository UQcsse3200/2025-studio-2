package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
class TiledPlatformComponentTest {

  private final int TILE_WIDTH = 16;
  private final int TILE_HEIGHT = 16;
  @Mock
  private SpriteBatch spriteBatch;
  @Mock
  private Texture mockTexture;
  private Entity entity;
  private TextureRegion leftEdge, middleTile, rightEdge;

  @BeforeEach
  void setUp() {
    leftEdge = spy(new TextureRegion(mockTexture, 0, 0, TILE_WIDTH, TILE_HEIGHT));
    middleTile = spy(new TextureRegion(mockTexture, TILE_WIDTH, 0, TILE_WIDTH, TILE_HEIGHT));
    rightEdge = spy(new TextureRegion(mockTexture, TILE_WIDTH * 2, 0, TILE_WIDTH, TILE_HEIGHT));

    entity = new Entity();
    entity.setPosition(0.0f, 0.0f);
  }

  @Test
  void shouldNotDrawAnythingForZeroWidthPlatform() {
    entity.setScale(0.0f, 1.0f);
    TiledPlatformComponent component = new TiledPlatformComponent(leftEdge, middleTile, rightEdge);
    component.setEntity(entity);

    component.draw(spriteBatch);

    verifyNoInteractions(spriteBatch);
  }

  @Test
  void shouldDrawHalvesForPlatformLessThanTwoTilesWide() {
    entity.setScale(1.5f, 1.0f);
    TiledPlatformComponent component = new TiledPlatformComponent(leftEdge, middleTile, rightEdge);
    component.setEntity(entity);

    component.draw(spriteBatch);

    verify(spriteBatch).draw(any(TextureRegion.class), eq(0.0f), eq(0.0f), eq(0.75f), eq(1.0f));
    verify(spriteBatch).draw(any(TextureRegion.class), eq(0.75f), eq(0.0f), eq(0.75f), eq(1.0f));
    verifyNoMoreInteractions(spriteBatch);
  }

  @Test
  void shouldDrawOnlyEdgesForTwoTileWidePlatform() {
    entity.setScale(2.0f, 1.0f);
    TiledPlatformComponent component = new TiledPlatformComponent(leftEdge, middleTile, rightEdge);
    component.setEntity(entity);

    component.draw(spriteBatch);

    InOrder inOrder = inOrder(spriteBatch);
    inOrder.verify(spriteBatch).draw(eq(leftEdge), eq(0.0f), eq(0.0f), eq(1.0f), eq(1.0f));
    inOrder.verify(spriteBatch).draw(eq(rightEdge), eq(1.0f), eq(0.0f), eq(1.0f), eq(1.0f));
    verifyNoMoreInteractions(spriteBatch);
  }

  @Test
  void shouldDrawStretchedTilesForNonIntegerWidthPlatform() {
    entity.setScale(3.5f, 1.0f);
    TiledPlatformComponent component = new TiledPlatformComponent(leftEdge, middleTile, rightEdge);
    component.setEntity(entity);

    component.draw(spriteBatch);

    float stretchedWidth = 3.5f / 4.0f;

    InOrder inOrder = inOrder(spriteBatch);
    inOrder.verify(spriteBatch).draw(eq(leftEdge), eq(0.0f), eq(0.0f), eq(stretchedWidth), eq(1.0f));
    inOrder.verify(spriteBatch).draw(eq(middleTile), eq(stretchedWidth), eq(0.0f), eq(stretchedWidth), eq(1.0f));
    inOrder.verify(spriteBatch).draw(eq(middleTile), eq(stretchedWidth * 2), eq(0.0f), eq(stretchedWidth), eq(1.0f));
    inOrder.verify(spriteBatch).draw(eq(rightEdge), eq(stretchedWidth * 3), eq(0.0f), eq(stretchedWidth), eq(1.0f));
    verifyNoMoreInteractions(spriteBatch);
  }

  @Test
  void shouldDrawCorrectNumberOfMiddleTiles() {
    entity.setScale(10.0f, 1.0f);
    TiledPlatformComponent component = new TiledPlatformComponent(leftEdge, middleTile, rightEdge);
    component.setEntity(entity);

    component.draw(spriteBatch);

    verify(spriteBatch, times(1)).draw(eq(leftEdge), anyFloat(), anyFloat(), anyFloat(), anyFloat());
    verify(spriteBatch, times(8)).draw(eq(middleTile), anyFloat(), anyFloat(), anyFloat(), anyFloat());
    verify(spriteBatch, times(1)).draw(eq(rightEdge), anyFloat(), anyFloat(), anyFloat(), anyFloat());
  }

  @Test
  void shouldHandleDifferentAspectRatio() {
    int wideWidth = 32;
    int wideHeight = 16;
    TextureRegion wideLeft = spy(new TextureRegion(mockTexture, 0, 0, wideWidth, wideHeight));
    TextureRegion wideMiddle = spy(new TextureRegion(mockTexture, wideWidth, 0, wideWidth, wideHeight));
    TextureRegion wideRight = spy(new TextureRegion(mockTexture, wideWidth * 2, 0, wideWidth, wideHeight));

    entity.setScale(5.0f, 1.0f);
    TiledPlatformComponent component = new TiledPlatformComponent(wideLeft, wideMiddle, wideRight);
    component.setEntity(entity);

    component.draw(spriteBatch);
    float stretchedWidth = 5.0f / 3.0f;

    InOrder inOrder = inOrder(spriteBatch);
    inOrder.verify(spriteBatch).draw(eq(wideLeft), eq(0.0f), eq(0.0f), eq(stretchedWidth), eq(1.0f));
    inOrder.verify(spriteBatch).draw(eq(wideMiddle), eq(stretchedWidth), eq(0.0f), eq(stretchedWidth), eq(1.0f));
    inOrder.verify(spriteBatch).draw(eq(wideRight), eq(stretchedWidth * 2), eq(0.0f), eq(stretchedWidth), eq(1.0f));
    verifyNoMoreInteractions(spriteBatch);
  }

  @Test
  void shouldThrowAssertionErrorForMismatchedTileDimensions() {
    TextureRegion mismatchedMiddle = spy(new TextureRegion(mockTexture, TILE_WIDTH, 0, TILE_WIDTH, TILE_HEIGHT - 1));

    assertThrows(AssertionError.class, () -> new TiledPlatformComponent(leftEdge, mismatchedMiddle, rightEdge));
  }
}