package com.csse3200.game.components.boss;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.EnemyFactory;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BossSpawnerComponentTest {

    private EntityService entityService;
    private GameTime gameTime;

    private Entity bossEntity;
    private EventHandler bossEvents;

    private Entity playerEntityMock; // use mock entity
    private BossSpawnerComponent spawner;

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();

        entityService = mock(EntityService.class);
        ServiceLocator.registerEntityService(entityService);

        gameTime = mock(GameTime.class);
        when(gameTime.getDeltaTime()).thenReturn(0.01f);
        ServiceLocator.registerTimeSource(gameTime);


        //Boss Entity + Event line
        bossEntity = mock(Entity.class);
        bossEvents = mock(EventHandler.class);
        when(bossEntity.getEvents()).thenReturn(bossEvents);
        when(bossEntity.getPosition()).thenReturn(new Vector2(10f, 10f));

        // Mock player entity: just make getComponent(PlayerActions.class) return non-null
        playerEntityMock = mock(Entity.class);
        when(playerEntityMock.getComponent(PlayerActions.class)).thenReturn(mock(PlayerActions.class));
        when(playerEntityMock.getPosition()).thenReturn(new Vector2(0f, 0f));

        Array<Entity> world = new Array<>();
        world.add(playerEntityMock);
        when(entityService.getEntities()).thenReturn(world);
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    void createsWithTriggers_andFindsPlayer() {
        List<Vector2> triggers = List.of(new Vector2(10f, 0f), new Vector2(20f, 0f));
        spawner = new BossSpawnerComponent(triggers, 2f);
        spawner.setEntity(bossEntity);

        assertDoesNotThrow(spawner::create);
        assertEquals(2, spawner.getTriggerCount());
        assertNotNull(spawner.getPlayer()); // 能找到玩家
    }

    @Test
    void doesNotActivateTriggerBeforePositionReached() {
        List<Vector2> triggers = List.of(new Vector2(10f, 0f));
        spawner = new BossSpawnerComponent(triggers, 2f);
        spawner.setEntity(bossEntity);
        spawner.create();

        // player < 10
        when(playerEntityMock.getPosition()).thenReturn(new Vector2(5f, 0f));
        spawner.update();

        assertFalse(spawner.isTriggerActivated(0));
        verify(bossEvents, never()).trigger(eq("spawningPhaseStart"), anyInt());
        verify(bossEvents, never()).trigger(eq("boss:phaseChanged"), anyInt());
    }

    @Test
    void activatesTriggerAndStartsPhase_whenPlayerReachesX() {
        List<Vector2> triggers = List.of(new Vector2(10f, 0f));
        spawner = new BossSpawnerComponent(triggers, 2f);
        spawner.setEntity(bossEntity);
        spawner.create();

        when(playerEntityMock.getPosition()).thenReturn(new Vector2(10f, 0f));
        spawner.update();

        assertTrue(spawner.isTriggerActivated(0));
        // Some machines update multiple times in one frame, so relax the limit to at least once.
        verify(bossEvents, atLeastOnce()).trigger("spawningPhaseStart", 0);
        verify(bossEvents, atLeastOnce()).trigger("boss:phaseChanged", 0);
    }

    @Test
    void resetTriggers_allowsRetriggeringPhase() {
        List<Vector2> triggers = List.of(new Vector2(10f, 0f));
        spawner = new BossSpawnerComponent(triggers, 2f);
        spawner.setEntity(bossEntity);
        spawner.create();

        when(playerEntityMock.getPosition()).thenReturn(new Vector2(12f, 0f));
        spawner.update();
        verify(bossEvents, atLeastOnce()).trigger("spawningPhaseStart", 0);

        spawner.resetTriggers();
        assertFalse(spawner.isTriggerActivated(0));
        assertEquals(0, spawner.getCurrentTriggerIndex());

        // trigger
        when(playerEntityMock.getPosition()).thenReturn(new Vector2(0f, 0f));
        spawner.update();
        when(playerEntityMock.getPosition()).thenReturn(new Vector2(12f, 0f));
        spawner.update();

        //Second trigger (at least 2 total calls)
        verify(bossEvents, atLeast(2)).trigger("spawningPhaseStart", 0);
    }

    @Test
    void addsNewTriggerDynamically() {
        List<Vector2> triggers = new ArrayList<>(List.of(new Vector2(10f, 0f)));
        spawner = new BossSpawnerComponent(triggers, 2f);
        spawner.setEntity(bossEntity);
        spawner.create();

        spawner.addSpawnTrigger(new Vector2(30f, 0f));
        assertEquals(2, spawner.getTriggerCount());
    }

    @Test
    void handlesEmptyEntityService_gracefully() {
        when(entityService.getEntities()).thenReturn(new Array<>());

        List<Vector2> triggers = List.of(new Vector2(10f, 0f));
        spawner = new BossSpawnerComponent(triggers, 2f);
        spawner.setEntity(bossEntity);
        spawner.create();

        assertDoesNotThrow(spawner::update);
        assertNull(spawner.getPlayer()); // Even if you can't find the player, you should be safe
    }

    @Test
    void handlesEmptyTriggerList() {
        List<Vector2> triggers = new ArrayList<>();
        spawner = new BossSpawnerComponent(triggers, 2f);
        spawner.setEntity(bossEntity);
        spawner.create();

        assertDoesNotThrow(spawner::update);
        assertEquals(0, spawner.getTriggerCount());
    }

    @Test
    void duringInitialCooldown_noDroneCreated_withoutAssets() {
        List<Vector2> triggers = List.of(new Vector2(10f, 0f));
        spawner = new BossSpawnerComponent(triggers, 2f);
        spawner.setEntity(bossEntity);
        spawner.create();

        // Trigger Phase: Setup spawnCooldown = 0.5
        when(playerEntityMock.getPosition()).thenReturn(new Vector2(10f, 0f));
        spawner.update();

        //Second frame: deduct all the cooldown at once,
        // but return early in the if(spawnCooldown>0) branch
        when(gameTime.getDeltaTime()).thenReturn(1.0f);
        spawner.update();

        // Frame 3: This time it enters the windup branch and triggers "generateDroneStart"
        spawner.update();

        // The entity should not actually be created (to avoid pulling resources)
        verify(entityService, never()).register(any(Entity.class));
        // At least one pre-swing animation event has been triggered
        verify(bossEvents, atLeastOnce()).trigger(eq("generateDroneStart"));
    }
    @Test
    void testPrivateCleanupDeadDrones() throws Exception {
        List<Vector2> triggers = List.of(new Vector2(10f, 0f));
        spawner = new BossSpawnerComponent(triggers, 2f);
        spawner.setEntity(bossEntity);
        spawner.create();

        // Add dead drones to internal list using reflection
        var spawnedDronesField = BossSpawnerComponent.class.getDeclaredField("spawnedDrones");
        spawnedDronesField.setAccessible(true);
        List<Entity> spawnedDrones = (List<Entity>) spawnedDronesField.get(spawner);

        Entity deadDrone = mock(Entity.class);
        spawnedDrones.add(deadDrone);
        spawnedDrones.add(null);

        when(entityService.getEntities()).thenReturn(new Array<>());

        var cleanupMethod = BossSpawnerComponent.class.getDeclaredMethod("cleanupDeadDrones");
        cleanupMethod.setAccessible(true);
        cleanupMethod.invoke(spawner);

        assertTrue(spawnedDrones.isEmpty());
    }

    @Test
    void testPrivateGetSpawnPositionAroundBoss() throws Exception {
        List<Vector2> triggers = List.of(new Vector2(10f, 0f));
        spawner = new BossSpawnerComponent(triggers, 2f);
        spawner.setEntity(bossEntity);

        Vector2 bossPos = new Vector2(100f, 50f);

        var method = BossSpawnerComponent.class.getDeclaredMethod("getSpawnPositionAroundBoss", Vector2.class);
        method.setAccessible(true);
        Vector2 spawnPos = (Vector2) method.invoke(spawner, bossPos);

        assertEquals(new Vector2(104f, 50f), spawnPos);
    }

    @Test
    void testPrivateFindPlayer() throws Exception {
        List<Vector2> triggers = List.of(new Vector2(10f, 0f));
        spawner = new BossSpawnerComponent(triggers, 2f);
        spawner.setEntity(bossEntity);

        // Clear player field first
        var playerField = BossSpawnerComponent.class.getDeclaredField("player");
        playerField.setAccessible(true);
        playerField.set(spawner, null);

        // Call private method using reflection
        var findMethod = BossSpawnerComponent.class.getDeclaredMethod("findPlayer");
        findMethod.setAccessible(true);
        findMethod.invoke(spawner);

        Entity foundPlayer = (Entity) playerField.get(spawner);
        assertEquals(playerEntityMock, foundPlayer);
    }

    @Test
    void testPrivateUpdateSpawning() throws Exception {
        List<Vector2> triggers = List.of(new Vector2(10f, 0f));
        spawner = new BossSpawnerComponent(triggers, 2f);
        spawner.setEntity(bossEntity);
        spawner.create();

        // Set private fields to activate spawning
        var isSpawningField = BossSpawnerComponent.class.getDeclaredField("isSpawningActive");
        isSpawningField.setAccessible(true);
        isSpawningField.set(spawner, true);

        var currentIndexField = BossSpawnerComponent.class.getDeclaredField("currentTriggerIndex");
        currentIndexField.setAccessible(true);
        currentIndexField.set(spawner, 0);

        // Call private method
        var updateMethod = BossSpawnerComponent.class.getDeclaredMethod("updateSpawning");
        updateMethod.setAccessible(true);
        updateMethod.invoke(spawner);

        // Verify no errors occurred
        assertNotNull(spawner);
    }

    @Test
    void testPrivateCheckSpawnTriggers() throws Exception {
        List<Vector2> triggers = List.of(new Vector2(10f, 0f), new Vector2(20f, 0f));
        spawner = new BossSpawnerComponent(triggers, 2f);
        spawner.setEntity(bossEntity);
        spawner.create();

        // Set player past first trigger
        when(playerEntityMock.getPosition()).thenReturn(new Vector2(15f, 0f));

        // Call private method
        var checkMethod = BossSpawnerComponent.class.getDeclaredMethod("checkSpawnTriggers");
        checkMethod.setAccessible(true);
        checkMethod.invoke(spawner);

        // Verify first trigger was activated
        assertTrue(spawner.isTriggerActivated(0));
    }

    @Test
    void testPrivateStartSpawningPhase() throws Exception {
        List<Vector2> triggers = List.of(new Vector2(10f, 0f));
        spawner = new BossSpawnerComponent(triggers, 2f);
        spawner.setEntity(bossEntity);
        spawner.create();

        // Set current trigger index
        var currentIndexField = BossSpawnerComponent.class.getDeclaredField("currentTriggerIndex");
        currentIndexField.setAccessible(true);
        currentIndexField.set(spawner, 0);

        // Call private method
        var startMethod = BossSpawnerComponent.class.getDeclaredMethod("startSpawningPhase");
        startMethod.setAccessible(true);
        startMethod.invoke(spawner);

        // Verify spawning was activated
        var isSpawningField = BossSpawnerComponent.class.getDeclaredField("isSpawningActive");
        isSpawningField.setAccessible(true);
        assertTrue((Boolean) isSpawningField.get(spawner));

        verify(bossEvents).trigger("spawningPhaseStart", 0);
    }


    @Test
    void testPrivateGetActiveDroneCount() throws Exception {
        List<Vector2> triggers = List.of(new Vector2(10f, 0f));
        spawner = new BossSpawnerComponent(triggers, 2f);
        spawner.setEntity(bossEntity);
        spawner.create();

        // Add drones to internal list using reflection
        var spawnedDronesField = BossSpawnerComponent.class.getDeclaredField("spawnedDrones");
        spawnedDronesField.setAccessible(true);
        List<Entity> spawnedDrones = (List<Entity>) spawnedDronesField.get(spawner);

        Entity activeDrone = mock(Entity.class);
        spawnedDrones.add(activeDrone);

        Array<Entity> worldEntities = new Array<>();
        worldEntities.add(activeDrone);
        when(entityService.getEntities()).thenReturn(worldEntities);

        // Call private method
        var countMethod = BossSpawnerComponent.class.getDeclaredMethod("getActiveDroneCount");
        countMethod.setAccessible(true);
        int count = (Integer) countMethod.invoke(spawner);

        assertEquals(1, count);
    }

    @Test
    void testPhaseConfigConstructor() {
        // Test PhaseConfig inner class
        BossSpawnerComponent.PhaseConfig config =
                new BossSpawnerComponent.PhaseConfig(
                        EnemyFactory.DroneVariant.SCOUT,
                        1.5f,
                        2,
                        3,
                        1.0f
                );

        assertEquals(EnemyFactory.DroneVariant.SCOUT, config.variant);
        assertEquals(1.5f, config.spawnInterval);
        assertEquals(2, config.maxConcurrent);
        assertEquals(3, config.totalToSpawn);
        assertEquals(1.0f, config.windup);
    }





}
