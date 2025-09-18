package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.SelfDestructComponent;
import com.csse3200.game.components.DeathOnTrapComponent;
import com.csse3200.game.components.DisposalComponent;
import com.csse3200.game.components.enemy.PatrolRouteComponent;
import com.csse3200.game.components.enemy.SpawnPositionComponent;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.lighting.ConeDetectorComponent;
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
import box2dLight.RayHandler;

/**
 * Factory for creating different types of enemies.
 * - Base drones pursue the player but are otherwise idle.
 * - Patrolling drones follow a patrol route.
 */
public class EnemyFactory {
    private static final EnemyConfigs configs =
            FileLoader.readClass(EnemyConfigs.class, "configs/enemies.json");

    // Light colors for bomber
    private static final Color BOMBER_IDLE_COLOR = new Color(0.5f, 0.5f, 1f, 0.8f);  // Blue-ish
    private static final Color BOMBER_ALERT_COLOR = new Color(1f, 0.3f, 0.3f, 1f);   // Red

    /**
     * Creates a drone enemy that starts idle. When activated by a security camera, starts chasing its target.
     * Has drone-specific animation, combat stats and chase task.
     * @param target that drone pursues when chasing
     * @param spawnPos the starting world position of the enemy
     * @return drone enemy entity
     */
    public static Entity createDrone(Entity target, Vector2 spawnPos) {
        BaseEntityConfig config = configs.drone;
        Entity drone = createBaseEnemy();
        if (spawnPos != null) drone.addComponent(new SpawnPositionComponent(spawnPos));

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/drone.atlas", TextureAtlas.class));
        animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("teleport", 0.05f, Animation.PlayMode.LOOP);


        drone
                .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
                .addComponent(animator)
                .addComponent(new DroneAnimationController());

        AITaskComponent aiComponent = drone.getComponent(AITaskComponent.class);
        ChaseTask chaseTask = new ChaseTask(target, 5f, 3f);
        CooldownTask cooldownTask = new CooldownTask(3f);

        // ENEMY ACTIVATION
        drone.getEvents().addListener("enemyActivated", () -> {
            chaseTask.activate(); // Priority 10
            cooldownTask.activate(); // Priority 5, so chase > cooldown
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
     * Same as basic drone enemy but patrols a given route, alternatively chasing a target when activated.
     * @param target that drone pursues when chasing
     * @param patrolRoute contains list of waypoints in patrol route
     * @return a patrolling drone enemy entity
     */
    public static Entity createPatrollingDrone(Entity target, Vector2[] patrolRoute) {
        Entity drone = createDrone(target, patrolRoute[0]);
        drone.addComponent(new PatrolRouteComponent(patrolRoute));

        AITaskComponent aiComponent = drone.getComponent(AITaskComponent.class);

        aiComponent.addTask(new PatrolTask(1f));

        return drone;
    }

    /**
     * Create a bomber-style drone with integrated cone light detection.
     * The bomber uses a downward-facing cone light to detect and bomb targets below.
     * @param target that drone pursues when bombing/chasing
     * @param spawnPos the starting world position of the enemy
     * @param bomberId unique ID for this bomber (used for light registration)
     * @return a bomber drone entity with light detection
     */
    public static Entity createBomberDrone(Entity target, Vector2 spawnPos, String bomberId) {
        BaseEntityConfig config = configs.drone;
        Entity drone = createBaseEnemy();
        if (spawnPos != null) drone.addComponent(new SpawnPositionComponent(spawnPos));

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/drone.atlas", TextureAtlas.class));
        animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("drop", 0.075f, Animation.PlayMode.LOOP);
        animator.addAnimation("teleBomber", 0.05f, Animation.PlayMode.LOOP);

        drone
                .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
                .addComponent(animator)
                .addComponent(new DroneAnimationController());

        // Add cone light for downward detection
        RayHandler rayHandler = ServiceLocator.getLightingService().getEngine().getRayHandler();
        ConeLightComponent lightComponent = new ConeLightComponent(
                rayHandler,
                100,              // rays
                BOMBER_IDLE_COLOR, // initial color (blue)
                6f,               // distance
                -90f,             // direction (straight down)
                60f               // cone degree
        );
        lightComponent.setFollowEntity(true);
        drone.addComponent(lightComponent);

        // Add cone detector for target detection
        ConeDetectorComponent detectorComponent = new ConeDetectorComponent(
                target,
                PhysicsLayer.OBSTACLE,  // occluder mask
                "bomber_" + bomberId     // unique ID
        );
        drone.addComponent(detectorComponent);

        // AI setup with light-aware tasks
        AITaskComponent aiComponent = drone.getComponent(AITaskComponent.class);

        // Create tasks that work with light detection
        BombChaseTask chaseTask = new BombChaseTask(
                target,
                10,    // Priority
                8f,    // Max chase distance
                3f,    // Optimal height for bombing
                0.5f   // Height tolerance
        );

        BombDropTask dropTask = new BombDropTask(
                target,
                15,    // Priority (highest)
                2f,    // Cooldown between drops
                3f,    // Optimal height
                0.5f   // Height tolerance
        );

        CooldownTask cooldownTask = new CooldownTask(3f, "teleBomber");

        // Wire up detection events
        drone.getEvents().addListener("targetDetected", (Entity detectedTarget) -> {
            // Change light color to alert
            lightComponent.setColor(BOMBER_ALERT_COLOR);
            // Activate chase and deactivate cooldown
            chaseTask.activate();
            cooldownTask.deactivate();
        });

        drone.getEvents().addListener("targetLost", (Entity lostTarget) -> {
            // Return light to idle color
            lightComponent.setColor(BOMBER_IDLE_COLOR);
            // Deactivate chase and activate cooldown for teleport
            chaseTask.deactivate();
            cooldownTask.activate();
        });

        // Add tasks in priority order (highest to lowest)
        aiComponent
                .addTask(dropTask)      // Priority 15 when at position with target detected
                .addTask(chaseTask)     // Priority 10 when target detected
                .addTask(cooldownTask); // Priority 5 for reset after losing target

        AnimationRenderComponent arc = drone.getComponent(AnimationRenderComponent.class);
        arc.scaleEntity();
        arc.startAnimation("float");

        return drone;
    }

    /**
     * Create a patrolling bomber drone with integrated light detection.
     * The bomber patrols a route and uses its cone light to detect targets below.
     * @param target that drone pursues when bombing/chasing
     * @param patrolRoute array of waypoints defining the patrol route
     * @param bomberId unique ID for this bomber
     * @return a patrolling bomber drone entity
     */
    public static Entity createPatrollingBomberDrone(Entity target, Vector2[] patrolRoute, String bomberId) {
        // Create bomber drone at first patrol point
        Entity drone = createBomberDrone(target, patrolRoute[0], bomberId);

        // Add patrol route component
        drone.addComponent(new PatrolRouteComponent(patrolRoute));

        // Add patrol task with lowest priority
        AITaskComponent aiComponent = drone.getComponent(AITaskComponent.class);
        aiComponent.addTask(new PatrolTask(1f)); // Priority 1 - default behavior

        return drone;
    }
    public static Entity createSelfDestructionDrone(Entity target, Vector2 spawnPos){
        BaseEntityConfig config = configs.drone;
        Entity drone= createBaseEnemy();
        drone.getComponent(PhysicsMovementComponent.class).setMaxSpeed(1.8f);

        //Explicitly ensure DynamicBody only if not already set by createBaseEnemy
        PhysicsComponent physics = drone.getComponent(PhysicsComponent.class);
        if (physics.getBody()!= null && physics.getBody().getType()!= BodyDef.BodyType.DynamicBody) {
            physics.setBodyType(BodyDef.BodyType.DynamicBody);
        }
        //add spawn if not provide
        if(spawnPos!= null)drone.addComponent(new SpawnPositionComponent(spawnPos));

        AnimationRenderComponent animator=
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/drone.atlas",TextureAtlas.class));
        animator.addAnimation("angry_float",0.1f,Animation.PlayMode.NORMAL);
        animator.addAnimation("float",0.1f,Animation.PlayMode.NORMAL);

        animator.addAnimation("bomb_effect",0.08f,Animation.PlayMode.NORMAL);

        drone
                .addComponent(new CombatStatsComponent(config.health,config.baseAttack))
                .addComponent(animator)
                .addComponent(new DroneAnimationController());
        //AITasks and selfDestruct behaviour is only added if valid target exists
        if (target!=null){
            drone.addComponent(new SelfDestructComponent(target));

            AITaskComponent aiComponent=drone.getComponent(AITaskComponent.class);
            ChaseTask chaseTask= new ChaseTask(target,10f,2f);
            aiComponent.addTask(chaseTask);
            chaseTask.activate();
        }
//
        animator.scaleEntity();
        animator.startAnimation("float");
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
                        .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER,40f))
                        .addComponent(new AITaskComponent())// Want this empty for base enemies
                        .addComponent(new DeathOnTrapComponent())
                        .addComponent(new DisposalComponent(0.5f));

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