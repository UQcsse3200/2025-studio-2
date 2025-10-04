package com.csse3200.game.components.projectiles;


import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;

/**
 * Component for a laser projectile.
 * This component is attached to a laser entity to give it specific behavior.
 */
public class LaserComponent extends Component {
    private final Entity shooter;
    private final float speed;
    private final int damage;

    /**
     * @param shooter The entity that fired the laser.
     * @param speed The speed of the laser.
     * @param damage The damage the laser deals.
     */
    public LaserComponent(Entity shooter, float speed, int damage) {
        this.shooter = shooter;
        this.speed = speed;
        this.damage = damage;
    }

    @Override
    public void create() {
        entity.setPosition(shooter.getCenterPosition());

        // You can add logic here if the laser needs to do something the moment it is created.
    }
}
