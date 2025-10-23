package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.*;
import com.csse3200.game.components.boss.BossLaserAttackComponent;
import com.csse3200.game.components.boss.BossSpawnerComponent;
import com.csse3200.game.components.boss.BossTouchKillComponent;
import com.csse3200.game.components.enemy.BombTrackerComponent;
import com.csse3200.game.components.enemy.PatrolRouteComponent;
import com.csse3200.game.components.enemy.SpawnPositionComponent;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.lighting.ConeDetectorComponent;
import com.csse3200.game.components.npc.BossAnimationController;
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
import box2dLight.RayHandler;

import java.util.ArrayList;

/**
 * Factory for creating enemy and boss entities.
 * Build enemies via dedicated helpers.
 */
public class EnemyFactory {
    private static final EnemyConfigs configs =
            FileLoader.readClass(EnemyConfigs.class, "configs/enemies.json");

    // Light colors for bomber
    private static final Color BOMBER_IDLE_COLOR = new Color(0.5f, 0.5f, 1f, 0.8f);  // Blue-ish
    private static final Color BOMBER_ALERT_COLOR = new Color(1f, 0.3f, 0.3f, 1f);   // Red

    // Drone variants spawned by boss
    public enum DroneVariant {
        SCOUT,   // tiny, low speed
        CHASER,  // regular, middle speed
        BRUTAL   // high speed
    }


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
        BaseEntityConfig config = configs.bomber;
        Entity drone = createBaseEnemy();
        if (spawnPos != null) drone.addComponent(new SpawnPositionComponent(spawnPos));

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/drone.atlas", TextureAtlas.class));
        animator.addAnimation("bidle", 0.15f, Animation.PlayMode.LOOP);
        animator.addAnimation("bscan", 0.15f, Animation.PlayMode.LOOP);
        animator.addAnimation("drop", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("teleBomber", 0.05f, Animation.PlayMode.LOOP);

        drone
                .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
                .addComponent(animator)
                .addComponent(new DroneAnimationController())
                .addComponent(new BombTrackerComponent());

        // Cone light component for downward detection
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
                PhysicsLayer.OBSTACLE,
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
        arc.startAnimation("bidle");

        PhysicsUtils.setScaledCollider(drone, 1f, 0.8f);

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

        // Add patrol task
        AITaskComponent aiComponent = drone.getComponent(AITaskComponent.class);
        aiComponent.addTask(new BombPatrolTask(1f)); // Priority 1 - default behavior

        return drone;
    }

    /**
     * Create an automatic bomber drone that continuously drops bombs while patrolling.
     * The bomber stays in drop animation and drops bombs every second while moving.
     * The bomb drop task briefly takes priority every second to drop a bomb, then
     * immediately yields back to patrol.
     * @param target reference entity (usually player) for bomb direction
     * @param patrolRoute array of waypoints defining the patrol route
     * @return an automatic bombing patrol drone entity
     */
    public static Entity createAutoBomberDrone(Entity target, Vector2[] patrolRoute) {
        BaseEntityConfig config = configs.drone;
        Entity drone = createBaseEnemy();

        // Set spawn position to first patrol point and add patrol route
        if (patrolRoute != null && patrolRoute.length > 0) {
            drone
                    .addComponent(new SpawnPositionComponent(patrolRoute[0]))
                    .addComponent(new PatrolRouteComponent(patrolRoute));
        }

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/drone.atlas", TextureAtlas.class));
        // Only add the drop animation
        animator.addAnimation("drop", 0.2f, Animation.PlayMode.LOOP);

        drone
                .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
                .addComponent(animator)
                // DO NOT add DroneAnimationController - it will override the animation
                .addComponent(new AutoBombDropComponent(target, 2f)) // 2 sec auto bomb drop
                .addComponent(new BombTrackerComponent());

        // AI setup with just patrol
        AITaskComponent aiComponent = drone.getComponent(AITaskComponent.class);
        BombPatrolTask patrolTask = new BombPatrolTask(1f);
        aiComponent.addTask(patrolTask);

        // Start and keep the drop animation
        AnimationRenderComponent arc = drone.getComponent(AnimationRenderComponent.class);
        arc.scaleEntity();
        arc.startAnimation("drop"); // Start in drop animation

        PhysicsUtils.setScaledCollider(drone, 1f, 0.8f);

        return drone;
    }

    /**
     * Create a self-destruction drone variant that chases its target then explodes near the target.
     * @param target entity to pursue and self destruct near
     * @param spawnPos optional spawn position
     * @return a self-destruction drone entity
     */
    public static Entity createSelfDestructionDrone(Entity target, Vector2 spawnPos){
        BaseEntityConfig config = configs.drone;
        Entity drone = createBaseEnemy();
        drone.getComponent(PhysicsMovementComponent.class).setMaxSpeed(3.1f);

        PhysicsComponent physics = drone.getComponent(PhysicsComponent.class);
        physics.setBodyType(BodyDef.BodyType.DynamicBody);

        if (spawnPos != null) drone.addComponent(new SpawnPositionComponent(spawnPos));

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/drone.atlas",TextureAtlas.class));

        animator.addAnimation("angry_float",0.1f,Animation.PlayMode.NORMAL);
        animator.addAnimation("float",0.1f,Animation.PlayMode.NORMAL);
        animator.addAnimation("bomb_effect",0.08f,Animation.PlayMode.NORMAL);
        animator.addAnimation("teleport", 0.05f, Animation.PlayMode.LOOP);

        drone
                .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
                .addComponent(animator)
                .addComponent(new DroneAnimationController());

        // AITasks and selfDestruct behaviour is only added if valid target exists
        if (target != null) {
            drone.addComponent(new SelfDestructComponent(target));

            AITaskComponent aiComponent = drone.getComponent(AITaskComponent.class);
            ChaseTask chaseTask = new ChaseTask(target,10f,2f);
            CooldownTask cooldownTask = new CooldownTask(3f);

            drone.getEvents().addListener("enemyActivated", () -> {
                chaseTask.activate(); // Priority 10
                cooldownTask.activate(); // Priority 5, so chase > cooldown
            });

            aiComponent
                    .addTask(chaseTask)
                    .addTask(cooldownTask);

            // Switch to float anim after teleport
            drone.getEvents().addListener("teleportFinish", () -> drone.getEvents().trigger("wanderStart"));
        }

        animator.scaleEntity();
        animator.startAnimation("float");
        return drone;
    }


    /**
     * Creates a self-destruct drone variant specifically for boss spawning.
     * Minimal behaviour (no teleport or cooldown functionality). The drone is activated
     * immediately and enters a permanent chase state until exploding.
     * @param target entity the player will pursue and explode near
     * @param spawnPos optional spawn position
     * @param atlasPath texture atlas path used for drone animations
     * @param speed max movement speed
     * @param bombEffectFrameDuration per-frame duration for the bomb_effect
     * @return a constructed self-destruct chase drone
     */
    public static Entity createBossSelfDestructDroneInternal(Entity target, Vector2 spawnPos,
                                                             String atlasPath, float speed,
                                                             float bombEffectFrameDuration) {
        BaseEntityConfig config = configs.drone;
        Entity drone = createBaseEnemy();
        //speed set
        drone.getComponent(PhysicsMovementComponent.class).setMaxSpeed(speed);

        //physic
        PhysicsComponent physics = drone.getComponent(PhysicsComponent.class);
        physics.setBodyType(BodyDef.BodyType.DynamicBody);

        TextureAtlas atlas = ServiceLocator.getResourceService().getAsset(atlasPath, TextureAtlas.class);
        AnimationRenderComponent animator = new AnimationRenderComponent(atlas);

        animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("bomb_effect", bombEffectFrameDuration, Animation.PlayMode.NORMAL);

        drone
                .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
                .addComponent(animator)
                .addComponent(new DroneAnimationController());

        // Add self-destruct component
        if (target != null) {
            drone.addComponent(new SelfDestructComponent(target));

            AITaskComponent aiComponent = drone.getComponent(AITaskComponent.class);

            // Only chase task - no cooldown/teleport task
            ChaseTask chaseTask = new ChaseTask(target, 80f, 2f); // Large chase distance

            // Activate immediately and permanently
            chaseTask.activate();

            aiComponent.addTask(chaseTask);

            // Start in permanent chase mode
            drone.getEvents().trigger("enemyActivated");
        }

        // First make sure the required sequence frames are in the atlas, then start the animation
        if (atlas.findRegions("angry_float").size > 0) {
            animator.startAnimation("angry_float");
        } else if (atlas.findRegions("float").size > 0) {
            animator.startAnimation("float");
        } else {
            throw new com.badlogic.gdx.utils.GdxRuntimeException(
                    "Atlas " + atlasPath + " missing required regions: angry_float/float");
        }

        // Set position directly
        if (spawnPos != null) {
            drone.setPosition(spawnPos);
        }

        return drone;
    }

    /**
     * Convenience overload for creating a boss-spawned self-destruct drone with default
     * atlas/speed/effect timings (CHASER-like baseline).
     * @param target entity to chase
     * @param spawnPos optional spawn position
     * @return a constructed self-destruct chase drone
     */
    public static Entity createBossSelfDestructDrone(Entity target, Vector2 spawnPos) {
        // default atlas
        final String DEFAULT_ATLAS = "images/drone.atlas";
        final float DEFAULT_SPEED  = 3.1f;
        final float  DEFAULT_BOMB_FD = 0.08f;
        return createBossSelfDestructDroneInternal(target, spawnPos, DEFAULT_ATLAS, DEFAULT_SPEED,DEFAULT_BOMB_FD);
    }

    /**
     * Variant-aware overload for creating a boss self-destruct drone, Selects atlas,
     * speed and bomb effect timing based on variant. Behaviour remains the same for boss-spawned
     * drones: no teleport or cooldown, immediate chase on activation.
     * @param target entity to chase
     * @param spawnPos optional spawn position
     * @param variant drone variant that determines atlas/speed/effect tuning
     * @return a constructed self-destruct chase drone
     */
    public static Entity createBossSelfDestructDrone(Entity target, Vector2 spawnPos, DroneVariant variant) {
        String atlasPath;
        float speed;
        float bombFD;

        switch (variant) {
            case SCOUT:
                atlasPath = "images/drone_scout.atlas";   //  SCOUT
                speed = 1.5f;
                bombFD  = 0.1f;
                break;
            case BRUTAL:
                atlasPath = "images/drone_brutal.atlas";  // BRUTAL
                speed = 3.8f;
                bombFD  = 0.01f;
                break;
            case CHASER:
            default:
                atlasPath = "images/drone_chaser.atlas";  // CHASER
                speed = 6f;
                bombFD  = 0.1f;
                break;
        }

        return createBossSelfDestructDroneInternal(target, spawnPos, atlasPath, speed, bombFD);
    }


    /**
     * Creates a base enemy entity with a minimal, reusable set of components that all enemies share
     * (physics, movement, collider, hit-box and AI task holder (with no tasks).
     * @return enemy
     */
    private static Entity createBaseEnemy() {
        Entity enemy =
                new Entity()
                        .addComponent(new PhysicsComponent())
                        .addComponent(new PhysicsMovementComponent())
                        .addComponent(new ColliderComponent())
                        .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                        .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER,10f))
                        .addComponent(new AITaskComponent())// Want this empty for base enemies
                        .addComponent(new DeathOnTrapComponent());

        enemy.getComponent(PhysicsMovementComponent.class).setMaxSpeed(1.4f); // Faster movement

        // No gravity so that drones can fly
        PhysicsComponent phys = enemy.getComponent(PhysicsComponent.class);
        Body body = phys.getBody();
        body.setGravityScale(0f);

        PhysicsUtils.setScaledCollider(enemy, 1f, 1f);
        return enemy;
    }

    /**
     * Builds the boss entity for the boss level with core combat/AI/rendering components and a
     * single empty {@link BossSpawnerComponent}. Triggers are configured externally in
     * {@link com.csse3200.game.entities.spawn.Spawners}.
     * - Chases the player
     * - {@link BossLaserAttackComponent} for ranged laser attacks
     * - {@link BossTouchKillComponent} for player death on contact
     * @param target the target entity used by attack and AI components
     * @param spawnPos optional initial world position
     * @return the constructed boss entity
     */
    public static Entity createBossEnemy(Entity target, Vector2 spawnPos) {
        Entity boss = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new PhysicsMovementComponent())
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                .addComponent(new CombatStatsComponent(9999, 100))
                .addComponent(new AITaskComponent())
                .addComponent(new BossAnimationController())
                .addComponent(new BossLaserAttackComponent(target))
                .addComponent(new BossTouchKillComponent(PhysicsLayer.PLAYER));

        // Physics
        boss.getComponent(PhysicsComponent.class).getBody().setGravityScale(0f);
        boss.getComponent(PhysicsMovementComponent.class).setMaxSpeed(3f);

        if (spawnPos != null) boss.addComponent(new SpawnPositionComponent(spawnPos));

        AnimationRenderComponent animator =
                new AnimationRenderComponent(ServiceLocator.getResourceService()
                        .getAsset("images/boss.atlas", TextureAtlas.class));
        animator.addAnimation("bossChase", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("bossGenerateDrone", 0.15f, Animation.PlayMode.LOOP);
        animator.addAnimation("bossTouchKill", 0.1f, Animation.PlayMode.NORMAL);
        animator.addAnimation("bossShootLaser", 0.085f, Animation.PlayMode.NORMAL);
        animator.addAnimation("touchKillEffect", 1f, Animation.PlayMode.NORMAL);
        animator.addAnimation("shootEffect", 1f, Animation.PlayMode.NORMAL);
        boss.addComponent(animator);

        // Render bigger
        boss.scaleHeight(5f);
        float bossX = boss.getScale().x;
        float bossY = boss.getScale().y;

        // Scale hit-box (size of actual boss content)
        boss.getComponent(HitboxComponent.class).setAsBox(
                new Vector2(bossX * 0.5f, bossY * 0.75f), boss.getCenterPosition()
        );

        animator.startAnimation("bossChase");

        BossSpawnerComponent droneSpawner = new  BossSpawnerComponent(new ArrayList<>(), 4f);
        boss.addComponent(droneSpawner);

        return boss;
    }

    private EnemyFactory() {throw new IllegalStateException("Instantiating static util class");}
}