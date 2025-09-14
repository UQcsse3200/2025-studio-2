package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/** Render a static texture. */
public class TextureRenderComponent extends RenderComponent {
  private Texture texture;
  private float rotation = 0f;
  private final Vector2 origin  = new Vector2(0, 0);

  /**
   * @param texturePath Internal path of static texture to render.
   *                    Will be scaled to the entity's scale.
   */
  public TextureRenderComponent(String texturePath) {
    this(ServiceLocator.getResourceService().getAsset(texturePath, Texture.class));
  }

  public void setTexture(String texture) {
    this.texture = ServiceLocator.getResourceService().getAsset(texture, Texture.class);
  }

  /**
   * Returns the texture image path
   *
   * @return the image path
   */
  public Texture getTexture() {
      return texture;
  }

  /**
   * Sets rotation of the texture
   *
   * @param rotation What to rotate by (0-360 degrees)
   */
  public void setRotation(float rotation) {
    this.rotation = rotation;
  }

  /**
   * @return Rotation degree of texture
   */
  public double getRotation() {
    return rotation;
  }

  public void setOrigin(float x, float y) {
    origin.set(x, y);
  }

  /** @param texture Static texture to render. Will be scaled to the entity's scale. */
  public TextureRenderComponent(Texture texture) {
    this.texture = texture;
  }

  /** Scale the entity to a width of 1 and a height matching the texture's ratio */
  public void scaleEntity() {
    entity.setScale(1f, (float) texture.getHeight() / texture.getWidth());
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 position = entity.getPosition();
    Vector2 scale = entity.getScale();

    if (origin.equals(new Vector2(0, 0))) {
      origin.set(scale.x / 2f, scale.y / 2f);
    }

    if (rotation == 0f) {
      batch.draw(texture, position.x, position.y, scale.x, scale.y);
    } else {

      batch.draw(texture,
              position.x, position.y,
              origin.x, origin.y,
              scale.x, scale.y,
              1f, 1f,
              rotation,
              0, 0,
              texture.getWidth(), texture.getHeight(),
              false, false);
    }
  }
}
