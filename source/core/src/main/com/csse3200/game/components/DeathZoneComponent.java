package com.csse3200.game.components;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;

/**
 * DeathZone component that makes sure to deal max damage to a player to kill them.
 * As well as remove regular entity characteristics from the DeathZone such as health
 */
public class DeathZoneComponent extends CombatStatsComponent {

    /**
     * Setting DeathZone Base attack power to max
     */
    public DeathZoneComponent() {
        this(0, 10000);
    }

    /**
     * DeathZone component Initialisation
     * @param health
     * @param baseAttack
     */
    public DeathZoneComponent(int health, int baseAttack) {
        super(health, baseAttack);
    }


    /**
     * Override CombatStatsComponent health-related functions to do nothing,
     * as death zones should not be able to be damaged or healed.
     * @param health health
     */
    @Override
    public void setHealth(int health) {    }

    /**
     * Override CombatStatsComponent health-related functions to do nothing,
     * as death zones should not be able to be damaged or healed.
     * @param health health
     */
    @Override
    public void addHealth(int health) {    }

    /**
     * Override CombatStatsComponent health-related functions to do nothing,
     * as death zones should not be able to be damaged or healed.
     */
    @Override
    public void hit(CombatStatsComponent attacker) {}

    /**
     * Override CombatStatsComponent health-related functions to do nothing,
     * as death zones should not be able to be damaged or healed.
     */
    @Override
    public Boolean isDead() {return true;}

    /**
     * Damages colliding entity on contact with death zone
     *
     * @param collider Player's ColliderComponent (null if player leaves collision range)
     */
    public void damage(ColliderComponent collider) {
        if(collider == null) {
            return;
        }

        Entity deathZone = this.getEntity();

        // Damage player
        Entity player = collider.getEntity();
        if (player.getPosition().y >= deathZone.getPosition().y) {
            player.getComponent(CombatStatsComponent.class).hit(this);
            //player.requestMoveToSafeSpot(resetPos); //While no death is implemented
        }
    }
}
