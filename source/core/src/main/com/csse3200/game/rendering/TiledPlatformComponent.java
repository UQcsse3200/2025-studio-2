package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * A render component for dynamically tiling a platform texture based on the entity's size.
 * It handles edge tiles, repeating middle tiles with variation, and stretching for a perfect fit.
 */
public class TiledPlatformComponent extends RenderComponent {
  private final TextureRegion leftEdge;
  private final TextureRegion middleTile;
  private final TextureRegion rightEdge;
  private final float tileAspectRatio;

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
    this.tileAspectRatio = (float) leftEdge.getRegionWidth() / leftEdge.getRegionHeight();
  }

  @Override
  protected void draw(SpriteBatch batch) {
    final Vector2 position = entity.getPosition();
    final Vector2 scale = entity.getScale();
    final float tileHeightWorld = scale.y;
    final float tileWidthWorld = tileHeightWorld * tileAspectRatio;

    if (tileWidthWorld <= 0) return;

    // Platform is narrower than a single tile.
    if (scale.x < tileWidthWorld) {
      throw new IllegalStateException("Attempted to draw a tiled platform too narrow!");
    }

    // Platform is narrower than two tiles.
    if (scale.x < 2 * tileWidthWorld) {
      batch.draw(leftEdge, position.x, position.y, scale.x / 2f, tileHeightWorld);
      batch.draw(rightEdge, position.x + scale.x / 2f, position.y, scale.x / 2f, tileHeightWorld);
      return;
    }

    // Normal tiling
    batch.draw(leftEdge, position.x, position.y, tileWidthWorld, tileHeightWorld);

    final float middleSectionWidth = scale.x - 2 * tileWidthWorld;
    final int numMiddleTiles = MathUtils.ceil(middleSectionWidth / tileWidthWorld);
    final float stretchedMiddleTileWidth = middleSectionWidth / numMiddleTiles;
    float currentX = position.x + tileWidthWorld;

    for (int i = 0; i < numMiddleTiles; i++) {
      batch.draw(middleTile, currentX, position.y, stretchedMiddleTileWidth, tileHeightWorld);
      currentX += stretchedMiddleTileWidth;
    }

    batch.draw(rightEdge, position.x + scale.x - tileWidthWorld, position.y, tileWidthWorld, tileHeightWorld);
  }
}
