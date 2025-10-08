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
    if (markerAsset.equals("images/door_open.png")) {
      marker.setSize(20f, 30f);
    }
    else if(markerAsset.equals("images/flying_bat_map.png")) {
      marker.setSize(20f, 20f);
    } else {
      marker.setSize(10f, 10f); // Default marker size
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