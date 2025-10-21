package com.csse3200.game.components.minimap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.csse3200.game.components.Component;
<<<<<<< HEAD
import com.csse3200.game.components.player.PlayerActions;
=======
import com.csse3200.game.physics.components.PhysicsComponent;
>>>>>>> 0e90f4a37849cb191cda74c553361486dee8f6ec
import com.csse3200.game.services.ServiceLocator;

/**
 * A component to be added to entities that should be tracked on the minimap.
 */
public class MinimapComponent extends Component {
  private final String markerAsset;
  private Image marker;

  private Vector2 MARKER_SCALE = new Vector2(1.56f, 1.56f);


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

    if (entity.getComponent(PlayerActions.class) != null) {
      targetPxW = worldSize.x * pxPerWUX * MARKER_SCALE / 3;
      targetPxH = worldSize.y * pxPerWUY * MARKER_SCALE / 2;
    }

    marker.setSize(targetPxW, targetPxH);
  }

  @Override
  public void create() {
    marker = new Image(ServiceLocator.getResourceService().getAsset(markerAsset, Texture.class));
    rescaleMarker();
    ServiceLocator.getMinimapService().trackEntity(entity, marker);
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
    MARKER_SCALE.x = sx;
    return this;
  }

  public MinimapComponent setScaleY(float sy) {
    MARKER_SCALE.y = sy;
    return this;
  }

  public MinimapComponent setScale(float scale) {
    MARKER_SCALE.set(scale, scale);
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