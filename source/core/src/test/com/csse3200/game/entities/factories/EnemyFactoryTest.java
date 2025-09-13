package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.enemy.PatrolRouteComponent;
import com.csse3200.game.components.enemy.SpawnPositionComponent;
import com.csse3200.game.components.SelfDestructComponent;
import com.csse3200.game.components.npc.DroneAnimationController;
import com.csse3200.game.components.npc.SelfDestructionDroneAnimation;
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
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class EnemyFactoryTest {
    private BaseEntityConfig droneConfig;
    private ResourceService rs;
    private GameTime gameTime;

    @BeforeEach
    void setUp() {
        EnemyConfigs configs = FileLoader.readClass(EnemyConfigs.class, "configs/enemies.json");
        droneConfig = configs.drone;

        // Register services needed for entities
        ServiceLocator.registerPhysicsService(new PhysicsService());
        ServiceLocator.registerRenderService(new RenderService());
        ServiceLocator.registerEntityService(new EntityService());

        rs = new ResourceService();
        ServiceLocator.registerResourceService(rs);
        rs.loadTextureAtlases(new String[]{"images/drone.atlas"});
        rs.loadAll();

        // Register time source needed for AI tasks
        gameTime = mock(GameTime.class);
        ServiceLocator.registerTimeSource(gameTime);
    }

    @AfterEach
    void cleanUp() {
        rs.unloadAssets(new String[]{"images/drone.atlas"});
        rs.dispose();
        ServiceLocator.clear();
    }

    @Test
    void createDrone_hasBaseEnemyComponents() {
        Entity player = new Entity();
        Entity drone = EnemyFactory.createDrone(player, new Vector2(0f, 0f));
        ServiceLocator.getEntityService().register(drone);

        assertNotNull(drone.getComponent(PhysicsComponent.class), "Drone should have a PhysicsComponent");
        assertNotNull(drone.getComponent(PhysicsMovementComponent.class), "Drone should have a PhysicsMovementComponent");
        assertNotNull(drone.getComponent(ColliderComponent.class), "Drone should have a ColliderComponent.class");
        assertNotNull(drone.getComponent(HitboxComponent.class), "Drone should have a HitboxComponent");
        assertEquals(PhysicsLayer.NPC, drone.getComponent(HitboxComponent.class).getLayer(), "Drone PhysicsLayer should be NPC");
        assertNotNull(drone.getComponent(AITaskComponent.class), "Drone should have an AITaskComponent");
    }

    @Test
    void createDrone_hasCorrectCombatStats() {
        Entity player = new Entity();
        Entity drone = EnemyFactory.createDrone(player, new Vector2(0f, 0f));
        ServiceLocator.getEntityService().register(drone);
        CombatStatsComponent stats = drone.getComponent(CombatStatsComponent.class);
        assertNotNull(stats, "Drone should have a CombatStatsComponent");
        assertEquals(droneConfig.health, stats.getHealth(), "Drone health mismatch");
        assertEquals(droneConfig.baseAttack, stats.getBaseAttack(), "Drone baseAttack mismatch");
    }

    @Test
    void createDrone_hasAnimations() {
        Entity player = new Entity();
        Entity drone = EnemyFactory.createDrone(player, new Vector2(0f, 0f));
        ServiceLocator.getEntityService().register(drone);

        AnimationRenderComponent anim = drone.getComponent(AnimationRenderComponent.class);
        assertNotNull(anim, "Drone should have an AnimationRenderComponent");
        assertTrue(anim.hasAnimation("float"), "Missing 'float' animation");
        assertTrue(anim.hasAnimation("angry_float"), "Missing 'angry_float' animation");
    }

    @Test
    void createDrone_hasAnimationController() {
        Entity player = new Entity();
        Entity drone = EnemyFactory.createDrone(player, new Vector2(0f, 0f));
        ServiceLocator.getEntityService().register(drone);
        assertNotNull(drone.getComponent(DroneAnimationController.class), "Drone should have AnimationController");
    }

    @Test
    void createDrone_returnsDistinct() {
        Entity securityLight = new Entity();
        Entity player = new Entity();

        Entity a = EnemyFactory.createDrone(player, new Vector2(0f, 0f));
        Entity b = EnemyFactory.createDrone(player, new Vector2(0f, 0f));

        assertNotSame(a, b, "Drones should be distinct");
        assertNotSame(a.getComponent(AITaskComponent.class), b.getComponent(AITaskComponent.class), "Drones should have distinct AITaskComponents");
    }

    @Test
    void createDrone_addsSpawnPosition() {
        Vector2 start = new Vector2(0, 0);
        Entity drone = EnemyFactory.createDrone(new Entity(), start);
        SpawnPositionComponent sp = drone.getComponent(SpawnPositionComponent.class);
        assertNotNull(sp);
        assertEquals(start, sp.getSpawnPos(),
                "Should have SpawnPositionComponent correctly initialised");
    }

    @Test
    void createDrone_doesNotAddNullSpawnPos() {
        Entity drone = EnemyFactory.createDrone(new Entity(), null);
        assertNull(drone.getComponent(SpawnPositionComponent.class),
                "No SpawnPositionComponent when initialised with null spawnPos");
    }

    @Test
    void patrollingDroneHasPatrolRouteComponent() {
        Entity patrolDrone = EnemyFactory.createPatrollingDrone(
                new Entity(),
                new Vector2[]{new Vector2(5f, 5f)}
        );
        assertNotNull(patrolDrone.getComponent(PatrolRouteComponent.class),
                "Patrolling drone should have a PatrolRouteComponent");
    }

    @Test
    void createPatrollingDrone_emptySteps() {
        Entity player = new Entity();
        Entity securityLight = new Entity();
        Entity drone = assertDoesNotThrow(
                () -> EnemyFactory.createPatrollingDrone(
                        player,
                        new Vector2[]{new Vector2(0f, 0f)}
                ), "Factory should not throw when steps array is empty");
        assertNotNull(drone);
    }

    @Test
    void patrollingDrone_storesWaypointsInOrder() {
        Vector2[] route = {new Vector2(0, 0), new Vector2(1, 1), new Vector2(2, 2)};
        Entity patrolDrone = EnemyFactory.createPatrollingDrone(new Entity(), route);
        PatrolRouteComponent pr = patrolDrone.getComponent(PatrolRouteComponent.class);
        assertNotNull(pr);

        Vector2[] stored = pr.getWaypoints();
        assertEquals(route.length, stored.length);
        for (int i = 0; i < route.length; i++) {
            assertEquals(route[i], stored[i]);
        }
    }

    @Test
    void patrollingDrone_hasSpawnPosition() {
        Vector2[] route = {new Vector2(0, 0), new Vector2(1, 1)};
        Entity patrolDrone = EnemyFactory.createPatrollingDrone(new Entity(), route);

        SpawnPositionComponent sp = patrolDrone.getComponent(SpawnPositionComponent.class);
        assertNotNull(sp);
        assertEquals(route[0], sp.getSpawnPos());
    }

    @Test
    void createBomberDrone_hasBaseEnemyComponents() {
        Entity bomberDrone = EnemyFactory.createBomberDrone(new Entity(), new Vector2(0f, 0f));
        ServiceLocator.getEntityService().register(bomberDrone);

        assertNotNull(bomberDrone.getComponent(PhysicsComponent.class),
                "Drone should have a PhysicsComponent");
        assertNotNull(bomberDrone.getComponent(PhysicsMovementComponent.class),
                "Drone should have a PhysicsMovementComponent");
        assertNotNull(bomberDrone.getComponent(ColliderComponent.class),
                "Drone should have a ColliderComponent.class");
        assertNotNull(bomberDrone.getComponent(HitboxComponent.class),
                "Drone should have a HitboxComponent");
        assertEquals(PhysicsLayer.NPC, bomberDrone.getComponent(HitboxComponent.class).getLayer(),
                "Drone PhysicsLayer should be NPC");
        assertNotNull(bomberDrone.getComponent(AITaskComponent.class),
                "Drone should have an AITaskComponent");
    }

    @Test
    void createBomberDrone_hasAnimations() {
        Entity bomberDrone = EnemyFactory.createBomberDrone(new Entity(), new Vector2(0f, 0f));
        ServiceLocator.getEntityService().register(bomberDrone);

        AnimationRenderComponent anim = bomberDrone.getComponent(AnimationRenderComponent.class);
        assertNotNull(anim, "Drone should have an AnimationRenderComponent");
        assertTrue(anim.hasAnimation("float"), "Missing 'float' animation");
        assertTrue(anim.hasAnimation("angry_float"), "Missing 'angry_float' animation");
        assertTrue(anim.hasAnimation("drop"), "Missing 'drop' animation");
    }

    @Test
    void createBomberDrone_hasAnimationController() {
            Entity bomberDrone = EnemyFactory.createBomberDrone(new Entity(), new Vector2(0f, 0f));
            ServiceLocator.getEntityService().register(bomberDrone);

            assertNotNull(bomberDrone.getComponent(DroneAnimationController.class),
                    "Drone should have AnimationController");
    }

    @Test
    void bomberDrone_startsOnFloatAnim() {
        Entity bomberDrone = EnemyFactory.createBomberDrone(new Entity(), new Vector2(0f, 0f));
        AnimationRenderComponent arc = bomberDrone.getComponent(AnimationRenderComponent.class);
        assertEquals("float", arc.getCurrentAnimation());
        assertTrue(arc.hasAnimation("float"));
    }

    @Test
    void createBomberDrone_hasCorrectCombatStats() {
        Entity bomberDrone = EnemyFactory.createBomberDrone(new Entity(), new Vector2(0f, 0f));
        ServiceLocator.getEntityService().register(bomberDrone);

        CombatStatsComponent stats = bomberDrone.getComponent(CombatStatsComponent.class);
        assertNotNull(stats, "Drone should have a CombatStatsComponent");
        assertEquals(droneConfig.health, stats.getHealth(), "Drone health mismatch");
        assertEquals(droneConfig.baseAttack, stats.getBaseAttack(), "Drone baseAttack mismatch");
    }

    @Test
    void createBomberDrone_addsSpawnPosition() {
        Vector2 start = new Vector2(0, 0);
        Entity bomberDrone = EnemyFactory.createBomberDrone(new Entity(), start);
        SpawnPositionComponent sp = bomberDrone.getComponent(SpawnPositionComponent.class);
        assertNotNull(sp);
        assertEquals(start, sp.getSpawnPos(),
                "Should have SpawnPositionComponent correctly initialised");
    }

    @Test
    void createBomberDrone_doesNotAddNullSpawnPos() {
        Entity bomberDrone = EnemyFactory.createBomberDrone(new Entity(), null);
        assertNull(bomberDrone.getComponent(SpawnPositionComponent.class),
                "No SpawnPositionComponent when initialised with null spawnPos");
    }

    @Test
    void createBomberDrone_returnsDistinct() {
        Entity a = EnemyFactory.createBomberDrone(new Entity(), new Vector2(0f, 0f));
        Entity b = EnemyFactory.createBomberDrone(new Entity(), new Vector2(0f, 0f));
        assertNotSame(a, b, "Drones should be distinct");

        AITaskComponent ai_a = a.getComponent(AITaskComponent.class);
        AITaskComponent ai_b = b.getComponent(AITaskComponent.class);
        assertNotSame(ai_a, ai_b,
                "Drones should have distinct AITaskComponents");
    }


    // Tests to verify correct AI task flow
    @Test
    void patrolDrone_patrolToChaseFlow() {
        Entity target = createEntityWithPosition();
        Vector2[] route = {new Vector2(0, 0), new Vector2(1, 0)};
        Entity light = new Entity();

        Entity drone = EnemyFactory.createPatrollingDrone(target, route);
        AITaskComponent ai = drone.getComponent(AITaskComponent.class);
        drone.create();

        List<String> eventLog = new ArrayList<>();
        drone.getEvents().addListener("patrolStart", () -> eventLog.add("patrolStart"));
        drone.getEvents().addListener("patrolEnd", () -> eventLog.add("patrolEnd"));
        drone.getEvents().addListener("chaseStart", () -> eventLog.add("chaseStart"));

        ai.update(); // Patrolling

        // Simulate camera -> enemy activation
        drone.getEvents().trigger("enemyActivated");
        ai.update(); // Chasing

        assertEquals(List.of("patrolStart", "patrolEnd", "chaseStart"), eventLog);
    }

    @Test
    void patrolDrone_chaseToCooldownFlow() {
        Entity target = createEntityWithPosition();
        Vector2[] route = {new Vector2(0, 0), new Vector2(1, 0)};
        Entity light = new Entity();

        Entity drone = EnemyFactory.createPatrollingDrone(target, route);
        AITaskComponent ai = drone.getComponent(AITaskComponent.class);
        drone.create();

        List<String> eventLog = new ArrayList<>();
        drone.getEvents().addListener("chaseStart", () -> eventLog.add("chaseStart"));
        drone.getEvents().addListener("chaseEnd", () -> eventLog.add("chaseEnd"));
        drone.getEvents().addListener("cooldownStart", () -> eventLog.add("cooldownStart"));

        ai.update(); // Patrolling
        drone.getEvents().trigger("enemyActivated");
        ai.update(); // Chasing
        drone.getEvents().trigger("enemyDeactivated");
        ai.update(); // Cooldown

        assertEquals(List.of("chaseStart", "chaseEnd", "cooldownStart"), eventLog);
    }

    @Test
    void patrolDrone_cooldownToPatrolFlow() {
        Entity target = createEntityWithPosition();
        Vector2[] route = {new Vector2(0, 0), new Vector2(1, 0)};
        Entity light = new Entity();

        Entity drone = EnemyFactory.createPatrollingDrone(target, route);
        AITaskComponent ai = drone.getComponent(AITaskComponent.class);
        drone.create();

        List<String> eventLog = new ArrayList<>();
        drone.getEvents().addListener("cooldownStart", () -> eventLog.add("cooldownStart"));
        drone.getEvents().addListener("cooldownEnd", () -> eventLog.add("cooldownEnd"));
        drone.getEvents().addListener("patrolStart", () -> eventLog.add("patrolStart"));

        drone.getEvents().trigger("enemyDeactivated");

        when(gameTime.getTime()).thenReturn(0L, 5000L);

        ai.update(); // Cooldown
        ai.update(); // Finish cooldown
        ai.update(); // Patrol

        assertEquals(List.of("cooldownStart", "cooldownEnd",  "patrolStart"), eventLog);
    }

    @Test
    void patrolDrone_cooldownToChaseFlow() {
        Entity target = createEntityWithPosition();
        Vector2[] route = {new Vector2(0, 0), new Vector2(1, 0)};
        Entity light = new Entity();

        Entity drone = EnemyFactory.createPatrollingDrone(target, route);
        AITaskComponent ai = drone.getComponent(AITaskComponent.class);
        drone.create();

        List<String> eventLog = new ArrayList<>();
        drone.getEvents().addListener("cooldownStart", () -> eventLog.add("cooldownStart"));
        drone.getEvents().addListener("cooldownEnd", () -> eventLog.add("cooldownEnd"));
        drone.getEvents().addListener("chaseStart", () -> eventLog.add("chaseStart"));
        drone.getEvents().addListener("patrolStart", () -> eventLog.add("patrolStart"));

        drone.getEvents().trigger("enemyDeactivated");
        ai.update(); // Cooldown

        // Advance cooldown (but not finished) need time sequence for wait subtask
        when(gameTime.getTime()).thenReturn(4000L);
        ai.update(); // Still in cooldown

        drone.getEvents().trigger("enemyActivated");
        ai.update(); // Chasing

        assertEquals(List.of("cooldownStart", "cooldownEnd", "chaseStart"), eventLog);
    }
    @Test
    void SelfDestructDrone_hasBaseEnemyComponent(){
        Entity player = new Entity();
        Entity securityLight = new Entity();
        Entity drone = EnemyFactory.createSelfDestructDrone(player, new Vector2(0f, 0f));
        ServiceLocator.getEntityService().register(drone);

        assertNotNull(drone.getComponent(PhysicsComponent.class), "SelfDestructDrone should have a PhysicsComponent");
        assertNotNull(drone.getComponent(PhysicsMovementComponent.class), "SelfDestructDrone should have a physicsMovementComponent");
        assertNotNull(drone.getComponent(ColliderComponent.class), "SelfDestructDrone should have a ColliderComponent.class");
        assertNotNull(drone.getComponent(HitboxComponent.class),"SelfDestructDrone should have a HitboxComponent.class");
        assertEquals(PhysicsLayer.NPC,drone.getComponent(HitboxComponent.class).getLayer(),"SelfDestructDrone PhysicsLayer should be NPC");
        assertNotNull(drone.getComponent(AITaskComponent.class),"SelfDestructDrone should have an AITaskComponent.class");
    }
    @Test
    void createSelfDestructDrone_hasAnimations(){
        Entity player = new Entity();
        Entity securityLight = new Entity();
        Entity drone =EnemyFactory.createSelfDestructDrone(player,new  Vector2(0f, 0f));

        AnimationRenderComponent anim = drone.getComponent(AnimationRenderComponent.class);
        assertNotNull(anim, "AnimationRenderComponent should have a AnimationRenderComponent.class");
        assertTrue(anim.hasAnimation("flying"),"Missing 'flying' animation");
        assertTrue(anim.hasAnimation("self_destruct"),"Missing 'self_destruct' animation");
    }
    @Test
    void createSelfDestructDrone_hasAnimationController(){
        Entity Player = new Entity();
        Entity securityLight = new Entity();
        Entity drone = EnemyFactory.createSelfDestructDrone(Player,new  Vector2(0f, 0f));

        assertNotNull(drone.getComponent(AnimationRenderComponent.class), "AnimationRenderComponent should have a AnimationRenderComponent.class");
    }
    @Test
    void createSelfDestructDrone_addsSpawnPosition(){
        Vector2 spawnPos = new Vector2(5f,5f);
        Entity drone = EnemyFactory.createSelfDestructDrone(new Entity(),spawnPos);

        SpawnPositionComponent sp = drone.getComponent(SpawnPositionComponent.class);
        assertNotNull(sp, "SpawnPositionComponent should have a SpawnPositionComponent.class");
        assertEquals(spawnPos,sp.getSpawnPos(),"SpawnPosition should match provided spawnPos");
    }
    @Test
    void createSelfDestructDrone_doesNotAddNullSpawnPos(){
        Entity drone = EnemyFactory.createSelfDestructDrone(new Entity(),new Vector2(0f,0f));
        assertNull(drone.getComponent(SpawnPositionComponent.class),"No SpawnPositionComponent when initialized with null spawnPos");
    }
    @Test
    void CreateSelfDestructDrone_returnsDistinct(){
        Entity player = new Entity();
        Entity securityLight = new Entity();
        Entity droneA = EnemyFactory.createSelfDestructDrone(player,new Vector2(0f, 0f));
        Entity droneB = EnemyFactory.createSelfDestructDrone(player, new Vector2(0f, 0f));

        assertNotSame(droneA,droneB,"Each SelfDestructDrone instance should be distinct");
        assertNotSame(droneA.getComponent(AITaskComponent.class),droneB.getComponent(AITaskComponent.class),"Each AITaskComponent should be distinct");
    }
    @Test
    void SelfDestructDrone_hasSelfDestructComponent(){
        Entity player = new Entity();
        Entity securityLight = new Entity();
        Entity drone = EnemyFactory.createSelfDestructDrone(player,new Vector2(0f,0f));
        assertNotNull(drone.getComponent(SelfDestructComponent.class),"SelfDestructDone should have a SelfDestructComponent");
    }
    @Test
    void drones_haveCorrectPhysicsProperties(){
        //base drone
        Entity baseDrone  = EnemyFactory.createDrone(new Entity(),new Vector2(0f,0f));
        PhysicsMovementComponent pmc = baseDrone.getComponent(PhysicsMovementComponent.class);
        assertEquals(1.4f,pmc.getMaxSpeed(),0.01,"Base drone max speed");
        PhysicsComponent phy = baseDrone.getComponent(PhysicsComponent.class);
        assertEquals(0f,phy.getBody().getGravityScale(),0.01,"Base drone gravity scale");

        //self destruct drone
        Entity SelfDestruct = EnemyFactory.createSelfDestructDrone(new Entity(),new Vector2(0f,0f));
        pmc = SelfDestruct.getComponent(PhysicsMovementComponent.class);
        assertEquals(1.8f,pmc.getMaxSpeed(),0.01f,"SelfDestructDrone max speed");
        phy = SelfDestruct.getComponent(PhysicsComponent.class);
        assertEquals(0f,phy.getBody().getGravityScale(),0.01f);
    }


    private Entity createEntityWithPosition() {
        Entity e = new Entity();
        e.setPosition(new Vector2(10, 10));
        e.create();
        return e;
    }
}