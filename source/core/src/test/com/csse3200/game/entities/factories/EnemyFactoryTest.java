package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.enemy.PatrolRouteComponent;
import com.csse3200.game.components.npc.DroneAnimationController;
import com.csse3200.game.components.npc.GhostAnimationController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.configs.BaseEntityConfig;
import com.csse3200.game.entities.configs.EnemyConfigs;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
public class EnemyFactoryTest {
    private BaseEntityConfig droneConfig;
    private ResourceService rs;

    @BeforeEach
    void setUp() {
        EnemyConfigs configs = FileLoader.readClass(EnemyConfigs.class, "configs/enemies.json");
        droneConfig = configs.drone;

        // Register services needed for entities
        ServiceLocator.registerPhysicsService(new PhysicsService());
        ServiceLocator.registerRenderService(new RenderService());
        ServiceLocator.registerEntityService(new EntityService());
        // Resource Service for loading atlases
        rs = new ResourceService();
        ServiceLocator.registerResourceService(rs);
        rs.loadTextureAtlases(new String[]{"images/drone.atlas"});
        rs.loadAll();
    }

    @AfterEach
    void cleanUp() {
        rs.unloadAssets(new String[]{"images/drone.atlas"});
        rs.dispose();
        ServiceLocator.clear();
    }

    @Test
    void createDrone_hasBaseEnemyComponents() {
        Entity drone = EnemyFactory.createDrone(new Entity());
        ServiceLocator.getEntityService().register(drone);

        assertNotNull(drone.getComponent(PhysicsComponent.class),
                "Drone should have a PhysicsComponent");
        assertNotNull(drone.getComponent(PhysicsMovementComponent.class),
                "Drone should have a PhysicsMovementComponent");
        assertNotNull(drone.getComponent(ColliderComponent.class),
                "Drone should have a ColliderComponent.class");
        assertNotNull(drone.getComponent(HitboxComponent.class),
                "Drone should have a HitboxComponent");
        assertEquals(drone.getComponent(HitboxComponent.class).getLayer(), PhysicsLayer.NPC,
                "Drone PhysicsLayer should be NPC");
        assertNotNull(drone.getComponent(AITaskComponent.class),
                "Drone should have an AITaskComponent");
    }

    @Test
    void createDrone_hasCombatStatsComponent() {
        Entity drone = EnemyFactory.createDrone(new Entity());
        ServiceLocator.getEntityService().register(drone);

        CombatStatsComponent stats = drone.getComponent(CombatStatsComponent.class);
        assertNotNull(stats, "Drone should have a CombatStatsComponent");
        assertEquals(droneConfig.health, stats.getHealth(), "Drone health mismatch");
        assertEquals(droneConfig.baseAttack, stats.getBaseAttack(), "Drone baseAttack mismatch");
    }

    @Test
    void createDrone_hasAnimations() {
        Entity drone = EnemyFactory.createDrone(new Entity());
        ServiceLocator.getEntityService().register(drone);

        // TODO: Update for drone-specific animations
        AnimationRenderComponent anim = drone.getComponent(AnimationRenderComponent.class);
        assertNotNull(anim, "Drone should have an AnimationRenderComponent");
        assertTrue(anim.hasAnimation("float"), "Missing 'float' animation");
        assertTrue(anim.hasAnimation("angry_float"), "Missing 'angry_float' animation");
    }

    @Test
    void createDrone_hasAnimationController() {
        Entity drone = EnemyFactory.createDrone(new Entity());
        ServiceLocator.getEntityService().register(drone);

        // TODO: Update for drone-specific animation controller
        assertNotNull(drone.getComponent(DroneAnimationController.class),
                "Drone should have AnimationController");
    }

    @Test
    void createDrone_isIdempotent() {
        Entity a = EnemyFactory.createDrone(new Entity());
        Entity b = EnemyFactory.createDrone(new Entity());
        assertNotSame(a, b, "Drones should be distinct");

        AITaskComponent ai_a = a.getComponent(AITaskComponent.class);
        AITaskComponent ai_b = b.getComponent(AITaskComponent.class);
        assertNotSame(ai_a, ai_b,
            "Drones should have distinct AITaskComponents");
    }

    @Test
    void patrollingDroneHasPatrolRouteComponent() {
        Entity patrolDrone = EnemyFactory.createPatrollingDrone(
                new Entity(),
                new Vector2(5, 5),
                new Vector2[0]
        );
        assertNotNull(patrolDrone.getComponent(PatrolRouteComponent.class),
                "Patrolling drone should have a PatrolRouteComponent");
    }

    @Test
    void createPatrollingDrone_emptySteps() {
        Entity drone = assertDoesNotThrow(
                () -> EnemyFactory.createPatrollingDrone(
                        new Entity(),
                        new Vector2(5, 5),
                        new Vector2[0]
                ), "Factory should not throw when steps array is empty");
        assertNotNull(drone);
    }
}