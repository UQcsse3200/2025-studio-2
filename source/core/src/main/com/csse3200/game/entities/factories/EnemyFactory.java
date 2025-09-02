package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.enemy.PatrolRouteComponent;
import com.csse3200.game.components.enemy.SpawnPositionComponent;
import com.csse3200.game.components.npc.DroneAnimationController;
import com.csse3200.game.components.npc.DroneAttackComponent;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.components.tasks.CooldownTask;
import com.csse3200.game.components.tasks.PatrolTask;
import com.csse3200.game.components.tasks.WanderTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.BaseEntityConfig;
import com.csse3200.game.entities.configs.EnemyConfigs;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Factory for creating different types of enemies.
 * - Base drones pursue the player but are otherwise idle.
 * - Patrolling drones follow a patrol route.
 */
public class EnemyFactory {
    private static final EnemyConfigs configs =
            FileLoader.readClass(EnemyConfigs.class, "configs/enemies.json");

    /**
     * Creates a drone enemy that remains idle unless chasing its target.
     * Has drone-specific animation, combat stats and chase task.
     * @param target that drone pursues when chasing
     * @return drone enemy entity
     */
    public static Entity createDrone(Entity target,Vector2 spawnPos) {
        BaseEntityConfig config = configs.drone;


        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/drone.atlas", TextureAtlas.class));

        // Add drone animations
        animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("drop", 0.5f, Animation.PlayMode.LOOP); // Attack animation

        Entity drone = createBaseEnemy(target);
        // Store the spawn position (so we can reset later)
        if (spawnPos != null) {
            drone.addComponent(new SpawnPositionComponent(spawnPos));
        }

        drone.getComponent(PhysicsMovementComponent.class).setMaxSpeed(1.4f); // Faster movement

        drone
                .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
                .addComponent(animator)
                .addComponent(new DroneAnimationController())
                .addComponent(new DroneAttackComponent(PhysicsLayer.PLAYER, 3.0f)); // 3 second attack cooldown

        AITaskComponent aiComponent = drone.getComponent(AITaskComponent.class);
        // Tasks
        ChaseTask chaseTask = new ChaseTask(target, 10, 3f, 4f);
        CooldownTask cooldownTask = new CooldownTask(2f);

        // When chase ends, activate cooldown
        drone.getEvents().addListener("chaseEnd", cooldownTask::activate);
        aiComponent.addTask(chaseTask);
        aiComponent.addTask(cooldownTask);


        AnimationRenderComponent arc = drone.getComponent(AnimationRenderComponent.class);
        arc.scaleEntity();
        arc.startAnimation("float");

        return drone;

    }

    /**
     * Same as basic drone enemy but patrols a given route, alternatively chasing a target when in range.
     * @param target that drone pursues when chasing
     * @param patrolRoute contains list of waypoints in patrol route
     * @return a patrolling drone enemy entity
     */
    public static Entity createPatrollingDrone(Entity target, Vector2[] patrolRoute) {
        Vector2 spawnPos = patrolRoute[0];
        Entity drone = createDrone(target, spawnPos);
        drone.addComponent(new PatrolRouteComponent(patrolRoute));

        AITaskComponent aiComponent = drone.getComponent(AITaskComponent.class);
        // Single cooldown + patrol
        CooldownTask cooldownTask = new CooldownTask(2f);
        drone.getEvents().addListener("chaseEnd", cooldownTask::activate);

        aiComponent.addTask(cooldownTask);       // priority 2 when active
        aiComponent.addTask(new PatrolTask(2f)); // default patrol

        return drone;
    }

    /**
     * Creates a base enemy entity with a minimal, reusable set of components that all enemies share
     * (physics, movement, collider, hitbox and AI task holder (with no tasks).
     * @return enemy
     */
    private static Entity createBaseEnemy(Entity target) {
        Entity enemy =
                new Entity()
                        .addComponent(new PhysicsComponent())
                        .addComponent(new PhysicsMovementComponent())
                        .addComponent(new ColliderComponent())
                        .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                        .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER,1.5f))
                        .addComponent(new AITaskComponent()); // Want this empty for base enemies

        PhysicsUtils.setScaledCollider(enemy, 1f, 1f);
        return enemy;
    }

    private EnemyFactory() {throw new IllegalStateException("Instantiating static util class");}
}