package com.csse3200.game.components.minimap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

/**
 * A component to be added to entities that should be tracked on the minimap.
 */
public class MinimapComponent extends Component {
  private final Image marker;

  /**
   * @param marker The image to use for the entity's marker.
   */
  public MinimapComponent(Image marker) {
    this.marker = marker;
  }

  /**
   * @param markerAsset The path to the texture for this entity's marker.
   */
  public MinimapComponent(String markerAsset) {
    this(loadImageWithDefaultSize(markerAsset));
  }

  private static Image loadImageWithDefaultSize(String markerAsset) {
    Image marker = new Image(ServiceLocator.getResourceService().getAsset(markerAsset, Texture.class));
      switch (markerAsset) {
          case "images/door_open.png" -> marker.setSize(20f, 30f);
          case "images/platform-map.png" -> marker.setSize(30f, 10f);
          case "images/flying_bat_map.png" -> marker.setSize(20f, 20f);
          case "images/drone-map.png" -> marker.setSize(20f, 10f);
          case "images/ladder-map.png" -> marker.setSize(13f, 13f);
          case "images/floor-map-1.png" -> marker.setSize(400f, 315f);
          case "images/floor-map-2.png" -> marker.setSize(680f, 315f);
          case "images/floor-map-3.png" -> marker.setSize(420f, 315f);
          case "images/gate-floor-map.png" -> marker.setSize(100f, 20f);
          case "images/puzzle-floor-map.png" -> marker.setSize(400f, 50f);
          default -> marker.setSize(15f, 20f); // Default marker size
      }

    return marker;
  }

  @Override
  public void create() {
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

  @Override
  public void dispose() {
    ServiceLocator.getMinimapService().stopTracking(entity);
  }
}