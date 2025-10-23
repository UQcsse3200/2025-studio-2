package com.csse3200.game.physics;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;

public class PhysicsUtils {

  // Set the collider to the base of the entity, scaled relative to the entity size.
  public static void setScaledCollider(Entity entity, float scaleX, float scaleY) {
    Vector2 boundingBox = entity.getScale().cpy().scl(scaleX, scaleY);
    entity
        .getComponent(ColliderComponent.class)
        .setAsBoxAligned(
            boundingBox, PhysicsComponent.AlignX.CENTER, PhysicsComponent.AlignY.BOTTOM);
  }

  public static void setScaledCollider(Entity entity, float scaleX, float scaleY,
                                       PhysicsComponent.AlignX alignX, PhysicsComponent.AlignY alignY) {
    Vector2 boundingBox = entity.getScale().cpy().scl(scaleX, scaleY);
    entity
            .getComponent(ColliderComponent.class)
            .setAsBoxAligned(
                    boundingBox, alignX, alignY);
  }

  public static void setScaledCollider(Entity entity, float scaleX, float scaleY,
                                       PhysicsComponent.AlignX alignX, PhysicsComponent.AlignY alignY, float angle) {
    Vector2 boundingBox = entity.getScale().cpy().scl(scaleX, scaleY);
    entity
            .getComponent(ColliderComponent.class)
            .setAsBoxAligned(
                    boundingBox, alignX, alignY, angle);
  }

  private PhysicsUtils() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
