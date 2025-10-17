package com.csse3200.game.components.boss;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.ExplosionFactory;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * When this entity touches a valid enemy's hitbox, deal damage to them and apply a knockback.
 *
 * <p>Requires CombatStatsComponent, HitboxComponent on this entity.
 *
 * <p>Damage is only applied if target entity has a CombatStatsComponent. Knockback is only applied
 * if target entity has a PhysicsComponent.
 */
public class BossTouchKillComponent extends Component {
  private short targetLayer;
  private CombatStatsComponent combatStats;
  private HitboxComponent hitboxComponent;

  /**
   * Create a component which attacks entities on collision, without knockback.
   * @param targetLayer The physics layer of the target's collider.
   */
  public BossTouchKillComponent(short targetLayer) {
    this.targetLayer = targetLayer;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    combatStats = entity.getComponent(CombatStatsComponent.class);
    hitboxComponent = entity.getComponent(HitboxComponent.class);
  }

  private void onCollisionStart(Fixture me, Fixture other) {
    if (hitboxComponent.getFixture() != me) {
      // Not triggered by hitbox, ignore
      return;
    }

    if (!PhysicsLayer.contains(targetLayer, other.getFilterData().categoryBits)) {
      // Doesn't match our target layer, ignore
      return;
    }

    // Try to attack target.
    Entity target = ((BodyUserData) other.getBody().getUserData()).entity;
    CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);

    entity.getEvents().trigger("touchKillStart");
    createBlackHole();
    target.setEnabled(false);

    if (targetStats != null) {
      Timer.schedule(new Timer.Task() {
        @Override public void run() {
          targetStats.hit(combatStats);
        }
      }, 1.5f);
    }
  }

  private void createBlackHole() {
    EntityService entityService = ServiceLocator.getEntityService();
    if (entityService == null) {
      return;
    }

    try {
      final Vector2 pos = entity.getCenterPosition().cpy();
      final float r = 20f;

      Entity blackhole = ExplosionFactory.createBlackHole(pos, r);
      if (blackhole != null) {
        entityService.register(blackhole);

        Timer.schedule(new Timer.Task() {
          @Override public void run() {
            if (blackhole != null) {
              blackhole.dispose();
            }
          }
        }, 5f);
      }
    } catch (Exception e) {
    }
  }
}
