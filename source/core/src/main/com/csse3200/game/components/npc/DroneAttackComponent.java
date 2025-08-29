package com.csse3200.game.components.npc;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that handles drone bomb-dropping attacks when colliding with the player.
 * Requires CombatStatsComponent and HitboxComponent on this entity.
 */
public class DroneAttackComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(DroneAttackComponent.class);

    private final GameTime timeSource;
    private final float attackCooldown; // Cooldown between attacks in seconds
    private final short targetLayer;
    private long lastAttackTime;
    private CombatStatsComponent combatStats;
    private HitboxComponent hitboxComponent;

    /**
     * Create a drone attack component
     * @param targetLayer Physics layer of entities to attack (usually PLAYER)
     * @param attackCooldown Cooldown between attacks in seconds
     */
    public DroneAttackComponent(short targetLayer, float attackCooldown) {
        this.timeSource = ServiceLocator.getTimeSource();
        this.targetLayer = targetLayer;
        this.attackCooldown = attackCooldown;
        this.lastAttackTime = 0;
    }

    @Override
    public void create() {
        super.create();
        combatStats = entity.getComponent(CombatStatsComponent.class);
        hitboxComponent = entity.getComponent(HitboxComponent.class);

        // Listen for collision events
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);

        logger.debug("Drone attack component created with {}s cooldown", attackCooldown);
    }

    private void onCollisionStart(Fixture me, Fixture other) {
        // Check if this is our hitbox colliding
        if (hitboxComponent.getFixture() != me) {
            return;
        }

        // Check if we're colliding with our target layer
        if (!PhysicsLayer.contains(targetLayer, other.getFilterData().categoryBits)) {
            return;
        }

        // Check attack cooldown
        if (timeSource.getTimeSince(lastAttackTime) < attackCooldown * 1000) {
            return;
        }

        // Get the target entity
        Entity target = ((BodyUserData) other.getBody().getUserData()).entity;

        // Perform bomb attack
        performBombAttack(target);

        // Update last attack time
        lastAttackTime = timeSource.getTime();
    }

    private void performBombAttack(Entity target) {
        logger.debug("Drone performing bomb attack on {}", target);

        // Trigger animation/sound events
        entity.getEvents().trigger("dropStart");

        /*// Create and drop bomb at target's position
        Entity bomb = ProjectileFactory.createBomb(
                entity,
                target.getPosition(),
                2.0f, // 2 second explosion delay
                1.5f, // 1.5 unit explosion radius
                combatStats.getBaseAttack() // Use drone's attack damage
        );

        // Register the bomb in the entity service
        ServiceLocator.getEntityService().register(bomb);*/

        logger.debug("Bomb dropped at position {} targeting {}", target.getPosition(), target);
    }
}