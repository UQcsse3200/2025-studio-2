package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.minimap.MinimapComponent;
import com.csse3200.game.components.npc.VolatilePlatformAnimationController;
import com.csse3200.game.components.platforms.ButtonTriggeredPlatformComponent;
import com.csse3200.game.components.platforms.MovingPlatformComponent;
import com.csse3200.game.components.platforms.VolatilePlatformComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.lighting.LightingDefaults;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.TiledPlatformComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Factory to create Platform entities.
 *
 * <p>Each Platform entity type should have a creation method that returns a corresponding entity.
 */
public class PlatformFactory {
  private static final Texture platformTexture = new Texture("images/platform.png");
  private static final TextureRegion leftEdge = new TextureRegion(platformTexture, 0, 0, 16, 16);
  private static final TextureRegion middleTile = new TextureRegion(platformTexture, 16, 0, 16, 16);
  private static final TextureRegion rightEdge = new TextureRegion(platformTexture, 32, 0, 16, 16);

  private PlatformFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }

  /**
   * Creates a static platform entity.
   *
   * @return entity
   */
  public static Entity createStaticPlatform() {
    Entity platform = new Entity()
        .addComponent(new TiledPlatformComponent(leftEdge, middleTile, rightEdge))
        .addComponent(new PhysicsComponent())
        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    platform.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    return platform;
  }

  /**
   * Creates a dynamic moving platform entity. Movement is based upon the calculation of offsetWorld.
   *
   * @param offsetWorld the world offset for the platform's movement
   * @param speed       the speed at which the platform moves
   * @return entity the created moving platform entity
   */
  public static Entity createMovingPlatform(Vector2 offsetWorld, float speed) {
    ColliderComponent collider = new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE);
    collider.setFriction(2f);
    Entity platform = new Entity()
        .addComponent(new TiledPlatformComponent(leftEdge, middleTile, rightEdge))
        .addComponent(new PhysicsComponent())
        .addComponent(collider)
        .addComponent(new MovingPlatformComponent(offsetWorld, speed));

    platform.getComponent(PhysicsComponent.class).setBodyType(BodyType.KinematicBody);
    return platform;
  }

  /**
   * Creates button triggered platform, where if the player presses the button, the platform moves
   *
   * @param offsetWorld
   * @param speed
   * @return
   */
  public static Entity createButtonTriggeredPlatform(Vector2 offsetWorld, float speed) {
    Entity platform = new Entity()
        .addComponent(new TiledPlatformComponent(leftEdge, middleTile, rightEdge))
        .addComponent(new PhysicsComponent())
        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
        .addComponent(new ButtonTriggeredPlatformComponent(offsetWorld, speed));

    platform.getComponent(PhysicsComponent.class).setBodyType(BodyType.KinematicBody);
    return platform;
  }

  /**
   * Creates a static platform that also reflects lasers,
   * using Tristyn's code from BoxFactory.createReflectorBox.
   *
   * @return the platform created
   */
  public static Entity createReflectivePlatform() {
    TextureRegion texture = new TextureRegion(
            new Texture("images/mirror-cube-off.png"), 0, 0, 16, 16);
    Entity reflectorPlatform = new Entity()
            .addComponent(new TiledPlatformComponent(texture, texture, texture))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.LASER_REFLECTOR));

    reflectorPlatform.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);createStaticPlatform();

    ConeLightComponent light = new ConeLightComponent(
            ServiceLocator.getLightingService().getEngine().getRayHandler(),
            LightingDefaults.RAYS,
            Color.RED,
            1f,
            0f,
            180f
    ).setFollowEntity(false);
    reflectorPlatform.addComponent(light);

    return reflectorPlatform;
  }

  /**
   * Creates volatile platform, where if the player stands on it for more than lifetime, the platform disappears.
   * Platform respawns after respawnDelay.
   *
   * @param lifetime
   * @param respawnDelay
   * @return
   */
  public static Entity createVolatilePlatform(float lifetime, float respawnDelay) {
    AnimationRenderComponent animator =
            new AnimationRenderComponent(
                    ServiceLocator.getResourceService().getAsset("images/volatile_platform.atlas", TextureAtlas.class));
    //10 is number of frames in sprite
    animator.addAnimation("break", lifetime/11, Animation.PlayMode.NORMAL);
    animator.addAnimation("blank",0.1f,Animation.PlayMode.NORMAL);

    Entity platform = new Entity()
            .addComponent(new TextureRenderComponent("images/platform.png"))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
            .addComponent(new VolatilePlatformComponent(lifetime, respawnDelay))
            .addComponent(new VolatilePlatformAnimationController())
            .addComponent(animator);

    platform.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    return platform;
  }

  /**
   * Creates volatile platform that is linked to a pressure plate. When the pressure plate is pressed, the platform
   * appears, and when it is released the platform is hidden.
   *
   * @return pressure plate platform entity
   */
  public static Entity createPressurePlatePlatform() {
    Entity platform = new Entity()
            .addComponent(new TextureRenderComponent("images/empty.png"))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE))
            .addComponent(new VolatilePlatformComponent(0f, 0f));
    platform.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    return platform;
  }


}