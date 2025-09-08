package com.csse3200.game.components.pausemenu;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.MinimapService;
import com.csse3200.game.services.ServiceLocator;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/**
 * An actor that displays the large map, designed to be placed within a container.
 * It dynamically updates entity markers by overriding the act() method.
 */
public class MapDisplay extends Table {
  private final MinimapService minimapService;
  private final Map<Entity, Image> trackedEntities = new HashMap<>();
  private final Vector2 worldSize;
  private final Vector2 origin;

  public MapDisplay() {
    this.minimapService = ServiceLocator.getMinimapService();
    if (minimapService == null) {
      throw new IllegalStateException("MinimapService must be registered before creating a MapDisplay.");
    }

    Image mapImage = new Image(minimapService.getMinimapTexture());
    this.worldSize = minimapService.getWorldSize();
    this.origin = minimapService.getOrigin();

    Stack stack = new Stack();
    stack.add(mapImage);
    stack.add(ServiceLocator.getMinimapService().getMapMarkerGroup());

    this.add(stack).expand().fill();
  }

  /**
   * This method is called by the stage every frame, allowing the map to update its markers.
   * @param delta Time since last frame.
   */
  @Override
  public void act(float delta) {
    if (!this.isVisible()) return;
    super.act(delta);

    Vector2 displaySize = new Vector2(this.getWidth(), this.getHeight());
    if (displaySize.x == 0 || displaySize.y == 0) {
      return;
    }

    Vector2 remapFactor = new Vector2(displaySize.x / worldSize.x, displaySize.y / worldSize.y);

    for (Map.Entry<Entity, Image> entry : minimapService.getTrackedEntities().entrySet()) {
      Entity entity = entry.getKey();
      Image marker = entry.getValue();

      Vector2 markerPos = worldToMapCoordinates(entity.getPosition(), origin, remapFactor);
      marker.setPosition(markerPos.x - marker.getWidth() / 2f, markerPos.y - marker.getHeight() / 2f);
    }
  }

  private Vector2 worldToMapCoordinates(Vector2 worldPos, Vector2 origin, Vector2 remapFactor) {
    float mapX = (worldPos.x - origin.x) * remapFactor.x;
    float mapY = (worldPos.y - origin.y) * remapFactor.y;
    return new Vector2(mapX, mapY);
  }
}