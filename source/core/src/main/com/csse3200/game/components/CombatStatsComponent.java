package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component used to store information related to combat such as health, attack, etc.
 * Any entities which engage in combat should have an instance of this class registered.
 * This class can be extended for more specific combat needs.
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

    public Boolean isDead() {
        return health <= 0;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        int oldHealth = this.health;
        this.health = Math.max(0, health);

        if (entity != null) {
            // 通知 UI 更新生命值
            entity.getEvents().trigger("updateHealth", this.health);

            // 如果是第一次掉到 0，触发死亡事件
            if (oldHealth > 0 && this.health == 0) {
                entity.getEvents().trigger("playerDied");
            }
        }
    }

    public void addHealth(int health) {
        setHealth(this.health + health);
    }

    public int getBaseAttack() {
        return baseAttack;
    }

    public void setBaseAttack(int attack) {
        if (attack >= 0) {
            this.baseAttack = attack;
        } else {
            logger.error("Can not set base attack to a negative attack value");
        }
    }

    /**
     * Called when this entity is hit by an attacker.
     * @param attacker the attacker's CombatStatsComponent
     */
    public void hit(CombatStatsComponent attacker) {
        long currentFrame = Gdx.graphics.getFrameId();
        if (currentFrame - lastHitFrame > INVULN_FRAMES) {
            lastHitFrame = currentFrame;

            int newHealth = getHealth() - (attacker.getBaseAttack()/4);
            setHealth(newHealth);

            // Trigger hurt animation/event
            entity.getEvents().trigger("hurt");
        }
    }
}
