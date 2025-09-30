package com.csse3200.game.components.obstacles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Filter;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class MoveableBoxComponentTest {

    private Entity box;
    private MoveableBoxComponent boxComponent;

    private Entity player;
    private ColliderComponent playerCollider;

    private long time = 0L;

    @BeforeEach
    void setup() {
        // mock services
        ServiceLocator.registerPhysicsService(new PhysicsService());

        ResourceService resourceService = mock(ResourceService.class);
        when(resourceService.getAsset(anyString(), any())).thenReturn(null);
        ServiceLocator.registerResourceService(resourceService);

        RenderService renderService = mock(RenderService.class);
        ServiceLocator.registerRenderService(renderService);

        GameTime gameTime = mock(GameTime.class);
        when(gameTime.getTime()).thenReturn(time);
        when(gameTime.getDeltaTime()).thenReturn(0.5f);
        ServiceLocator.registerTimeSource(gameTime);

        // create box
        box = new Entity();
        boxComponent = new MoveableBoxComponent();

        box.addComponent(new PhysicsComponent())
           .addComponent(new ColliderComponent())
           .addComponent(new TextureRenderComponent((String) null))
           .addComponent(boxComponent);
        box.create();

        // create new player entity
        player = new Entity();
        playerCollider = new ColliderComponent();
        player.addComponent(playerCollider);
        player.addComponent(new KeyboardPlayerInputComponent());
    }

    @Test
    void create_shouldThrowIfMissingComponents() {
        Entity badBox = new Entity().addComponent(new MoveableBoxComponent());
        assertThrows(IllegalStateException.class, badBox::create);
    }

    @Test
    void update_shouldApplyDefaultFilterOnInitUpdate() {
        boxComponent.update();
        Filter filter = box.getComponent(ColliderComponent.class).getFixture().getFilterData();

        assertEquals(PhysicsLayer.OBSTACLE, filter.categoryBits);
        assertTrue((filter.maskBits & PhysicsLayer.PLAYER) != 0);
        assertTrue((filter.maskBits & PhysicsLayer.NPC) != 0);
        assertTrue((filter.maskBits & PhysicsLayer.LASER_REFLECTOR) != 0);
    }

    @Test
    void update_shouldRespectPhysicsLayerOverride() {
        boxComponent.setPhysicsLayer(PhysicsLayer.LASER_REFLECTOR);
        boxComponent.update();

        Filter filter = box.getComponent(ColliderComponent.class).getFixture().getFilterData();
        assertEquals(PhysicsLayer.LASER_REFLECTOR, filter.categoryBits);
    }

    @Test
    void update_shouldResetIfOutOfBounds() {
        float startX = box.getPosition().x;
        float startY = box.getPosition().y;

        // drop box below -5f (box kill plane)
        box.setPosition(startX, -6f);
        boxComponent.update();

        assertEquals(startX, box.getPosition().x, 1e-4);
        assertEquals(startY, box.getPosition().y, 1e-4);
    }

    @Test
    void interact_shouldPickUpAndDropOff() throws Exception{
        // box should start not picked up
        Field f = MoveableBoxComponent.class.getDeclaredField("pickedUp");
        f.setAccessible(true);

        // forcefully register player with box (usually done by init collision)
        boxComponent.setPlayerInRange(playerCollider);

        boolean pickedUp = (boolean) f.get(boxComponent);
        assertFalse(pickedUp);

        // trigger interaction
        player.getEvents().trigger("interact");

        pickedUp = (boolean) f.get(boxComponent);
        assertTrue(pickedUp);

        // trigger interaction
        player.getEvents().trigger("interact");

        pickedUp = (boolean) f.get(boxComponent);
        assertFalse(pickedUp);
    }
}