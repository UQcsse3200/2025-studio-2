package com.csse3200.game.components.minimap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

/**
 * A component to be added to entities that should be tracked on the minimap.
 */
public class MinimapComponent extends Component {
  private final String markerAsset;
  private Image marker;

  private final Vector2 MARKER_SCALE = new Vector2();
  private float scaleX = 1f;
  private float scaleY = 1f;
  private static final float BOUNDS_SCALAR = 0.01253571f; // dont ask...
  private static final GridPoint2 DEFAULT_BOUNDS = new GridPoint2(100, 100);
  private GridPoint2 tileBounds;

  private float lastScreenRatio = -1f;


  /**
   * @param markerAsset The path to the texture for this entity's marker.
   */
  public MinimapComponent(String markerAsset) {
    this.markerAsset = markerAsset;
  }

  private void rescaleMarker() {
    if (marker == null) return;

    Vector2 worldSize = entity.getScale().cpy();
    float pxPerWUX = ServiceLocator.getMinimapService().getPixelsPerWorldUnitX();
    float pxPerWUY = ServiceLocator.getMinimapService().getPixelsPerWorldUnitY();

    float targetPxW = worldSize.x * pxPerWUX * MARKER_SCALE.x;
    float targetPxH = worldSize.y * pxPerWUY * MARKER_SCALE.y;

    marker.setSize(targetPxW, targetPxH);
  }

  @Override
  public void create() {
    var screen = ServiceLocator.getMainGameScreen();
    tileBounds = screen == null ? DEFAULT_BOUNDS : screen.getGameArea().getMapBounds();
    float ratio = (float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
    lastScreenRatio = ratio;

    MARKER_SCALE.set(tileBounds.x * BOUNDS_SCALAR * scaleX * ratio, tileBounds.y * BOUNDS_SCALAR * scaleY * ratio);

    Texture asset = ServiceLocator.getResourceService().getAsset(markerAsset, Texture.class);
    if (asset == null) return;
    marker = new Image(asset);

    var svs = ServiceLocator.getMinimapService();
    if (svs == null) return;
    svs.trackEntity(entity, marker);

    rescaleMarker();
  }

  @Override
  public void update() {
    float ratio = (float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
    if (Math.abs(ratio - lastScreenRatio) > 1e-4) {
      MARKER_SCALE.set(tileBounds.x * BOUNDS_SCALAR * scaleX * ratio, tileBounds.y * BOUNDS_SCALAR * scaleY * ratio);
      rescaleMarker();
      lastScreenRatio = ratio;
    }
  }

  /**
   * Updates the marker on the minimap with a new drawable.
   *
   * @param marker The new drawable for the marker.
   */
  public void setMarker(Image marker) {
    ServiceLocator.getMinimapService().setMarker(entity, marker);
  }

  /**
   * Tints the marker on the minimap with a new color.
   *
   * @param color The new color for the marker.
   */
  public void tintMarker(Color color) {
    ServiceLocator.getMinimapService().setMarkerColor(entity, color);
  }

  public MinimapComponent setScaleX(float sx) {
    scaleX = sx;
    return this;
  }

  public MinimapComponent setScaleY(float sy) {
    scaleY = sy;
    return this;
  }

  public MinimapComponent setScale(float scale) {
    scaleX = scale;
    scaleY = scale;
    return this;
  }

  @Override
  public void dispose() {
      // Prevent crash if service has already been cleared
      if (ServiceLocator.getMinimapService() != null) {
          ServiceLocator.getMinimapService().stopTracking(entity);
      }
  }
}