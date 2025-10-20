package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.TiledWallComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Factory for creating static vertical wall entities.
 * Walls:
 * - Are static (do not move)
 * - Block movement
 * - Are not considered "ground" (player shouldn't stand on them)
 * - Use distinct textures from platforms
 */
public class WallFactory {
  private WallFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }

  /**
   * Create a vertical wall at world position (x, y) with given size (width, height).
   * Texture is scaled to fit the specified size.
   *
   * @param x       World X (bottom-left)
   * @param y       World Y (bottom-left)
   * @param width   Wall width in world units
   * @param height  Wall height in world units
   * @param texture Wall texture
   * @return Configured wall entity
   */
  public static Entity createWall(float x, float y, float width, float height, String texture) {
    Entity wall =
        new Entity()
            .addComponent(new TextureRenderComponent(texture))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
    wall.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    wall.getComponent(TextureRenderComponent.class).scaleEntity();

    wall.setPosition(x + width / 2.0f, y + height / 2.0f);
    return wall;
  }

  /**
   * Create a vertical wall at world position (x, y) with given size (width, height).
   * Texture is scaled to fit the specified size.
   *
   * @return Configured wall entity
   */
  public static Entity createTiledWall() {
    Texture topTexture = ServiceLocator.getResourceService().getAsset("images/walltiles/WallTop.png", Texture.class);
    Texture middleTexture = ServiceLocator.getResourceService().getAsset("images/walltiles/WallTile.png", Texture.class);

    Entity wall =
        new Entity()
            .addComponent(new TiledWallComponent(new TextureRegion(topTexture), new TextureRegion(middleTexture)))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
    wall.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    return wall;
  }
}
