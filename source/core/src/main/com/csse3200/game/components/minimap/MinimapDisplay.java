package com.csse3200.game.components.minimap;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.MinimapService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import net.dermetfan.utils.Pair;

import java.lang.reflect.Array;
import java.util.Map;

/**
 * A UI component for displaying a minimap.
 */
public class MinimapDisplay extends UIComponent {
  private final Camera camera;
  private final MinimapService service;
  private final Vector2 origin;
  private final Vector2 worldSizeRemapFactor;
  private final float displaySize;
  private final MinimapOptions options;
  private Table rootTable;
  private final Group markers = new Group();

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
    camera = ServiceLocator.getRenderService().getStage().getCamera();

    service = ServiceLocator.getMinimapService();
    Vector2 worldSize = service.getWorldSize();
    this.worldSizeRemapFactor = new Vector2(displaySize / worldSize.x, displaySize / worldSize.y);
    this.origin = service.getOrigin();
    this.options = options;
    this.displaySize = displaySize;
  }

  /**
   * Adds the given marker to the markers group.
   *
   * @param marker the marker image to be added to the group.
   */
  public void addMarker(Image marker) {
    markers.addActor(marker);
  }

  /**
   * Removes the given marker form the group
   *
   * @param marker the marker image to removed.
   */
  public void removeMarker(Image marker) {
    markers.removeActor(marker);
  }

  @Override
  public void create() {
    super.create();
    addActors();
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

    Texture minimapTexture = ServiceLocator.getMinimapService().getMinimapTexture();
    Image minimapImage = new Image(minimapTexture);

    Stack contentStack = new Stack();
    contentStack.add(minimapImage);
    contentStack.add(markers);

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

    final Pair<Vector2, Vector2> bounds = getCameraBounds();
    final Vector2 bottomLeft = bounds.getKey();
    final Vector2 topRight = bounds.getValue();
    final Vector2 adjustedTopRight = topRight.sub(bottomLeft);

    for (Map.Entry<Entity, Image> entry: service.getTrackedEntities().entrySet()) {
      final Vector2 position = entry.getKey().getPosition().sub(bottomLeft);
      final Image marker = entry.getValue();
      if (position.x < 0 || position.y < 0 || position.x > adjustedTopRight.x || position.y > adjustedTopRight.y) continue;

      final Vector2 minimapCoords = worldToMinimapCoordinates(position);
      marker.setPosition(
          minimapCoords.x - marker.getWidth() / 2f,
          minimapCoords.y - marker.getHeight() / 2f
      );
    }
  }

  public Pair<Vector2, Vector2> getCameraBounds() {
    float cameraX = camera.position.x;
    float cameraY = camera.position.y;
    float halfViewportWidth = camera.viewportWidth / 2;
    float halfViewportHeight = camera.viewportHeight / 2;

    Vector2 bottomLeft = new Vector2(cameraX - halfViewportWidth, cameraY - halfViewportHeight);
    Vector2 topRight = new Vector2(cameraX + halfViewportWidth, cameraY + halfViewportHeight);

    return new Pair<>(bottomLeft, topRight);
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
  }
}
