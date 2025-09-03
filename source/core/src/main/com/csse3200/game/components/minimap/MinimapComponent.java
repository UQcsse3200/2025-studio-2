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
    marker.setSize(10f, 10f); // Default marker size
    return marker;
  }

  @Override
  public void create() {
    ServiceLocator.getMinimapService().trackEntity(entity, marker);
  }

  /**
   * Updates the marker on the minimap with a new drawable.
   *
   * @param drawable The new drawable for the marker.
   */
  public void setMarker(Drawable drawable) {
    ServiceLocator.getMinimapService().setMarker(entity, drawable);
  }

  /**
   * Updates the marker on the minimap with a new texture.
   *
   * @param markerAsset The path to the new texture for the marker.
   */
  public void setMarker(String markerAsset) {
    Drawable drawable = new TextureRegionDrawable(
        ServiceLocator.getResourceService().getAsset(markerAsset, Texture.class)
    );
    setMarker(drawable);
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