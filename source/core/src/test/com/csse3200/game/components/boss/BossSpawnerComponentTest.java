package com.csse3200.game.components.boss;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BossSpawnerComponentTest {

    private EntityService entityService;
    private Entity bossEntity;
    private Entity playerEntity;
    private BossSpawnerComponent spawnerComponent;
    private EventHandler bossEvents;

    @BeforeEach
    void setUp() {
        // Clear any existing services
        ServiceLocator.clear();


        entityService = mock(EntityService.class);
        ServiceLocator.registerEntityService(entityService);

        // Create mock boss entity with event handler
        bossEntity = mock(Entity.class);
        bossEvents = mock(EventHandler.class);
        when(bossEntity.getEvents()).thenReturn(bossEvents);
        when(bossEntity.getPosition()).thenReturn(new Vector2(10f, 10f));

        // Create real player entity (no mocking needed for basic position)
        playerEntity = new Entity();
        playerEntity.setPosition(0f, 0f);

        // Mock entity service to return our entities
        com.badlogic.gdx.utils.Array<Entity> entities = new com.badlogic.gdx.utils.Array<>();
        entities.add(playerEntity);
        when(entityService.getEntities()).thenReturn(entities);
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    void createsWithTriggers() {
        List<Vector2> triggers = List.of(new Vector2(10f, 0f), new Vector2(20f, 0f));

        spawnerComponent = new BossSpawnerComponent(triggers, 2f);
        spawnerComponent.setEntity(bossEntity);
        assertDoesNotThrow(spawnerComponent::create);

        assertEquals(2, spawnerComponent.getTriggerCount());
        assertEquals(0, spawnerComponent.getTotalDronesSpawned());
        assertEquals(3, spawnerComponent.getMaxDrones());
    }



    @Test
    void doesNotActivateTriggerWhenPlayerNotAtPosition() {
        List<Vector2> triggers = List.of(new Vector2(10f, 0f));
        spawnerComponent = new BossSpawnerComponent(triggers, 2f);
        spawnerComponent.setEntity(bossEntity);
        spawnerComponent.create();

        // Player is before trigger position
        playerEntity.setPosition(5f, 0f);
        spawnerComponent.update();

        assertFalse(spawnerComponent.isTriggerActivated(0));
    }


    @Test
    void resetsTriggersAndDrones() {
        List<Vector2> triggers = List.of(new Vector2(10f, 0f));
        spawnerComponent = new BossSpawnerComponent(triggers, 2f);
        spawnerComponent.setEntity(bossEntity);
        spawnerComponent.create();

        // Activate trigger
        playerEntity.setPosition(10f, 0f);
        spawnerComponent.update();

        // Reset
        spawnerComponent.resetTriggers();

        assertFalse(spawnerComponent.isTriggerActivated(0));
        assertEquals(0, spawnerComponent.getCurrentTriggerIndex());
        assertEquals(0, spawnerComponent.getTotalDronesSpawned());
        assertFalse(spawnerComponent.isMaxDronesReached());
    }

    @Test
    void addsNewTriggerDynamically() {
        List<Vector2> triggers = new ArrayList<>(List.of(new Vector2(10f, 0f)));
        spawnerComponent = new BossSpawnerComponent(triggers, 2f);
        spawnerComponent.setEntity(bossEntity);
        spawnerComponent.create();

        spawnerComponent.addSpawnTrigger(new Vector2(30f, 0f));

        assertEquals(2, spawnerComponent.getTriggerCount());
    }


    @Test
    void handlesEmptyEntityService() {
        List<Vector2> triggers = List.of(new Vector2(10f, 0f));
        spawnerComponent = new BossSpawnerComponent(triggers, 2f);
        spawnerComponent.setEntity(bossEntity);

        // Empty entity service
        when(entityService.getEntities()).thenReturn(new com.badlogic.gdx.utils.Array<>());
        spawnerComponent.create();

        assertDoesNotThrow(spawnerComponent::update);
    }

    @Test
    void handlesEmptyTriggerList() {
        List<Vector2> triggers = new ArrayList<>();
        spawnerComponent = new BossSpawnerComponent(triggers, 2f);
        spawnerComponent.setEntity(bossEntity);
        spawnerComponent.create();

        assertDoesNotThrow(spawnerComponent::update);
        assertEquals(0, spawnerComponent.getTriggerCount());
    }




}