package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * A render component for dynamically tiling a wall texture vertically.
 */
public class TiledWallComponent extends RenderComponent {
  private final TextureRegion topTile;
  private final TextureRegion middleTile;
  private final float tileAspectRatio;
  private final float topTileAspectRatio;

  /**
   * Creates a new TiledWallComponent.
   *
   * @param topTile    The texture for the top of the wall.
   * @param middleTile The texture for repeating middle sections of the wall.
   */
  public TiledWallComponent(TextureRegion topTile, TextureRegion middleTile) {
    this.topTile = topTile;
    this.middleTile = middleTile;

    tileAspectRatio = (float) middleTile.getRegionWidth() / middleTile.getRegionHeight();
    topTileAspectRatio = (float) topTile.getRegionWidth() / topTile.getRegionHeight();
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 position = entity.getPosition();
    Vector2 scale = entity.getScale();
    float topTileWorldHeight = scale.x / topTileAspectRatio;
    float tileWorldHeight = scale.x / tileAspectRatio;
    scale.y -= topTileWorldHeight;
    int totalTilesToDraw = MathUtils.round(scale.y / tileWorldHeight);

    if (0 >= totalTilesToDraw || 0 >= scale.x || 0 >= scale.y) return;

    float stretchedTileHeight = scale.y / totalTilesToDraw;
    for (int i = 0; i < totalTilesToDraw; i++) {
      float currentY = position.y + i * stretchedTileHeight;
      batch.draw(middleTile, position.x, currentY, scale.x, stretchedTileHeight);
    }

    float topY = position.y + totalTilesToDraw * stretchedTileHeight;
    batch.draw(topTile, position.x, topY, scale.x, topTileWorldHeight);
  }
}
