package com.csse3200.game.components.platforms;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class VolatilePlatformComponentTest {
    private Entity platformEntity;
    private VolatilePlatformComponent platform;
    private ColliderComponent collider;
    private TextureRenderComponent texture;
    private GameTime mockTime;
    private AnimationRenderComponent animator;
    private Fixture fixture;
    private Body body;

    @BeforeEach
    void setup() {
        mockTime = mock(GameTime.class);
        ServiceLocator.registerTimeSource(mockTime);

        platformEntity = new Entity();
        collider = mock(ColliderComponent.class);
        texture = mock(TextureRenderComponent.class);
        animator = mock(AnimationRenderComponent.class);

        fixture = mock(Fixture.class);
        body = mock(Body.class);
        when(fixture.getBody()).thenReturn(body);
        when(collider.getFixture()).thenReturn(fixture);

        platformEntity.addComponent(collider);
        platformEntity.addComponent(texture);
        platformEntity.addComponent(animator);

        platform = new VolatilePlatformComponent(2f, 3f);
        platformEntity.addComponent(platform);
        platformEntity.create();
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    void volatilePlatform_initiallyVisible() {
        platform.create();

        verify(texture, atLeastOnce()).setTexture("images/platform.png");
        verify(collider, atLeastOnce()).setSensor(false);
    }

    @Test
    void volatilePlatform_hasAllComponents() {
        assertNotNull(platformEntity.getComponent(ColliderComponent.class));
        assertNotNull(platformEntity.getComponent(TextureRenderComponent.class));
        assertNotNull(platformEntity.getComponent(VolatilePlatformComponent.class));
    }

    @Test
    void setVisible_showsAndHidesPlatform() {
        platform.setVisible(true);
        verify(collider, atLeastOnce()).setSensor(false);
        verify(texture, atLeastOnce()).setTexture("images/platform.png");

        platform.setVisible(false);
        verify(collider, atLeastOnce()).setSensor(true);
        verify(texture, atLeastOnce()).setTexture("images/empty.png");
    }

    @Test
    void update_respawnsPlatformAfterDelay() {
        platform.setVisible(false);

        when(mockTime.getTime()).thenReturn(1000L + 3100L);
        platform.update();

        verify(texture, atLeastOnce()).setTexture("images/platform.png");
        verify(collider, atLeastOnce()).setSensor(false);
    }

    @Test
    void linkToPlate_triggersVisibility() {
        Entity plate = new Entity();
        platform.linkToPlate(plate);

        plate.getEvents().trigger("platePressed");
        verify(texture, atLeastOnce()).setTexture("images/platform.png");

        plate.getEvents().trigger("plateReleased");
        verify(texture, atLeastOnce()).setTexture("images/empty.png");
    }

    @Test
    void linkedPlatform_initiallyHidden() {
        Entity plate = new Entity();
        platform.linkToPlate(plate);

        verify(texture, atLeastOnce()).setTexture("images/empty.png");
        verify(collider, atLeastOnce()).setSensor(true);
    }

    @Test
    void linkedPlatform_updateDoesNotChangeVisibilityWithoutPlate() {
        Entity plate = new Entity();
        platform.linkToPlate(plate);

        when(mockTime.getTime()).thenReturn(10000L);
        platform.update();

        verify(texture, atLeastOnce()).setTexture("images/empty.png");
        verify(collider, atLeastOnce()).setSensor(true);
    }

    @Test
    void linkedPlatform_respondsToPlateEvents() {
        Entity plate = new Entity();
        platform.linkToPlate(plate);

        plate.getEvents().trigger("platePressed");
        verify(texture, atLeastOnce()).setTexture("images/platform.png");
        verify(collider, atLeastOnce()).setSensor(false);

        plate.getEvents().trigger("plateReleased");
        verify(texture, atLeastOnce()).setTexture("images/empty.png");
        verify(collider, atLeastOnce()).setSensor(true);
    }
}
