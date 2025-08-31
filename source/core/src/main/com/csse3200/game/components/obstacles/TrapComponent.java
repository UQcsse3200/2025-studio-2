package com.csse3200.game.components.obstacles;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;

public class TrapComponent extends CombatStatsComponent {
    private Vector2 safePosition; // The nearest safe position to which to respawn colliding entities.
    // Note: Will want to improve logic to set safePosition based on
    public TrapComponent(Vector2 safePosition) {
        this(0, 10, safePosition);
    }

    public TrapComponent(int health, int baseAttack) {
        super(health, baseAttack);
    }

    public TrapComponent(int health, int baseAttack, Vector2 safePosition) {
        super(health, baseAttack);
        this.safePosition = safePosition;
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

        Entity player = collider.getEntity();
        player.getComponent(CombatStatsComponent.class).hit(this);
        // TODO PROBLEM THAT THE PLAYER COLLIDES WAY TOO SOON IF APPROACHING FROM BELOW
        //  (make a second collider below only and if player is also colliding with that then cancel?)
        // also todo rotation
        // also todo position resets but fuck that it still breaks the game after a couple hours I need to do 3506.
//        System.out.println("About to break everything");
//        player.getComponent(PhysicsComponent.class).getBody().setTransform(safePosition, 0f);
    }
}
