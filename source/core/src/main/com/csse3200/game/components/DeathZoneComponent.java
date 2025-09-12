package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.ColliderComponent;

public class DeathZoneComponent extends CombatStatsComponent {
    private Vector2 resetPos;

    public DeathZoneComponent(Vector2 resetPos) {
        this(0, 10000, resetPos);
    }

    public DeathZoneComponent(int health, int baseAttack, Vector2 resetPos) {
        super(health, baseAttack);
        this.resetPos = resetPos;
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
            player.requestMoveToSafeSpot(resetPos); //While no death is implemented
        }
    }
}
