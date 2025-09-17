package com.csse3200.game.entities.factories;

import box2dLight.ConeLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.enemy.PatrolRouteComponent;
import com.csse3200.game.components.enemy.SpawnPositionComponent;
import com.csse3200.game.components.SelfDestructComponent;
import com.csse3200.game.components.npc.DroneAnimationController;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.configs.BaseEntityConfig;
import com.csse3200.game.entities.configs.EnemyConfigs;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.lighting.LightingEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.components.lighting.ConeDetectorComponent;
import com.csse3200.game.lighting.LightingService;
import box2dLight.RayHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class EnemyFactoryTest {
    private BaseEntityConfig droneConfig;
    private ResourceService rs;
    private GameTime gameTime;

    private MockedConstruction<RayHandler> rhCons;
    private MockedConstruction<ConeLight> coneCons;

    @BeforeEach
    void setUp() {
        EnemyConfigs configs = FileLoader.readClass(EnemyConfigs.class, "configs/enemies.json");
        droneConfig = configs.drone;

        // Register services needed for entities
        ServiceLocator.registerPhysicsService(new PhysicsService());
        ServiceLocator.registerEntityService(new EntityService());

        RenderService renderService = new RenderService();
        renderService.setDebug(mock(DebugRenderer.class));
        ServiceLocator.registerRenderService(renderService);


        rs = new ResourceService();
        ServiceLocator.registerResourceService(rs);
        rs.loadTextureAtlases(new String[]{"images/drone.atlas"});
        rs.loadAll();

        // Mock time source needed for AI tasks
        gameTime = mock(GameTime.class);
        ServiceLocator.registerTimeSource(gameTime);
        when(gameTime.getTime()).thenReturn(0L);

        // Mock light source
        List<List<?>> capturedArgs = new ArrayList<>();
        rhCons = mockConstruction(RayHandler.class);
        coneCons = mockConstruction(
                ConeLight.class,
                (mock, ctx) -> {
                    capturedArgs.add(ctx.arguments());
                    when(mock.getDistance()).thenReturn((Float) ctx.arguments().get(3));
                    when(mock.getConeDegree()).thenReturn((Float) ctx.arguments().get(7));
                    when(mock.getDirection()).thenReturn((Float) ctx.arguments().get(6));
                    when(mock.getColor()).thenReturn((Color) ctx.arguments().get(2));
                });

        // This call now uses the mocks that are stored as class fields
        ServiceLocator.registerLightingService(createLightingService());
    }

    @AfterEach
    void cleanUp() {
        rs.unloadAssets(new String[]{"images/drone.atlas"});
        rs.dispose();
        ServiceLocator.clear();
        if (rhCons != null) {
            rhCons.close();
        }
        if (coneCons != null) {
            coneCons.close();
        }
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
    /*
        @Test
        void createBomberDrone_hasBaseEnemyComponents() {
            Entity bomberDrone = EnemyFactory.createBomberDrone(new Entity(), new Vector2(0f, 0f), "bomber1");
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
            Entity bomberDrone = EnemyFactory.createBomberDrone(new Entity(), new Vector2(0f, 0f), "bomber1");
            ServiceLocator.getEntityService().register(bomberDrone);

            AnimationRenderComponent anim = bomberDrone.getComponent(AnimationRenderComponent.class);
            assertNotNull(anim, "Drone should have an AnimationRenderComponent");
            assertTrue(anim.hasAnimation("float"), "Missing 'float' animation");
            assertTrue(anim.hasAnimation("angry_float"), "Missing 'angry_float' animation");
            assertTrue(anim.hasAnimation("drop"), "Missing 'drop' animation");
        }

        @Test
        void createBomberDrone_hasAnimationController() {
                Entity bomberDrone = EnemyFactory.createBomberDrone(new Entity(), new Vector2(0f, 0f), "bomber1");
                ServiceLocator.getEntityService().register(bomberDrone);

                assertNotNull(bomberDrone.getComponent(DroneAnimationController.class),
                        "Drone should have AnimationController");
        }

        @Test
        void bomberDrone_startsOnFloatAnim() {
            Entity bomberDrone = EnemyFactory.createBomberDrone(new Entity(), new Vector2(0f, 0f), "bomber1");
            AnimationRenderComponent arc = bomberDrone.getComponent(AnimationRenderComponent.class);
            assertEquals("float", arc.getCurrentAnimation());
            assertTrue(arc.hasAnimation("float"));
        }

        @Test
        void createBomberDrone_hasCorrectCombatStats() {
            Entity bomberDrone = EnemyFactory.createBomberDrone(new Entity(), new Vector2(0f, 0f), "bomber1");
            ServiceLocator.getEntityService().register(bomberDrone);

            CombatStatsComponent stats = bomberDrone.getComponent(CombatStatsComponent.class);
            assertNotNull(stats, "Drone should have a CombatStatsComponent");
            assertEquals(droneConfig.health, stats.getHealth(), "Drone health mismatch");
            assertEquals(droneConfig.baseAttack, stats.getBaseAttack(), "Drone baseAttack mismatch");
        }
    */
    @Test
    void createBomberDrone_addsSpawnPosition() {
        Vector2 start = new Vector2(0, 0);
        Entity bomberDrone = EnemyFactory.createBomberDrone(new Entity(), start, "bomber1");
        SpawnPositionComponent sp = bomberDrone.getComponent(SpawnPositionComponent.class);
        assertNotNull(sp);
        assertEquals(start, sp.getSpawnPos(),
                "Should have SpawnPositionComponent correctly initialised");
    }

    @Test
    void createBomberDrone_doesNotAddNullSpawnPos() {
        Entity bomberDrone = EnemyFactory.createBomberDrone(new Entity(), null, "bomber1");
        assertNull(bomberDrone.getComponent(SpawnPositionComponent.class),
                "No SpawnPositionComponent when initialised with null spawnPos");
    }

    @Test
    void createBomberDrone_returnsDistinct() {
        Entity a = EnemyFactory.createBomberDrone(new Entity(), new Vector2(0f, 0f), "bomber1");
        Entity b = EnemyFactory.createBomberDrone(new Entity(), new Vector2(0f, 0f), "bomber1");
        assertNotSame(a, b, "Drones should be distinct");

        AITaskComponent ai_a = a.getComponent(AITaskComponent.class);
        AITaskComponent ai_b = b.getComponent(AITaskComponent.class);
        assertNotSame(ai_a, ai_b,
                "Drones should have distinct AITaskComponents");
    }


    // Tests to verify correct AI task flow
    @Test
    void patrolDrone_patrolToChaseFlow() {
        Entity target = createEntityWithPosition(new Vector2(0.5f, 0));
        Vector2[] route = {new Vector2(0, 0), new Vector2(1, 0)};

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
        Entity target = createEntityWithPosition(new Vector2(100, 100));
        Vector2[] route = {new Vector2(0, 0), new Vector2(1, 0)};

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

        when(gameTime.getTime()).thenReturn(3100L); // After chase grace period, should end
        ai.update(); // Cooldown

        assertEquals(List.of("chaseStart", "chaseEnd", "cooldownStart"), eventLog);
    }

    @Test
    void patrolDrone_cooldownToChaseFlow() {
        Entity target = createEntityWithPosition(new Vector2(100, 100));
        Vector2[] route = {new Vector2(0, 0), new Vector2(1, 0)};

        Entity drone = EnemyFactory.createPatrollingDrone(target, route);
        AITaskComponent ai = drone.getComponent(AITaskComponent.class);
        drone.create();

        List<String> eventLog = new ArrayList<>();
        drone.getEvents().addListener("cooldownStart", () -> eventLog.add("cooldownStart"));
        drone.getEvents().addListener("cooldownEnd", () -> eventLog.add("cooldownEnd"));
        drone.getEvents().addListener("chaseStart", () -> eventLog.add("chaseStart"));
        drone.getEvents().addListener("patrolStart", () -> eventLog.add("patrolStart"));

        drone.getEvents().trigger("enemyActivated");
        ai.update(); // Chasing

        when(gameTime.getTime()).thenReturn(3100L); // Finish chase
        ai.update(); // Cooldown

        // Advance cooldown (but not finished)
        when(gameTime.getTime()).thenReturn(4000L);
        ai.update(); // Still in cooldown

        drone.getEvents().trigger("enemyActivated");
        ai.update(); // Chasing

        assertEquals(List.of("chaseStart", "cooldownStart", "cooldownEnd", "chaseStart"), eventLog);
    }
    @Test
    void SelfDestructDrone_hasALlRequiredComponents() {
        Entity SelfDestructDrone = EnemyFactory.createSelfDestructionDrone(new Entity(),  new Vector2(0, 0));
        assertNotNull(SelfDestructDrone.getComponent(CombatStatsComponent.class));
        assertNotNull(SelfDestructDrone.getComponent(AnimationRenderComponent.class));
        assertNotNull(SelfDestructDrone.getComponent(AITaskComponent.class));
        assertNotNull(SelfDestructDrone.getComponent(PhysicsMovementComponent.class));
    }
    @Test
    void SelfDestructDrone_hasCorrectMaxSpeed(){
        Entity SelfDestructDrone = EnemyFactory.createSelfDestructionDrone(new Entity(),  new Vector2(0, 0));
        PhysicsMovementComponent physics = SelfDestructDrone.getComponent(PhysicsMovementComponent.class);

        assertEquals(1.8f,physics.getMaxSpeed(),0.01f,"SelfDestructDrone should have a max speed of 0.9f");
    }
    @Test
    void SelfDestructDrone_hasCorrectPhysicsBodyType(){
        Entity SelfDestructDrone = EnemyFactory.createSelfDestructionDrone(new  Entity(),  new Vector2(0, 0));
        PhysicsComponent physics = SelfDestructDrone.getComponent(PhysicsComponent.class);

        assertEquals(BodyDef.BodyType.DynamicBody,physics.getBody().getType(),"SelfDestructDrone Should use a dynamic physics body");

    }
    @Test
    void SelfDestructDrone_hasSpawnPosition(){
        Entity SelfDestructDrone = EnemyFactory.createSelfDestructionDrone(new Entity(),  new Vector2(0, 0));
        SpawnPositionComponent spawn = SelfDestructDrone.getComponent(SpawnPositionComponent.class);

        assertNotNull(spawn,"SpawnPositionComponent should be present when start position provided");
        assertEquals(new Vector2(0,0),spawn.getSpawnPos(),"SpawnPositionComponent should be correctly initialised with given position");
    }
    @Test
    void SelfDestructDrone_doesNotAddNullPawnPos(){
        Entity SelfDestructDrone = EnemyFactory.createSelfDestructionDrone(new Entity(), null);
        assertNull(SelfDestructDrone.getComponent(SpawnPositionComponent.class),"No spawnPositionComponent when initialised with null spawnPos");

    }
    @Test
    void SelfDestructDrone_hasAnimation(){
        Entity SelfDestructDrone = EnemyFactory.createSelfDestructionDrone(new Entity(),new Vector2(0,0));
        AnimationRenderComponent animator = SelfDestructDrone.getComponent(AnimationRenderComponent.class);
        assertNotNull(animator,"SelfDestructDrone Should have an AnimationRender Component");
        assertTrue(animator.hasAnimation("angry_float"),"missing angry_float animation");
        assertTrue(animator.hasAnimation("float"),"missing float animation");
        assertTrue(animator.hasAnimation("bomb_effect"),"missing bomb_effect  animation");
    }
    @Test
    void SelfDestructDrone_StartsOnFloatAnimation(){
        Entity SelfDestructDrone = EnemyFactory.createSelfDestructionDrone(new Entity(),  new Vector2(0, 0));
        AnimationRenderComponent animator = SelfDestructDrone.getComponent(AnimationRenderComponent.class);

        assertEquals("float",animator.getCurrentAnimation(),"SelfDestruct should start on Float animation");
    }
    @Test
    void SelfDestructDrone_hasCorrectCombatStats(){
        Entity SelfDestructDrone = EnemyFactory.createSelfDestructionDrone(new Entity(),  new Vector2(0, 0));
        CombatStatsComponent stats = SelfDestructDrone.getComponent(CombatStatsComponent.class);

        assertNotNull(stats,"Drone should have a CombatStatsComponent");
        assertEquals(droneConfig.health,stats.getHealth(),"DroneHealth mismatch");
        assertEquals(droneConfig.baseAttack,stats.getBaseAttack(),"Drone baseAttack mismatch");
    }
    @Test
    void SelfDestructDrone_returnsDistinct(){
        Entity a = EnemyFactory.createSelfDestructionDrone(new Entity(),  new Vector2(0, 0));
        Entity b = EnemyFactory.createSelfDestructionDrone(new Entity(),  new Vector2(0, 0));

        assertNotSame(a,b,"Drones Should be distinct");
        assertNotSame(a.getComponent(AITaskComponent.class),b.getComponent(AITaskComponent.class),"Drones Should have Distinct AITaskComponent");
    }
    /*
    @Test
    void SelfDestructDrone_hasChaseTask(){
        Entity target = new Entity();
        target.addComponent(new SpawnPositionComponent(new Vector2(5,5)));
        Entity SelfDestructDrone = EnemyFactory.createSelfDestructionDrone(target,  new Vector2(0, 0));
        AITaskComponent aiTask = SelfDestructDrone.getComponent(AITaskComponent.class);

        assertDoesNotThrow(aiTask::update,"AI component should be updated without throwing ");
    }
    */
    @Test
    void SelfDestructDrone_handleNullParentEntityGracefully(){
        assertDoesNotThrow(() -> EnemyFactory.createSelfDestructionDrone(null,new Vector2(0, 0)),"Creating a selfDestructDrone with null parent entity should not throw");
    }
    /*
    @Test
    void SelfDestructDrone_triggerExplosionOnDeath(){
        Entity SelfDestructDrone = EnemyFactory.createSelfDestructionDrone(new Entity(),new Vector2(0, 0));

        final boolean[] exploded = {false};
        SelfDestructDrone.getEvents().addListener("explode",()->exploded[0]=true);

        //SelfDestructDrone.getComponent(CombatStatsComponent.class).setHealth(0);
        SelfDestructDrone.getComponent(SelfDestructComponent.class).update();
        assertTrue(exploded[0],"SelfDestructDrone should trigger explosion event on death");
    }

     */

    private Entity createEntityWithPosition(Vector2 pos) {
        Entity e = new Entity();
        e.setPosition(pos);
        e.create();
        return e;
    }

    private LightingService createLightingService() {
        return new LightingService(
                new LightingEngine(new CameraComponent(), new World(new Vector2(0, 0), true)));
    }
}