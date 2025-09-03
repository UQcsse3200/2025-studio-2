package com.csse3200.game.components.obstacles;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;

public class TrapComponent extends CombatStatsComponent {
    private Vector2 safeSpot;

    public TrapComponent(Vector2 safeSpot) {
        this(0, 40, safeSpot);
    }

    public TrapComponent(int health, int baseAttack, Vector2 safeSpot) {
        super(health, baseAttack);
        this.safeSpot = safeSpot;
    }


    /**
     * Override CombatStatsComponent health-related functions to do nothing,
     * as traps should not be able to be damaged or healed.
     * @param health health
     */
    @Override
    public void setHealth(int health) {    }

    /**
     * Override CombatStatsComponent health-related functions to do nothing,
     * as traps should not be able to be damaged or healed.
     * @param health health
     */
    @Override
    public void addHealth(int health) {    }

    /**
     * Override CombatStatsComponent health-related functions to do nothing,
     * as traps should not be able to be damaged or healed.
     */
    @Override
    public void hit(CombatStatsComponent attacker) {}

    /**
     * Override CombatStatsComponent health-related functions to do nothing,
     * as traps should not be able to be damaged or healed.
     */
    @Override
    public Boolean isDead() {return true;}

    /**
     * Sets whether a player is in interaction range of this button
     * Adds the player to the "interact" event the first time
     *
     * @param collider Player's ColliderComponent (null if player leaves collision range)
     */
    public void damage(ColliderComponent collider) {
        if(collider == null) {
            return;
        }

        Entity trap = this.getEntity();

        // Damage player
        Entity player = collider.getEntity();
        if (player.getPosition().y >= trap.getPosition().y) {
            player.getComponent(CombatStatsComponent.class).hit(this);
            player.requestMoveToSafeSpot(safeSpot);
        }
    }
}
