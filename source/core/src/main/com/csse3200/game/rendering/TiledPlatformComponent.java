package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * A render component for dynamically tiling a platform texture based on the entity's size.
 * It handles edge tiles, repeating middle tiles, and stretching for a perfect fit.
 */
public class TiledPlatformComponent extends RenderComponent {
  private final TextureRegion leftEdge;
  private final TextureRegion middleTile;
  private final TextureRegion rightEdge;
  private final float tileAspectRatio;

  // Pre-calculated regions for the merged tile.
  private final TextureRegion leftEdgeHalf;
  private final TextureRegion rightEdgeHalf;

  /**
   * Creates a new TiledPlatformComponent.
   *
   * @param leftEdge The texture for the left edge.
   * @param middleTile The middle region texture.
   * @param rightEdge The texture for the right edge.
   */
  public TiledPlatformComponent(TextureRegion leftEdge, TextureRegion middleTile, TextureRegion rightEdge) {
    this.leftEdge = leftEdge;
    this.middleTile = middleTile;
    this.rightEdge = rightEdge;

    int w = leftEdge.getRegionWidth();
    int h = leftEdge.getRegionHeight();
    tileAspectRatio = ((float) w) / h;

    assert(w == middleTile.getRegionWidth() && h == middleTile.getRegionHeight());
    assert(w == rightEdge.getRegionWidth() && h == rightEdge.getRegionHeight());

    leftEdgeHalf = new TextureRegion(leftEdge, 0, 0, w / 2, h);
    rightEdgeHalf = new TextureRegion(rightEdge, w / 2, 0, w / 2, h);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 position = entity.getPosition();
    Vector2 scale = entity.getScale();
    float tileHeightWorld = scale.y;
    float tileWidthWorld = tileHeightWorld * tileAspectRatio;
    int totalTilesToDraw = MathUtils.ceil(scale.x / tileWidthWorld);

    if (totalTilesToDraw == 0) return;

    // Platform is narrower than two tiles.
    if (totalTilesToDraw < 2) {
      batch.draw(leftEdgeHalf, position.x, position.y, scale.x / 2f, tileHeightWorld);
      batch.draw(rightEdgeHalf, position.x + scale.x / 2f, position.y, scale.x / 2f, tileHeightWorld);
      return;
    }

    // Normal tiling
    float stretchedTileWidth = scale.x / totalTilesToDraw;
    int numMiddleTiles = totalTilesToDraw - 2;

    float currentX = position.x;
    batch.draw(leftEdge, currentX, position.y, stretchedTileWidth, tileHeightWorld);
    currentX += stretchedTileWidth;
    for (int i = 0; i < numMiddleTiles; i++) {
      batch.draw(middleTile, currentX, position.y, stretchedTileWidth, tileHeightWorld);
      currentX += stretchedTileWidth;
    }
    batch.draw(rightEdge, currentX, position.y, stretchedTileWidth, tileHeightWorld);
  }
}
