package com.csse3200.game.components;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * When this entity touches a valid enemy's hitbox, deal damage to them and apply a knockback.
 *
 * <p>Requires CombatStatsComponent, HitboxComponent on this entity.
 *
 * <p>Damage is only applied if target entity has a CombatStatsComponent. Knockback is only applied
 * if target entity has a PhysicsComponent.
 */
public class TouchAttackComponent extends Component {
  private short targetLayer;
  private float knockbackForce = 0f;
  private CombatStatsComponent combatStats;
  private HitboxComponent hitboxComponent;
  private CombatStatsComponent attackerStats;
  /**
   * Create a component which attacks entities on collision, without knockback.
   * @param targetLayer The physics layer of the target's collider.
   */
  public TouchAttackComponent(short targetLayer) {
    this.targetLayer = targetLayer;
    this.attackerStats = null;

  }

  /**
   * Create a component which attacks entities on collision, with knockback.
   * @param targetLayer The physics layer of the target's collider.
   * @param knockback The magnitude of the knockback applied to the entity.
   */
  public TouchAttackComponent(short targetLayer, float knockback,CombatStatsComponent attackerStats) {
    this.targetLayer = targetLayer;
    this.knockbackForce = knockback;
    this.attackerStats = attackerStats;
  }

  /**
   * Returns the knockback force applied to the enitity it collides with.
   *
   * @return the size of the knockback force
   */
  public float getKnockbackForce() {
      return knockbackForce;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
    combatStats = entity.getComponent(CombatStatsComponent.class);
    hitboxComponent = entity.getComponent(HitboxComponent.class);
  }

  private void onCollisionStart(Fixture me, Fixture other) {
      System.out.println("Laser collision detected: me=" + me + ", other=" + other);
    if ( hitboxComponent == null || hitboxComponent.getFixture() == null) {
      // Not triggered by hitbox, ignore
      return;
    }

    if (!PhysicsLayer.contains(targetLayer, other.getFilterData().categoryBits)) {
      // Doesn't match our target layer, ignore
      return;
    }

    Object data = other.getBody().getUserData();
    if (!(data instanceof BodyUserData) ){
        return;
    }


    // Try to attack target.
    Entity target = ((BodyUserData)data).entity;
    if(target == null) return;

    CombatStatsComponent targetStats = target.getComponent(CombatStatsComponent.class);

      // Try to attack target
      if(targetStats != null && attackerStats != null) {
          targetStats.hit(attackerStats); // existing
          System.out.println("TouchAttackComponent: Laser hit " + target + " for " + attackerStats.getBaseAttack() + " damage");
      }


// Step 1 fallback: Use this entity's CombatStatsComponent if attackerStats is null (for lasers)
      if (targetStats != null && attackerStats == null) {CombatStatsComponent laserStats = entity.getComponent(CombatStatsComponent.class);
          if (laserStats != null) {targetStats.addHealth(-laserStats.getBaseAttack());
              System.out.println("TouchAttackComponent: Laser fallback dealt " + laserStats.getBaseAttack() + " damage to " + target);
          }
      }
    // Apply knockback
    PhysicsComponent physicsComponent = target.getComponent(PhysicsComponent.class);
    if (physicsComponent != null && knockbackForce > 0f) {
      Body targetBody = physicsComponent.getBody();
      Vector2 direction = target.getCenterPosition().sub(entity.getCenterPosition());
      Vector2 impulse = direction.setLength(knockbackForce);
      targetBody.applyLinearImpulse(impulse, targetBody.getWorldCenter(), true);
    }
    // Direction from target -> attacker so UI can point at the source
    Vector2 toAttacker = entity.getCenterPosition().sub(target.getCenterPosition()).nor();
    target.getEvents().trigger("damageDirection", toAttacker);
  }

}
