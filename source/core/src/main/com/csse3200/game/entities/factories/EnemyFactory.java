package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.SelfDestructComponent;
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
     * @param spawnPos the starting world position of the enemy
     * @return drone enemy entity
     */
    public static Entity createDrone(Entity target, Vector2 spawnPos, Entity securityLight) {
        BaseEntityConfig config = configs.drone;
        Entity drone = createBaseEnemy();
        if (spawnPos != null) drone.addComponent(new SpawnPositionComponent(spawnPos)); // For resets

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
        ChaseTask chaseTask = new ChaseTask(target);
        CooldownTask cooldownTask = new CooldownTask(5f);

        // FOR LIGHT-GATED ENEMY CHASING
        securityLight.getEvents().addListener("targetDetected", entity -> {
            chaseTask.activate();
            cooldownTask.deactivate();
        });
        securityLight.getEvents().addListener("targetLost", entity -> {
            chaseTask.deactivate();
            cooldownTask.activate();
        });

        aiComponent
                .addTask(chaseTask)
                .addTask(cooldownTask);

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
    public static Entity createPatrollingDrone(Entity target, Vector2[] patrolRoute, Entity securityLight) {
        Entity drone = createDrone(target, patrolRoute[0], securityLight);
        drone.addComponent(new PatrolRouteComponent(patrolRoute));

        AITaskComponent aiComponent = drone.getComponent(AITaskComponent.class);

        aiComponent.addTask(new PatrolTask(1f));

        return drone;
    }

    /**
     * Create a bomber-style drone enemy that chases the target and drops bombs when target is
     * directly below (within a certain range).
     * @param target that drone pursues when bombing/chasing
     * @param spawnPos the starting world position of the enemy
     * @return a bomber drone entity
     */
    public static Entity createBomberDrone(Entity target, Vector2 spawnPos, Entity securityLight) {
        BaseEntityConfig config = configs.drone;
        Entity drone = createBaseEnemy();
        if (spawnPos != null) drone.addComponent(new SpawnPositionComponent(spawnPos));

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/drone.atlas", TextureAtlas.class));
        animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("drop", 0.075f, Animation.PlayMode.LOOP); // Attack animation

        drone
                .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
                .addComponent(animator)
                .addComponent(new DroneAnimationController());

        // AI setup
        AITaskComponent aiComponent = drone.getComponent(AITaskComponent.class);

        BombChaseTask chaseTask = new BombChaseTask(target, 10, 4f, 7f, 3f, 1.5f, 2f);
        BombDropTask dropTask = new BombDropTask(target, 15, 1.5f, 2f, 3f);
        CooldownTask cooldownTask = new CooldownTask(3);

        // SECURITY LIGHT INTEGRATION
        // When security light detects player, activate the chase task
        securityLight.getEvents().addListener("targetDetected", entity -> chaseTask.activate());

        // When chase ends, activate cooldown
        drone.getEvents().addListener("chaseEnd", cooldownTask::activate);
        // Add tasks to AI
        aiComponent
                .addTask(chaseTask)
                .addTask(dropTask)
                .addTask(cooldownTask);

        AnimationRenderComponent arc = drone.getComponent(AnimationRenderComponent.class);
        arc.scaleEntity();
        arc.startAnimation("float");

        return drone;
    }

    public static Entity createSelfDestructionDrone(Entity target, Vector2 spawnPos, Entity securityLight){
        BaseEntityConfig config = configs.drone;
        Entity drone= createBaseEnemy();
        drone.getComponent(PhysicsMovementComponent.class).setMaxSpeed(1.8f);
        if(spawnPos!= null)drone.addComponent(new SpawnPositionComponent(spawnPos));

        AnimationRenderComponent animator=
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/drone.atlas",TextureAtlas.class));
        animator.addAnimation("angry_float",0.1f,Animation.PlayMode.LOOP);
        animator.addAnimation("float",0.1f,Animation.PlayMode.LOOP);

        animator.addAnimation("explode",0.08f,Animation.PlayMode.LOOP);

        drone
                .addComponent(new CombatStatsComponent(config.health,config.baseAttack))
                .addComponent(animator)
                .addComponent(new DroneAnimationController())
                .addComponent(new SelfDestructComponent(target));

        AITaskComponent aiComponent=drone.getComponent(AITaskComponent.class);
        ChaseTask chaseTask= new ChaseTask(target,10,3f,4f);

        securityLight.getEvents().addListener("targetDetected", entity->chaseTask.activate());

        aiComponent.addTask(chaseTask);

        AnimationRenderComponent arc= drone.getComponent(AnimationRenderComponent.class);
        arc.scaleEntity();
        arc.startAnimation("float");
        return drone;


    }

    /**
     * Creates a base enemy entity with a minimal, reusable set of components that all enemies share
     * (physics, movement, collider, hitbox and AI task holder (with no tasks).
     * @return enemy
     */
    private static Entity createBaseEnemy() {
        Entity enemy =
                new Entity()
                        .addComponent(new PhysicsComponent())
                        .addComponent(new PhysicsMovementComponent())
                        .addComponent(new ColliderComponent())
                        .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                        .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER,1.5f))
                        .addComponent(new AITaskComponent()); // Want this empty for base enemies

        enemy.getComponent(PhysicsMovementComponent.class).setMaxSpeed(1.4f); // Faster movement

        // No gravity so that drones can fly
        PhysicsComponent phys = enemy.getComponent(PhysicsComponent.class);
        Body body = phys.getBody();
        body.setGravityScale(0f);

        PhysicsUtils.setScaledCollider(enemy, 1f, 1f);
        return enemy;
    }

    private EnemyFactory() {throw new IllegalStateException("Instantiating static util class");}
}