package com.csse3200.game.components.obstacles;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;

public class TrapComponent extends CombatStatsComponent {
    private Vector2 safeSpot;
    private int rotation;

    public TrapComponent(Vector2 safeSpot, int rotation) {
        this(0, 40, safeSpot, rotation);
    }

    public TrapComponent(int health, int baseAttack, Vector2 safeSpot, int rotation) {
        super(health, baseAttack);
        this.safeSpot = safeSpot;
        this.rotation = rotation;
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
        Entity player = collider.getEntity();

        boolean legalDirection = switch (rotation) {
            case 1 -> // Facing left
                    (player.getPosition().x < trap.getPosition().x);
            case 2 -> // Facing down
                    (player.getPosition().y < trap.getPosition().y);
            case 3 -> // Facing right
                    (player.getPosition().x > trap.getPosition().x);
            default -> // Facing up
                    (player.getPosition().y >= trap.getPosition().y);
        };

        // Damage player
        if (legalDirection) {
            player.getComponent(CombatStatsComponent.class).hit(this);
            System.out.println("Player" + player.getPosition());
            System.out.println("Trap" + trap.getPosition());
            player.requestMoveToSafeSpot(safeSpot);
        }
    }
}
