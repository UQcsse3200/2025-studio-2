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

import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
class TiledWallComponentTest {

  @Mock
  private SpriteBatch spriteBatch;
  @Mock
  private Texture mockTexture;

  private Entity entity;
  private TextureRegion topTile, middleTile;
  private TiledWallComponent component;

  private static final int TOP_TILE_HEIGHT = 8;
  private static final int TILE_WIDTH = 32;
  private static final int MIDDLE_TILE_HEIGHT = 32;

  @BeforeEach
  void setUp() {
    topTile = spy(new TextureRegion(mockTexture, 0, 0, TILE_WIDTH, TOP_TILE_HEIGHT));
    middleTile = spy(new TextureRegion(mockTexture, 0, TOP_TILE_HEIGHT, TILE_WIDTH, MIDDLE_TILE_HEIGHT));

    entity = new Entity();
    entity.setPosition(0.0f, 0.0f);

    component = new TiledWallComponent(topTile, middleTile);
    component.setEntity(entity);
  }

  @Test
  void shouldDrawNothingForZeroHeightWall() {
    entity.setScale(1.0f, 0.0f);
    component.draw(spriteBatch);
    verifyNoInteractions(spriteBatch);
  }

  @Test
  void shouldDrawNothingIfShorterThanTopTile() {
    float wallWidth = 1.0f;
    float topTileWorldHeight = wallWidth / ((float) TILE_WIDTH / TOP_TILE_HEIGHT);
    entity.setScale(wallWidth, topTileWorldHeight * 0.9f);
    component.draw(spriteBatch);
    verifyNoInteractions(spriteBatch);
  }

  @Test
  void shouldNotDrawTopTileWhenHeightIsExact() {
    float wallWidth = 1.0f;
    float topTileWorldHeight = wallWidth / ((float) TILE_WIDTH / TOP_TILE_HEIGHT);
    entity.setScale(wallWidth, topTileWorldHeight);
    component.draw(spriteBatch);
    verifyNoMoreInteractions(spriteBatch);
  }

  @Test
  void shouldDrawStretchedMiddleTilesForNonIntegerHeight() {
    float wallWidth = 1.0f;
    float middleTileAspectRatio = (float) TILE_WIDTH / MIDDLE_TILE_HEIGHT;
    float topTileAspectRatio = (float) TILE_WIDTH / TOP_TILE_HEIGHT;

    float middleTileWorldHeight = wallWidth / middleTileAspectRatio;
    float topTileWorldHeight = wallWidth / topTileAspectRatio;

    entity.setScale(wallWidth, topTileWorldHeight + middleTileWorldHeight * 2.5f);
    component.draw(spriteBatch);

    float remainingHeight = entity.getScale().y - topTileWorldHeight;
    float stretchedMiddleHeight = remainingHeight / 3.0f;

    InOrder inOrder = inOrder(spriteBatch);
    inOrder.verify(spriteBatch).draw(middleTile, 0.0f, 0.0f, wallWidth, stretchedMiddleHeight);
    inOrder.verify(spriteBatch).draw(middleTile, 0.0f, stretchedMiddleHeight, wallWidth, stretchedMiddleHeight);
    inOrder.verify(spriteBatch).draw(middleTile, 0.0f, stretchedMiddleHeight * 2, wallWidth, stretchedMiddleHeight);
    inOrder.verify(spriteBatch).draw(topTile, 0.0f, stretchedMiddleHeight * 3, wallWidth, topTileWorldHeight);
    verifyNoMoreInteractions(spriteBatch);
  }

  @Test
  void shouldDrawCorrectNumberOfMiddleTiles() {
    float wallWidth = 1.0f;
    float middleTileAspectRatio = (float) TILE_WIDTH / MIDDLE_TILE_HEIGHT;
    float topTileAspectRatio = (float) TILE_WIDTH / TOP_TILE_HEIGHT;
    float middleTileWorldHeight = wallWidth / middleTileAspectRatio;
    float topTileWorldHeight = wallWidth / topTileAspectRatio;

    entity.setScale(wallWidth, topTileWorldHeight + middleTileWorldHeight * 2);
    component.draw(spriteBatch);

    verify(spriteBatch, times(2)).draw(eq(middleTile), anyFloat(), anyFloat(), eq(wallWidth), eq(middleTileWorldHeight));
    verify(spriteBatch, times(1)).draw(eq(topTile), anyFloat(), anyFloat(), eq(wallWidth), eq(topTileWorldHeight));
  }
}