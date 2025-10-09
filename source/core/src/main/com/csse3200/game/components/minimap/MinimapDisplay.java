package com.csse3200.game.components.minimap;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.MinimapService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import net.dermetfan.utils.Pair;

import java.util.Map;

/**
 * A UI component for displaying a minimap.
 */
public class MinimapDisplay extends UIComponent {
  private final Camera camera;
  private float minimapScaleFactor;
  private final Vector2 textureBottomLeft;
  private final Vector2 fullMapSize;
  private final float displaySize;
  private final MinimapOptions options;
  private Cell<?> contentCell;
  private Cell<?> rootCell;
  private Table rootTable;
  private Image minimapImage;
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
    public MinimapPosition position = MinimapPosition.BOTTOM_RIGHT;
  }

  /**
   * Creates a new minimap display.
   *
   * @param displaySize The size (width and height) of the minimap on the screen.
   * @param options Options for the minimap shape and position.
   */
  public MinimapDisplay(float displaySize, MinimapOptions options) {
    camera = ServiceLocator.getRenderService().getRenderer().getCamera().getCamera();

    MinimapService service = ServiceLocator.getMinimapService();
    Vector2 textureTopRight = service.getTextureTopRight();
    this.textureBottomLeft = service.getTextureBottomLeft();
    fullMapSize = textureTopRight.cpy().sub(textureBottomLeft);
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
    rootTable.setName("minimap");
    rootTable.setUserObject(this);

    switch (options.position) {
      case TOP_LEFT -> rootTable.top().left();
      case TOP_RIGHT -> rootTable.top().right();
      case BOTTOM_LEFT -> rootTable.bottom().left();
      case BOTTOM_RIGHT -> rootTable.bottom().right();
    }
    rootTable.pad(10f);

    final MinimapService service = ServiceLocator.getMinimapService();
    minimapImage = new Image(service.getMinimapTexture());

    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(new Color(0, 0, 0, 0.60f)); // Black with 60% opacity
    pixmap.fill();
    Texture backgroundTexture = new Texture(pixmap);
    pixmap.dispose();
    Image background = new Image(backgroundTexture);

    Stack stack = new Stack();
    stack.addActor(background);
    stack.add(markers);
    stack.clipBegin();

    markers.addActor(minimapImage);

    Table content = new Table();
    contentCell = content.add(stack).size(displaySize);
    rootCell = rootTable.add(content.clip());
    stage.addActor(rootTable);
  }

  @Override
  public void update() {
    if (!rootTable.isVisible()) return;

    minimapScaleFactor = displaySize / camera.viewportHeight;
    minimapImage.setSize(fullMapSize.x * minimapScaleFactor, fullMapSize.y * minimapScaleFactor);
    final float width = minimapScaleFactor * camera.viewportWidth;
    rootCell.width(width);
    contentCell.width(width);

    final Vector2 cameraOrigin =
        new Vector2(camera.viewportWidth / 2 - camera.position.x, camera.viewportHeight / 2 - camera.position.y);
    final Vector2 cameraMinimapOrigin = worldToMinimapCoordinates(cameraOrigin);
    minimapImage.setPosition(cameraMinimapOrigin.x, cameraMinimapOrigin.y);

    for (Map.Entry<Entity, Image> entry : ServiceLocator.getMinimapService().getTrackedEntities().entrySet()) {
      final Vector2 minimapCoords = worldToMinimapCoordinates(entry.getKey().getPosition());
      Image marker = entry.getValue();
      marker.setPosition(cameraMinimapOrigin.x + minimapCoords.x, cameraMinimapOrigin.y + minimapCoords.y);
    }
  }

  private Vector2 worldToMinimapCoordinates(Vector2 worldPos) {
    float mapX = (worldPos.x - textureBottomLeft.x) * minimapScaleFactor;
    float mapY = (worldPos.y - textureBottomLeft.y) * minimapScaleFactor;
    return new Vector2(mapX, mapY);
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
