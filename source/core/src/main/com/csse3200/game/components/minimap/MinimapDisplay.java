package com.csse3200.game.components.minimap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.ui.UIComponent;
import java.util.HashMap;
import java.util.Map;

/**
 * A UI component for displaying a minimap.
 */
public class MinimapDisplay extends UIComponent {
  private final Texture minimapTexture;
  private final Vector2 worldSizeRemapFactor;
  private final Vector2 origin;
  private final float displaySize;
  private final MinimapOptions options;
  private final Map<Entity, Image> trackedEntities = new HashMap<>();
  private Group markerGroup;
  private Table rootTable;

  /**
   * Dictate where the Minimap will be drawn
   */
  public enum MinimapPosition {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
  }

  /**
   * Used to specify the options for drawing the minimap.
   */
  public static class MinimapOptions {
    public MinimapPosition position;
  }

  /**
   * Creates a new minimap display.
   *
   * @param minimapTexture The texture to use for the minimap background.
   * @param worldSize The size of the game world the texture represents.
   * @param origin The position that origin of the texture will have in the world.
   * @param displaySize The size (width and height) of the minimap on the screen.
   * @param options Options for the minimap shape and position.
   */
  public MinimapDisplay(Texture minimapTexture, Vector2 origin, Vector2 worldSize, float displaySize, MinimapOptions options) {
    this.minimapTexture = minimapTexture;
    this.worldSizeRemapFactor = new Vector2(displaySize / worldSize.x, displaySize / worldSize.y);
    this.origin = origin;
    this.options = options;
    this.displaySize = displaySize;
  }

  @Override
  public void create() {
    super.create();
    addActors();

    // Linking to its actor for easy look-up later
    rootTable.setUserObject(this);
    rootTable.setName("minimap");
  }

  private void addActors() {
    rootTable = new Table();
    rootTable.setFillParent(true);

    switch (options.position) {
      case TOP_LEFT -> rootTable.top().left();
      case TOP_RIGHT -> rootTable.top().right();
      case BOTTOM_LEFT -> rootTable.bottom().left();
      case BOTTOM_RIGHT -> rootTable.bottom().right();
    }
    rootTable.pad(10f);

    Image minimapImage = new Image(minimapTexture);
    markerGroup = new Group();

    Stack contentStack = new Stack();
    contentStack.add(minimapImage);
    contentStack.add(markerGroup);

    Table minimapContainer = new Table();
    minimapContainer.add(contentStack).size(displaySize);

    Stack finalStack = new Stack();
    finalStack.add(minimapContainer);

    rootTable.add(finalStack).size(displaySize);
    stage.addActor(rootTable);
  }

  @Override
  public void update() {
    for (Map.Entry<Entity, Image> entry : trackedEntities.entrySet()) {
      Entity entity = entry.getKey();
      Image marker = entry.getValue();

      Vector2 minimapCoords = worldToMinimapCoordinates(entity.getPosition());
      marker.setPosition(
          minimapCoords.x - marker.getWidth() / 2f,
          minimapCoords.y - marker.getHeight() / 2f
      );
    }
  }

  private Vector2 worldToMinimapCoordinates(Vector2 worldPos) {
    float minimapX = (worldPos.x - this.origin.x) * worldSizeRemapFactor.x;
    float minimapY = (worldPos.y - this.origin.y) * worldSizeRemapFactor.y;
    return new Vector2(minimapX, minimapY);
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
      markerGroup.addActor(marker);
    }
  }

  /**
   * Stops tracking an entity on the minimap.
   *
   * @param entity The entity to stop tracking.
   */
  public void stopTracking(Entity entity) {
    Image marker = trackedEntities.remove(entity);
    if (marker != null) markerGroup.removeActor(marker);
  }

  /**
   * Updates the marker on the minimap with a new drawable.
   *
   * @param entity The entity whose marker to change.
   * @param drawable The new drawable for the marker.
   */
  public void setMarker(Entity entity, Drawable drawable) {
    Image marker = trackedEntities.get(entity);
    if (marker != null) marker.setDrawable(drawable);
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

  @Override
  protected void draw(SpriteBatch batch) {
    // Handled by the stage
  }

  @Override
  public float getZIndex() {
    return Float.POSITIVE_INFINITY;
  }

  @Override
  public int getLayer() {
    return Integer.MAX_VALUE;
  }

  @Override
  public void dispose() {
    super.dispose();
    if (rootTable != null) {
      rootTable.remove();
    }
  }
}
