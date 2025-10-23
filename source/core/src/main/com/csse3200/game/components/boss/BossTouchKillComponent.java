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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When this entity touches a valid enemy's hitbox, deal damage to them and apply a knockback.
 *
 * <p>Requires CombatStatsComponent, HitboxComponent on this entity.
 *
 * <p>Damage is only applied if target entity has a CombatStatsComponent. Knockback is only applied
 * if target entity has a PhysicsComponent.
 */
public class BossTouchKillComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(BossTouchKillComponent.class);

  private short targetLayer;
  private CombatStatsComponent combatStats;
  private HitboxComponent hitboxComponent;

  private boolean hitPlayer = false;

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
    //if (hitPlayer) return; // Only damage once
    if (hitboxComponent.getFixture() != me) return;
    if (!PhysicsLayer.contains(targetLayer, other.getFilterData().categoryBits)) return;

    // Try to attack target.
    Entity target = ((BodyUserData) other.getBody().getUserData()).entity;
    CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);

    if (targetStats == null) return;
    entity.getEvents().trigger("touchKillStart");
    createBlackHole();

    // Bypass invulnerability frames by directly setting health
    int newHealth = targetStats.getHealth() - combatStats.getBaseAttack();
    targetStats.setHealth(newHealth);
  }

  private void createBlackHole() {
    EntityService entityService = ServiceLocator.getEntityService();
    if (entityService == null) {
      // Only VFX so safe to skip
      logger.warn("EntityService is null; skipping black hole spawn.");
      return;
    }

    try {
      final Vector2 pos = entity.getCenterPosition().cpy();
      final float r = 20f;
      Entity blackHole = ExplosionFactory.createBlackHole(pos, r);
      entityService.register(blackHole);

      // Auto-dispose after 5s
      Timer.schedule(new Timer.Task() {
        @Override
        public void run() {
          try {
            blackHole.dispose();
          } catch (Exception e) {
            logger.warn("[BOSS TOUCH KILL] Dispose failed for blackHole", e);
          }
        }
      }, 3f);
    } catch (Exception e) {
      // VFX failure should not break gameplay, log and continue
      logger.warn("[BOSS TOUCH KILL] Failed to create/register blackHole VFX", e);
    }
  }
}
