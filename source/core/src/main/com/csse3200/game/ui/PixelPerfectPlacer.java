package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.Array;

/**
 * A layout component that allows placing actors at precise pixel coordinates relative to a background image.
 */
public class PixelPerfectPlacer extends Stack {
  private final Image backgroundImage;
  private final Group overlayGroup;
  private final Array<OverlayConstraint> overlays = new Array<>();
  final int textureWidth;
  final int textureHeight;

  private record OverlayConstraint(Actor actor, Rect rect) {}

  /**
   * Helper record to store the actor's pixel-based layout constraints.
   *
   * @param x the x coordinate of table in pixels
   * @param y the y coordinate of table in pixels
   * @param width the width of the table in pixels
   * @param height the height of the table in pixels
   */
  public record Rect(int x, int y, int width, int height) {}

  /**
   * Creates a new PixelPerfectPlacer with a specified background image.
   *
   * @param texture The background texture to use.
   */
  public PixelPerfectPlacer(Texture texture) {
    textureWidth = texture.getWidth();
    textureHeight = texture.getHeight();
    this.backgroundImage = new Image(texture);
    this.overlayGroup = new Group();

    this.add(backgroundImage);
    this.add(overlayGroup);
  }

  /**
   * Adds an actor to be placed on top of the background image.
   *
   * @param actor The actor to place (e.g., a Table, Button, etc.).
   * @param rect the box with x,y corresponding to top left corner of the box taken from the top left corner of the
   *            image (using gimp) and its width and height
   */
  public void addOverlay(Actor actor, Rect rect) {
    if (actor == null) throw new IllegalArgumentException("actor cannot be null");
    final Rect transformedPosition =
        new Rect(rect.x, textureHeight -  (rect.y + rect.height), rect.width, rect.height);
    overlays.add(new OverlayConstraint(actor, transformedPosition));
    overlayGroup.addActor(actor);
    // WidgetGroup's comment says to call this
    super.invalidate();
  }

  /**
   * This method is called by Scene2D's layout manager whenever the table's size or children change.
   * It calculates and applies the correct on-screen positions and sizes for all overlaid actors.
   */
  @Override
  public void layout() {
    super.layout();

    float scaleX = backgroundImage.getWidth() / textureWidth;
    float scaleY = backgroundImage.getHeight() / textureHeight;

    for (OverlayConstraint constraint : overlays) {
      constraint.actor.setBounds(
          constraint.rect.x * scaleX, constraint.rect.y * scaleY,
          constraint.rect.width * scaleX, constraint.rect.height * scaleY
      );
    }
  }
}