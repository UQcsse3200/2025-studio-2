package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.enemy.PatrolRouteComponent;
import com.csse3200.game.components.enemy.SpawnPositionComponent;
import com.csse3200.game.components.npc.DroneAnimationController;
import com.csse3200.game.components.tasks.*;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.BaseEntityConfig;
import com.csse3200.game.entities.configs.EnemyConfigs;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Factory for creating different types of enemies.
 * - Basic Drone: simple chaser.
 * - Patrolling Drone: patrol + chase + cooldown cycle.
 * - Bomber Drone: chase + bomb dropping.
 */
public class EnemyFactory {
    private static final EnemyConfigs configs =
            FileLoader.readClass(EnemyConfigs.class, "configs/enemies.json");

    /**
     * Creates a basic drone enemy that can chase the player.
     */
    public static Entity createDrone(Entity target, Vector2 spawnPos) {
        BaseEntityConfig config = configs.drone;
        Entity drone = createBaseEnemy();
        if (spawnPos != null) {
            drone.addComponent(new SpawnPositionComponent(spawnPos));
        }

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/drone.atlas", TextureAtlas.class));
        animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);

        drone
                .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
                .addComponent(animator)
                .addComponent(new DroneAnimationController());

        AITaskComponent aiComponent = drone.getComponent(AITaskComponent.class);
        ChaseTask chaseTask = new ChaseTask(target, 5f, 3f);
        CooldownTask cooldownTask = new CooldownTask(3f);

        drone.getEvents().addListener("enemyActivated", () -> {
            chaseTask.activate();
            cooldownTask.activate();
        });

        aiComponent.addTask(chaseTask).addTask(cooldownTask);

        AnimationRenderComponent arc = drone.getComponent(AnimationRenderComponent.class);
        arc.scaleEntity();
        arc.startAnimation("float");

        return drone;
    }

    /**
     * Creates a patrolling drone (patrol → chase → cooldown loop).
     */
    public static Entity createPatrollingDrone(Entity target, Vector2[] patrolRoute) {
        BaseEntityConfig config = configs.patrollingDrone;
        Entity drone = createBaseEnemy();
        if (patrolRoute != null && patrolRoute.length > 0) {
            drone.addComponent(new SpawnPositionComponent(patrolRoute[0]));
            drone.addComponent(new PatrolRouteComponent(patrolRoute));
        }

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/drone.atlas", TextureAtlas.class));
        animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);

        drone
                .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
                .addComponent(animator)
                .addComponent(new DroneAnimationController());

        AITaskComponent aiComponent = drone.getComponent(AITaskComponent.class);

        PatrolTask patrolTask = new PatrolTask(1f);
        ChaseTask chaseTask = new ChaseTask(target, 5f, 3f);
        CooldownTask cooldownTask = new CooldownTask(3f);

        aiComponent.addTask(patrolTask).addTask(chaseTask).addTask(cooldownTask);

        // Event listeners for switching states (tests rely on this)
        drone.getEvents().addListener("enemyActivated", () -> {
            chaseTask.activate();
            cooldownTask.deactivate();
        });
        drone.getEvents().addListener("enemyDeactivated", () -> {
            chaseTask.deactivate();
            cooldownTask.activate();
        });

        AnimationRenderComponent arc = drone.getComponent(AnimationRenderComponent.class);
        arc.scaleEntity();
        arc.startAnimation("float");

        return drone;
    }

    /**
     * Creates a bomber drone (chase + bomb dropping).
     * ⚠️ Note: still using configs.drone for compatibility with EnemyFactoryTest.
     */
    public static Entity createBomberDrone(Entity target, Vector2 spawnPos) {
        BaseEntityConfig config = configs.drone; // test expects drone config, not bomber
        Entity drone = createBaseEnemy();
        if (spawnPos != null) {
            drone.addComponent(new SpawnPositionComponent(spawnPos));
        }

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/drone.atlas", TextureAtlas.class));
        animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("drop", 0.075f, Animation.PlayMode.LOOP);

        drone
                .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
                .addComponent(animator)
                .addComponent(new DroneAnimationController());

        AITaskComponent aiComponent = drone.getComponent(AITaskComponent.class);
        BombChaseTask chaseTask = new BombChaseTask(target, 10, 4f, 7f, 3f, 1.5f, 2f);
        BombDropTask dropTask = new BombDropTask(target, 15, 1.5f, 2f, 3f);
        CooldownTask cooldownTask = new CooldownTask(3);

        drone.getEvents().addListener("enemyActivated", chaseTask::activate);
        drone.getEvents().addListener("enemyDeactivated", () -> {
            chaseTask.deactivate();
            cooldownTask.activate();
        });

        aiComponent.addTask(chaseTask).addTask(dropTask).addTask(cooldownTask);

        AnimationRenderComponent arc = drone.getComponent(AnimationRenderComponent.class);
        arc.scaleEntity();
        arc.startAnimation("float");

        return drone;
    }

    /**
     * Shared base enemy setup.
     */
    private static Entity createBaseEnemy() {
        Entity enemy =
                new Entity()
                        .addComponent(new PhysicsComponent())
                        .addComponent(new PhysicsMovementComponent())
                        .addComponent(new ColliderComponent())
                        .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                        .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1.5f))
                        .addComponent(new AITaskComponent());

        enemy.getComponent(PhysicsMovementComponent.class).setMaxSpeed(1.4f);

        PhysicsComponent phys = enemy.getComponent(PhysicsComponent.class);
        Body body = phys.getBody();
        body.setGravityScale(0f);

        PhysicsUtils.setScaledCollider(enemy, 1f, 1f);
        return enemy;
    }

    private EnemyFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}
