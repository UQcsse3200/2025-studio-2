package com.csse3200.game.components.boss;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
class BossLaserAttackComponentTest {

    @Mock private Entity bossEntity;
    @Mock private Entity playerEntity;
    @Mock private Entity laserProjectile;
    @Mock private GameTime gameTime;

    private EventHandler bossEvents;
    private BossLaserAttackComponent component;

    @BeforeEach
    void setUp() {
        bossEvents = new EventHandler();
        ServiceLocator.registerTimeSource(gameTime);
        component = new BossLaserAttackComponent(playerEntity);

        lenient().when(bossEntity.getEvents()).thenReturn(bossEvents);
        lenient().when(bossEntity.getCenterPosition()).thenReturn(new Vector2(10, 10));
        lenient().when(bossEntity.getId()).thenReturn(1);
        lenient().when(playerEntity.isEnabled()).thenReturn(true);
        lenient().when(playerEntity.getCenterPosition()).thenReturn(new Vector2(20, 20));

        component.setEntity(bossEntity);
    }

    @Test
    void shouldCreateWithTarget() {
        assertNotNull(component);
        assertEquals(playerEntity, component.getTarget());
    }

    @Test
    void shouldInitialize() {
        component.create();
        assertDoesNotThrow(() -> component.create());
    }

    @Test
    void shouldNotAttackWithNullTarget() {
        component.create();
        component.setTarget(null);
        lenient().when(gameTime.getDeltaTime()).thenReturn(8f);

        EventHandler spy = spy(bossEvents);
        lenient().when(bossEntity.getEvents()).thenReturn(spy);
        component.update();

        verify(spy, never()).trigger("shootLaserStart");
    }

    @Test
    void shouldNotAttackWithDisabledTarget() {
        component.create();
        lenient().when(playerEntity.isEnabled()).thenReturn(false);
        lenient().when(gameTime.getDeltaTime()).thenReturn(8f);
        component.update();

        EventHandler spy = spy(bossEvents);
        lenient().when(bossEntity.getEvents()).thenReturn(spy);
        verify(spy, never()).trigger("shootLaserStart");
    }

    @Test
    void shouldNotAttackBeforeCooldown() {
        component.create();
        EventHandler spy = spy(bossEvents);
        lenient().when(bossEntity.getEvents()).thenReturn(spy);
        lenient().when(gameTime.getDeltaTime()).thenReturn(3f);

        component.update();
        verify(spy, never()).trigger("shootLaserStart");
    }

    @Test
    void shouldAttackAfterCooldown() {
        component.create();
        EventHandler spy = spy(bossEvents);
        when(bossEntity.getEvents()).thenReturn(spy);
        when(gameTime.getDeltaTime()).thenReturn(7.5f);

        component.update();
        verify(spy).trigger("shootLaserStart");
    }

    @Test
    void shouldResetCooldownAfterAttack() {
        component.create();
        lenient().when(gameTime.getDeltaTime()).thenReturn(7.5f);
        component.update();

        EventHandler spy = spy(bossEvents);
        lenient().when(bossEntity.getEvents()).thenReturn(spy);
        lenient().when(gameTime.getDeltaTime()).thenReturn(0.1f);
        component.update();

        verify(spy, never()).trigger("shootLaserStart");
    }

    @Test
    void shouldAccumulateCooldown() {
        component.create();
        EventHandler spy = spy(bossEvents);
        when(bossEntity.getEvents()).thenReturn(spy);

        when(gameTime.getDeltaTime()).thenReturn(2f);
        component.update();
        component.update();
        component.update();
        verify(spy, never()).trigger("shootLaserStart");

        when(gameTime.getDeltaTime()).thenReturn(1.5f);
        component.update();
        verify(spy).trigger("shootLaserStart");
    }

    @Test
    void shouldCalculateDirectionCorrectly() {
        component.create();
        lenient().when(bossEntity.getCenterPosition()).thenReturn(new Vector2(0, 0));
        lenient().when(playerEntity.getCenterPosition()).thenReturn(new Vector2(10, 0));

        try (MockedStatic<ProjectileFactory> factory = mockStatic(ProjectileFactory.class)) {
            factory.when(() -> ProjectileFactory.createLaserProjectile(
                    any(), any(),
                    argThat(dir -> Math.abs(dir.x - 1f) < 0.01f && Math.abs(dir.y) < 0.01f),
                    anyFloat(), anyInt()
            )).thenReturn(laserProjectile);

            lenient().when(gameTime.getDeltaTime()).thenReturn(7.5f);
            component.update();
        }
    }

    @Test
    void shouldSetTarget() {
        Entity newTarget = mock(Entity.class);
        component.setTarget(newTarget);
        assertEquals(newTarget, component.getTarget());
    }

    @Test
    void shouldHandleNullTarget() {
        component.setTarget(null);
        assertNull(component.getTarget());
    }

    @Test
    void shouldResetCooldownWhenDisabled() {
        component.create();
        lenient().when(gameTime.getDeltaTime()).thenReturn(5f);
        component.update();

        component.setEnabled(false);

        EventHandler spy = spy(bossEvents);
        lenient().when(bossEntity.getEvents()).thenReturn(spy);
        lenient().when(gameTime.getDeltaTime()).thenReturn(0.1f);
        component.update();

        verify(spy, never()).trigger("shootLaserStart");
    }

    @Test
    void shouldHandleMultipleAttacks() {
        component.create();
        EventHandler spy = spy(bossEvents);
        lenient().when(bossEntity.getEvents()).thenReturn(spy);
        lenient().when(gameTime.getDeltaTime()).thenReturn(7.5f);

        component.update();
        component.update();
        verify(spy, times(2)).trigger("shootLaserStart");
    }

    @Test
    void shouldHandleZeroDeltaTime() {
        component.create();
        lenient().when(gameTime.getDeltaTime()).thenReturn(0f);
        assertDoesNotThrow(() -> component.update());
    }

    @Test
    void shouldHandleSmallDeltaTimes() {
        component.create();
        EventHandler spy = spy(bossEvents);
        lenient().when(bossEntity.getEvents()).thenReturn(spy);
        lenient().when(gameTime.getDeltaTime()).thenReturn(0.016f);

        for (int i = 0; i < 440; i++) {
            component.update();
        }
        verify(spy, atLeastOnce()).trigger("shootLaserStart");
    }

    @Test
    void shouldHandleSamePosition() {
        component.create();
        Vector2 pos = new Vector2(10, 10);
        lenient().when(bossEntity.getCenterPosition()).thenReturn(pos);
        lenient().when(playerEntity.getCenterPosition()).thenReturn(pos);

        try (MockedStatic<ProjectileFactory> factory = mockStatic(ProjectileFactory.class)) {
            factory.when(() -> ProjectileFactory.createLaserProjectile(
                    any(), any(), any(), anyFloat(), anyInt()
            )).thenReturn(laserProjectile);

            lenient().when(gameTime.getDeltaTime()).thenReturn(7.5f);
            assertDoesNotThrow(() -> component.update());
        }
    }
}