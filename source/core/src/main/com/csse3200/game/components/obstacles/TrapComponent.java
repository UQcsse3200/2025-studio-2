package com.csse3200.game.components.obstacles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;

public class TrapComponent extends CombatStatsComponent {
    private final Vector2 safeSpot;
    private final int rotation;
    private final float trapSize = 0.85f; // The width of half a unit
    private final float playerHeight = 1f;

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
    public void setHealth(int health) {
        // traps should not be able to be damaged or healed.
    }

    /**
     * Override CombatStatsComponent health-related functions to do nothing,
     * as traps should not be able to be damaged or healed.
     * @param health health
     */
    @Override
    public void addHealth(int health) {
        // traps should not be able to be damaged or healed.
    }

    /**
     * Override CombatStatsComponent health-related functions to do nothing,
     * as traps should not be able to be damaged or healed.
     */
    @Override
    public void hit(CombatStatsComponent attacker) {
        // traps should not be able to be damaged or healed.
    }

    /**
     * Override CombatStatsComponent health-related functions to do nothing,
     * as traps should not be able to be damaged or healed.
     */
    @Override
    public Boolean isDead() {
        return true;
    }

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

        Vector2 playerPos = player.getPosition();
        Vector2 trapPos = trap.getPosition();

        boolean legalDirection;
        if (rotation % 2 == 0) { // Avoid side-on collisions
            if (playerPos.x > trapPos.x) {
                legalDirection = (playerPos.x < (trapPos.x + trapSize));
            } else {
                legalDirection = (playerPos.x > (trapPos.x - trapSize));
            }
        } else { // Fix the over-enthusiastic lower bound
            if (playerPos.y > trapPos.y) {
                legalDirection = (playerPos.y < (trapPos.y + playerHeight));
            } else {
                legalDirection = (playerPos.y > (trapPos.y - (playerHeight + trapSize/2)));
            }
        }

        legalDirection &= switch (rotation) {
            case 1 -> // Facing left
                    (// Must approach from left
                            player.getPosition().x < trap.getPosition().x);
            case 2 -> // Facing down
                    (// Must approach from beneath
                            playerPos.y < trapPos.y);
            case 3 -> // Facing right
                    (// Must approach from right
                            player.getPosition().x > (trap.getPosition().x-0.3f));
            default -> // Facing up
                    (// Must approach from above
                            player.getPosition().y > trap.getPosition().y);
        };

        // Damage player
        if (legalDirection) {
            Gdx.app.postRunnable(() -> player.setPosition(safeSpot));
            player.getComponent(CombatStatsComponent.class).hit(this);
        }
    }
}
