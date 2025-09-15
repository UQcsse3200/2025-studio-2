package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component used to store information related to combat such as health, attack, etc. Any entities
 * which engage it combat should have an instance of this class registered. This class can be
 * extended for more specific combat needs.
 */
public class CombatStatsComponent extends Component {

  private static final Logger logger = LoggerFactory.getLogger(CombatStatsComponent.class);
  private int health;
  private int baseAttack;

  // "Grace period" between hits
  private static final long INVULN_FRAMES = 30;
  private long lastHitFrame = -100;

  public CombatStatsComponent(int health, int baseAttack) {
    setHealth(health);
    setBaseAttack(baseAttack);
  }

  // Copy constructor
  public CombatStatsComponent(CombatStatsComponent other) {
    this.health = other.health;
    this.baseAttack = other.baseAttack;
    this.lastHitFrame = other.lastHitFrame;
  }

  /**
   * Returns true if the entity's has 0 health, otherwise false.
   *
   * @return is player dead
   */
  public Boolean isDead() {
    return health == 0;
  }

  /**
   * Returns the entity's health.
   *
   * @return entity's health
   */
  public int getHealth() {
    return health;
  }

  /**
   * Sets the entity's health. Health has a minimum bound of 0.
   *
   * @param health health
   */
  public void setHealth(int health) {
    if (health >= 0) {
      this.health = health;
    } else {
      this.health = 0;
    }
    if (entity != null) {
      entity.getEvents().trigger("updateHealth", this.health);
    }
  }

  /**
   * Adds to the player's health. The amount added can be negative.
   *
   * @param health health to add
   */
  public void addHealth(int health) {
    setHealth(this.health + health);
  }

  /**
   * Returns the entity's base attack damage.
   *
   * @return base attack damage
   */
  public int getBaseAttack() {
    return baseAttack;
  }

  /**
   * Sets the entity's attack damage. Attack damage has a minimum bound of 0.
   *
   * @param attack Attack damage
   */
  public void setBaseAttack(int attack) {
    if (attack >= 0) {
      this.baseAttack = attack;
    } else {
      logger.error("Can not set base attack to a negative attack value");
    }
  }

  public void hit(CombatStatsComponent attacker) {
    long currentFrame = Gdx.graphics.getFrameId();
    if (currentFrame - lastHitFrame > INVULN_FRAMES) {
      lastHitFrame = currentFrame;
      int newHealth = getHealth() - attacker.getBaseAttack();
      setHealth(newHealth);

      // Animate hurt
      entity.getEvents().trigger("hurt");
    }
  }
}
