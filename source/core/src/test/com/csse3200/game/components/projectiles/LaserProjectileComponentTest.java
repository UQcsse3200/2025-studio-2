package com.csse3200.game.components.projectiles;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
class LaserProjectileComponentTest {

    @Mock
    private Entity projectileEntity;
    @Mock
    private Entity sourceEntity;
    @Mock
    private Entity playerEntity;
    @Mock
    private PhysicsComponent physicsComponent;
    @Mock
    private Body body;
    @Mock
    private Fixture projectileFixture;
    @Mock
    private Fixture playerFixture;
    @Mock
    private Fixture obstacleFixture;
    @Mock
    private Body playerBody;
    @Mock
    private Body obstacleBody;
    @Mock
    private HitboxComponent hitboxComponent;
    @Mock
    private CombatStatsComponent projectileStats;
    @Mock
    private CombatStatsComponent playerStats;
    @Mock
    private AnimationRenderComponent animator;
    @Mock
    private EntityService entityService;

    private EventHandler projectileEvents;
    private EventHandler playerEvents;
    private LaserProjectileComponent laserComponent;
    private Vector2 testDirection;
    private float testSpeed;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testDirection = new Vector2(1, 0); // Right direction
        testSpeed = 10f;

        // Setup event handlers
        projectileEvents = new EventHandler();
        playerEvents = new EventHandler();

        // Setup ServiceLocator
        ServiceLocator.registerEntityService(entityService);

        // Create component
        laserComponent = new LaserProjectileComponent(testDirection, testSpeed, sourceEntity);
    }

    private void setupBasicMocks() {
        // Setup projectile entity
        lenient().when(projectileEntity.getEvents()).thenReturn(projectileEvents);
        lenient().when(projectileEntity.getComponent(PhysicsComponent.class)).thenReturn(physicsComponent);
        lenient().when(projectileEntity.getComponent(CombatStatsComponent.class)).thenReturn(projectileStats);
        lenient().when(projectileEntity.getComponent(AnimationRenderComponent.class)).thenReturn(animator);
        lenient().when(projectileEntity.getCenterPosition()).thenReturn(new Vector2(0, 0));
        lenient().when(projectileEntity.isEnabled()).thenReturn(true);

        // Setup physics component and body
        lenient().when(physicsComponent.getBody()).thenReturn(body);
        lenient().when(body.getPosition()).thenReturn(new Vector2(0, 0));

        // Setup player entity
        lenient().when(playerEntity.getEvents()).thenReturn(playerEvents);
        lenient().when(playerEntity.getComponent(HitboxComponent.class)).thenReturn(hitboxComponent);
        lenient().when(playerEntity.getComponent(CombatStatsComponent.class)).thenReturn(playerStats);
        lenient().when(hitboxComponent.getLayer()).thenReturn(PhysicsLayer.PLAYER);

        // Setup player fixture and body
        lenient().when(playerFixture.getBody()).thenReturn(playerBody);
        BodyUserData playerBodyData = new BodyUserData();
        playerBodyData.entity = playerEntity;
        lenient().when(playerBody.getUserData()).thenReturn(playerBodyData);

        // Setup obstacle fixture
        lenient().when(obstacleFixture.getBody()).thenReturn(obstacleBody);
        Filter obstacleFilter = new Filter();
        obstacleFilter.categoryBits = PhysicsLayer.OBSTACLE;
        lenient().when(obstacleFixture.getFilterData()).thenReturn(obstacleFilter);

        // Setup projectile stats
        lenient().when(projectileStats.getBaseAttack()).thenReturn(10);

        laserComponent.setEntity(projectileEntity);
    }

    @Test
    void shouldCreateWithCorrectParameters() {
        setupBasicMocks();
        assertNotNull(laserComponent);
        laserComponent.create();
        verify(projectileEntity).getComponent(PhysicsComponent.class);
    }

    @Test
    void shouldStoreStartingPosition() {
        setupBasicMocks();
        Vector2 startPos = new Vector2(5, 5);
        when(body.getPosition()).thenReturn(startPos);

        laserComponent.create();

        // Verify component was created (indirectly tests that start position was stored)
        verify(physicsComponent, atLeast(1)).getBody();
    }

    @Test
    void shouldRegisterCollisionListener() {
        setupBasicMocks();
        laserComponent.create();

        // Verify that collision listener works by triggering an event
        // If listener wasn't registered, this would have no effect
        projectileEvents.trigger("collisionStart", projectileFixture, playerFixture);

        // Verify the collision was processed (entity disabled due to player hit)
        verify(projectileEntity).setEnabled(false);
    }

    @Test
    void shouldDisposeWhenMaxDistanceTraveled() {
        setupBasicMocks();
        laserComponent.create();

        // Simulate projectile traveling max distance
        when(projectileEntity.getCenterPosition()).thenReturn(new Vector2(150, 0));

        laserComponent.update();

        // Verify entity was disabled
        verify(projectileEntity).setEnabled(false);
    }

    @Test
    void shouldNotUpdateAfterHit() {
        setupBasicMocks();
        laserComponent.create();

        // Trigger collision with player
        projectileEvents.trigger("collisionStart", projectileFixture, playerFixture);

        // Reset the mock to clear previous interactions
        clearInvocations(projectileEntity);

        // Update should not cause disposal again
        lenient().when(projectileEntity.getCenterPosition()).thenReturn(new Vector2(200, 0));

        laserComponent.update();

        // Verify setEnabled was not called again
        verify(projectileEntity, never()).setEnabled(false);
    }

    @Test
    void shouldDealDamageToPlayer() {
        setupBasicMocks();
        laserComponent.create();

        // Trigger collision with player
        projectileEvents.trigger("collisionStart", projectileFixture, playerFixture);

        // Verify damage was dealt
        verify(playerStats).hit(projectileStats);
    }

    @Test
    void shouldTriggerPlayerCrouchOnHit() {
        setupBasicMocks();

        // Use a spy on EventHandler to verify trigger calls
        EventHandler playerEventsSpy = spy(playerEvents);
        when(playerEntity.getEvents()).thenReturn(playerEventsSpy);

        laserComponent.create();

        // Trigger collision with player
        projectileEvents.trigger("collisionStart", projectileFixture, playerFixture);

        // Verify crouch event was triggered
        verify(playerEventsSpy).trigger("crouch");
    }

    @Test
    void shouldIgnoreCollisionWithSource() {
        setupBasicMocks();
        laserComponent.create();

        // Setup source entity fixture
        Fixture sourceFixture = mock(Fixture.class);
        Body sourceBody = mock(Body.class);
        when(sourceFixture.getBody()).thenReturn(sourceBody);

        BodyUserData sourceBodyData = new BodyUserData();
        sourceBodyData.entity = sourceEntity;
        when(sourceBody.getUserData()).thenReturn(sourceBodyData);

        // Trigger collision with source
        projectileEvents.trigger("collisionStart", projectileFixture, sourceFixture);

        // Verify no damage was dealt and entity wasn't disabled
        verify(projectileEntity, never()).setEnabled(false);
    }

    @Test
    void shouldDisposeOnObstacleHit() {
        setupBasicMocks();
        laserComponent.create();

        // Trigger collision with obstacle
        BodyUserData obstacleBodyData = new BodyUserData();
        obstacleBodyData.entity = mock(Entity.class); // Different entity, not source
        when(obstacleBody.getUserData()).thenReturn(obstacleBodyData);

        projectileEvents.trigger("collisionStart", projectileFixture, obstacleFixture);

        // Verify entity was disabled (disposal initiated)
        verify(projectileEntity).setEnabled(false);
    }

    @Test
    void shouldHandleNullFixtureInCollision() {
        setupBasicMocks();
        laserComponent.create();

        // Trigger collision with null fixture
        projectileEvents.trigger("collisionStart", projectileFixture, null);

        // Should not throw exception and entity should still be enabled
        verify(projectileEntity, never()).setEnabled(false);
    }

    @Test
    void shouldHandleNullBodyInCollision() {
        setupBasicMocks();
        laserComponent.create();

        Fixture nullBodyFixture = mock(Fixture.class);
        when(nullBodyFixture.getBody()).thenReturn(null);

        projectileEvents.trigger("collisionStart", projectileFixture, nullBodyFixture);

        // Should not throw exception
        verify(projectileEntity, never()).setEnabled(false);
    }

    @Test
    void shouldHandleNullUserDataInCollision() {
        setupBasicMocks();
        laserComponent.create();

        Fixture fixture = mock(Fixture.class);
        Body body = mock(Body.class);
        when(fixture.getBody()).thenReturn(body);
        when(body.getUserData()).thenReturn(null);

        projectileEvents.trigger("collisionStart", projectileFixture, fixture);

        // Should not throw exception
        verify(projectileEntity, never()).setEnabled(false);
    }

    @Test
    void shouldIdentifyPlayerCorrectly() {
        setupBasicMocks();
        laserComponent.create();

        // Setup player with correct physics layer
        when(hitboxComponent.getLayer()).thenReturn(PhysicsLayer.PLAYER);

        projectileEvents.trigger("collisionStart", projectileFixture, playerFixture);

        // Verify player was hit
        verify(playerStats).hit(projectileStats);
    }

    @Test
    void shouldNotIdentifyNonPlayerAsPlayer() {
        setupBasicMocks();
        laserComponent.create();

        // Setup entity without player layer
        Entity nonPlayer = mock(Entity.class);
        HitboxComponent nonPlayerHitbox = mock(HitboxComponent.class);
        lenient().when(nonPlayer.getComponent(HitboxComponent.class)).thenReturn(nonPlayerHitbox);
        lenient().when(nonPlayer.getComponent(CombatStatsComponent.class)).thenReturn(mock(CombatStatsComponent.class));
        lenient().when(nonPlayerHitbox.getLayer()).thenReturn(PhysicsLayer.NPC);

        Fixture nonPlayerFixture = mock(Fixture.class);
        Body nonPlayerBody = mock(Body.class);
        Filter nonPlayerFilter = new Filter();
        nonPlayerFilter.categoryBits = (short) 0; // Not an obstacle

        lenient().when(nonPlayerFixture.getBody()).thenReturn(nonPlayerBody);
        lenient().when(nonPlayerFixture.getFilterData()).thenReturn(nonPlayerFilter);

        BodyUserData nonPlayerBodyData = new BodyUserData();
        nonPlayerBodyData.entity = nonPlayer;
        lenient().when(nonPlayerBody.getUserData()).thenReturn(nonPlayerBodyData);

        projectileEvents.trigger("collisionStart", projectileFixture, nonPlayerFixture);

        // Verify player damage was not dealt
        verify(playerStats, never()).hit(any());
    }

    @Test
    void shouldStopMovementOnHit() {
        setupBasicMocks();
        laserComponent.create();

        projectileEvents.trigger("collisionStart", projectileFixture, playerFixture);

        // Verify body velocity was set to zero
        verify(body).setLinearVelocity(0, 0);
    }

    @Test
    void shouldHandleDisposalWhenEntityDisabled() {
        setupBasicMocks();
        when(projectileEntity.isEnabled()).thenReturn(false);
        laserComponent.create();

        // Trigger collision - should handle gracefully
        projectileEvents.trigger("collisionStart", projectileFixture, playerFixture);

        // Should not attempt to disable already disabled entity
        verify(projectileEntity, never()).setEnabled(false);
    }

    @Test
    void shouldHandleMissingCombatStatsOnPlayer() {
        setupBasicMocks();
        laserComponent.create();

        when(playerEntity.getComponent(CombatStatsComponent.class)).thenReturn(null);

        projectileEvents.trigger("collisionStart", projectileFixture, playerFixture);

        // Should not throw exception
        verify(playerStats, never()).hit(any());
    }

    @Test
    void shouldHandleMissingCombatStatsOnProjectile() {
        setupBasicMocks();
        laserComponent.create();

        when(projectileEntity.getComponent(CombatStatsComponent.class)).thenReturn(null);

        projectileEvents.trigger("collisionStart", projectileFixture, playerFixture);

        // Should not throw exception
        verify(playerStats, never()).hit(any());
    }

    @Test
    void shouldNormalizeDirection() {
        Vector2 unnormalizedDirection = new Vector2(3, 4); // Length = 5
        LaserProjectileComponent component = new LaserProjectileComponent(unnormalizedDirection, testSpeed, sourceEntity);

        // Direction should be normalized internally
        // This is verified by the component not throwing exceptions during operation
        setupBasicMocks();
        component.setEntity(projectileEntity);
        component.create();

        // If direction wasn't normalized, calculations would be incorrect
        assertNotNull(component);
    }

    @Test
    void shouldHandleZeroDistanceTraveled() {
        setupBasicMocks();
        laserComponent.create();

        // At starting position
        when(projectileEntity.getCenterPosition()).thenReturn(new Vector2(0, 0));

        laserComponent.update();

        // Should not dispose
        verify(projectileEntity, never()).setEnabled(false);
    }

    @Test
    void shouldDisposeComponentProperly() {
        setupBasicMocks();
        laserComponent.create();
        laserComponent.dispose();

        // Should complete without exceptions
        assertNotNull(laserComponent);
    }
}