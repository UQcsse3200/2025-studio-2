package com.csse3200.game.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.components.minimap.MinimapDisplay;
import com.csse3200.game.entities.Entity;

/**
 * Service to hold the state of tracked entities for the minimap.
 */
public class MinimapService implements Disposable {
  private final Map<Entity, Image> trackedEntities = new HashMap<>();
  private final Texture minimapTexture;
  private final Vector2 worldSize;
  private final Vector2 origin;
  private MinimapDisplay minimapDisplay;

  /**
   * Creates a new minimap service.
   *
   * @param minimapTexture The texture to use for the minimap background.
   * @param worldSize The size of the game world the texture represents. (Top Right corner)
   * @param origin The position that origin of the texture will have in the world. (Bottom Left corner)
   */
  public MinimapService(Texture minimapTexture, Vector2 worldSize, Vector2 origin) {
    this.minimapTexture = minimapTexture;
    this.worldSize = worldSize;
    this.origin = origin;
  }

  /**
   * Gets the texture to use for the minimap background.
   *
   * @return The texture to use for the minimap background.
   */
  public Texture getMinimapTexture() {
    return minimapTexture;
  }

  /**
   * Gets the size of the game world the texture represents.
   *
   * @return The size of the game world the texture represents.
   */
  public Vector2 getWorldSize() {
    return worldSize;
  }

  /**
   * Gets the position that origin of the texture will have in the world.
   *
   * @return The position that origin of the texture will have in the world.
   */
  public Vector2 getOrigin() {
    return origin;
  }

  /**
   * Sets the minimap display (to which entities are added).
   */
  public void setDisplay(MinimapDisplay minimapDisplay) {
    this.minimapDisplay = minimapDisplay;
  }

  /**
   * Gets the minimap display.
   *
   * @return The minimap display (to which entities are added).
   */
  public MinimapDisplay getDisplay() {
    return minimapDisplay;
  }

  /**
   * Adds an entity to be tracked on the minimap.
   *
   * @param entity The entity to track.
   * @param marker The image to use for the entity's marker.
   */
  public void trackEntity(Entity entity, Image marker) {
    if (!trackedEntities.containsKey(entity)) {
      trackedEntities.put(entity, marker);
      minimapDisplay.addMarker(marker);
    }
  }

  /**
   * Stops tracking an entity on the minimap.
   *
   * @param entity The entity to stop tracking.
   */
  public void stopTracking(Entity entity) {
    Image marker = trackedEntities.remove(entity);
    if (marker != null) {
      minimapDisplay.removeMarker(marker);
    }
  }

  /**
   * Updates the marker on the minimap with a new drawable.
   *
   * @param entity The entity whose marker to change.
   * @param marker The new drawable for the marker.
   */
  public void setMarker(Entity entity, Image marker) {
    Image oldMarker = trackedEntities.replace(entity, marker);
    if (oldMarker != null) {
      minimapDisplay.removeMarker(oldMarker);
    }
    minimapDisplay.addMarker(marker);
  }

  /**
   * Tints the marker on the minimap with a new color.
   *
   * @param entity The entity whose marker to change.
   * @param color The new color for the marker.
   */
  public void setMarkerColor(Entity entity, Color color) {
    Image marker = trackedEntities.get(entity);
    if (marker != null) marker.setColor(color);
  }

  /**
   * Gets a read-only view of the currently tracked entities.
   *
   * @return A map of all entities being tracked and their associated markers.
   */
  public Map<Entity, Image> getTrackedEntities() {
    return Collections.unmodifiableMap(trackedEntities);
  }

  @Override
  public void dispose() {
    trackedEntities.clear();
  }
}

