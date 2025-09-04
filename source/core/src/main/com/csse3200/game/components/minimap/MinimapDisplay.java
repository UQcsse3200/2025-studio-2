package com.csse3200.game.components.minimap;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.MinimapService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
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
  private Table rootTable;

  /**
   * Dictate where the Minimap will be drawn
   */
  public enum MinimapPosition {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
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
   * @param displaySize The size (width and height) of the minimap on the screen.
   * @param options Options for the minimap shape and position.
   */
  public MinimapDisplay(float displaySize, MinimapOptions options) {
    MinimapService minimapService = ServiceLocator.getMinimapService();
    if (minimapService == null) {
      throw new IllegalStateException("MinimapService must be registered before creating a MinimapDisplay.");
    }
    this.minimapTexture = minimapService.getMinimapTexture();
    Vector2 worldSize = minimapService.getWorldSize();
    this.worldSizeRemapFactor = new Vector2(displaySize / worldSize.x, displaySize / worldSize.y);
    this.origin = minimapService.getOrigin();
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

    Stack contentStack = new Stack();
    contentStack.add(minimapImage);
    contentStack.add(ServiceLocator.getMinimapService().getMinimapMarkerGroup());

    Table minimapContainer = new Table();
    minimapContainer.add(contentStack).size(displaySize);

    Stack finalStack = new Stack();
    finalStack.add(minimapContainer);

    rootTable.add(finalStack).size(displaySize);
    stage.addActor(rootTable);
  }

  @Override
  public void update() {
    if (!rootTable.isVisible()) return;

    for (Map.Entry<Entity, Image> entry : ServiceLocator.getMinimapService().getTrackedEntities().entrySet()) {
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

  /**
   * @param visible: Set the visibility of minimap display to this value.
   */
  public void setVisible(boolean visible) {
    rootTable.setVisible(visible);
    if (visible) syncMarkers();
  }

  private void syncMarkers() {
    Group markerGroup = ServiceLocator.getMinimapService().getMinimapMarkerGroup();
    for (Map.Entry<Entity, Image> serviceEntry : ServiceLocator.getMinimapService().getTrackedEntities().entrySet()) {
      markerGroup.addActor(serviceEntry.getValue());
    }
  }
}
