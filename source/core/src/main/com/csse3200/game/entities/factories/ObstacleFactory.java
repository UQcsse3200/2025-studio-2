package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.minimap.MinimapComponent;
import com.csse3200.game.components.obstacles.DoorComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.services.ServiceLocator;

/**
 * Factory to create obstacle entities.
 *
 * <p>Each obstacle entity type should have a creation method that returns a corresponding entity.
 */
public class ObstacleFactory {

  /**
   * Creates a tree entity.
   * @return entity
   */
  public static Entity createTree() {
    Entity tree =
        new Entity()
            .addComponent(new TextureRenderComponent("images/tree.png"))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    tree.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    tree.getComponent(TextureRenderComponent.class).scaleEntity();
    tree.scaleHeight(2.5f);
    PhysicsUtils.setScaledCollider(tree, 0.5f, 0.2f);
    return tree;
  }

  /**
   * Creates an invisible physics wall.
   * @param width Wall width in world units
   * @param height Wall height in world units
   * @return Wall entity of given width and height
   */
  public static Entity createWall(float width, float height) {
    Entity wall = new Entity()
        .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
    wall.setScale(width, height);
    return wall;
  }

    /**
     * Creates a door Entity with key identifier
     * @param keyId the unique key identifier that can unlock this door
     * @return a new door entity bound to {@code keyId}, in the locked state
     */
  public static Entity createDoor (String keyId, GameArea area) {

    Entity door = new Entity();

    AnimationRenderComponent animator = new AnimationRenderComponent(
        ServiceLocator.getResourceService().getAsset("images/doors.atlas", TextureAtlas.class)
    );

    // Add all door animations
    animator.addAnimation("door_closed", 0.2f, Animation.PlayMode.LOOP);
    animator.addAnimation("door_opening", 0.1f, Animation.PlayMode.NORMAL);
    animator.addAnimation("door_open", 0.2f, Animation.PlayMode.LOOP);


    door.addComponent(animator);
    door.addComponent(new DoorComponent(keyId, area));

    // Physics components
    door.addComponent(new PhysicsComponent());
    door.addComponent(new ColliderComponent());
    door.addComponent(new HitboxComponent().setLayer(PhysicsLayer.OBSTACLE));
    door.addComponent(new MinimapComponent("images/door_open.png"));

    // Make sure door starts in closed state
    door.getComponent(PhysicsComponent.class).getBody().setType(BodyDef.BodyType.StaticBody);

    return door;
  }

  private ObstacleFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
