package com.csse3200.game.components;

import com.csse3200.game.components.obstacles.MoveableBoxComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class BoxPressurePlateComponentTest {
    private Entity plate;
    private BoxPressurePlateComponent plateComponent;
    private TextureRenderComponent renderer;

    @BeforeEach
    void setup() {
        ResourceService resourceService = mock(ResourceService.class);
        when(resourceService.getAsset(anyString(), eq(TextureRenderComponent.class))).thenReturn(renderer);
        ServiceLocator.registerResourceService(resourceService);

        renderer = mock(TextureRenderComponent.class);

        plate = new Entity()
                .addComponent(renderer)
                .addComponent(new BoxPressurePlateComponent());

        plateComponent = plate.getComponent(BoxPressurePlateComponent.class);
        plateComponent.create();

        plate.setPosition(0, 0);
    }

    @Test
    void create_initializesRenderer() {
        assertNotNull(plateComponent);
        assertNotNull(plate.getComponent(TextureRenderComponent.class));
    }

    @Test
    void setEntityOnPlate_playerAbove_triggersPlatePressedAndReleased() {
        Entity player = new Entity().addComponent(new PlayerActions());
        player.setPosition(0, 1f); // above plate

        boolean[] pressed = {false};
        boolean[] released = {false};
        plate.getEvents().addListener("platePressed", () -> pressed[0] = true);
        plate.getEvents().addListener("plateReleased", () -> released[0] = true);

        plateComponent.setEntityOnPlate(player, true);
        assertTrue(pressed[0]);
        verify(renderer, atLeastOnce()).setTexture(any());

        plateComponent.setEntityOnPlate(player, false);
        assertTrue(released[0]);
        verify(renderer, atLeast(2)).setTexture(any());
    }

    @Test
    void setEntityOnPlate_boxAboveObstacleLayer_triggersPlatePressedAndReleased() {
        Entity box = new Entity().addComponent(new MoveableBoxComponent());
        ColliderComponent boxCollider = mock(ColliderComponent.class);
        when(boxCollider.getLayer()).thenReturn(PhysicsLayer.OBSTACLE);
        box.addComponent(boxCollider);
        box.setPosition(0, 1f);

        boolean[] pressed = {false};
        boolean[] released = {false};
        plate.getEvents().addListener("platePressed", () -> pressed[0] = true);
        plate.getEvents().addListener("plateReleased", () -> released[0] = true);

        plateComponent.setEntityOnPlate(box, true);
        assertTrue(pressed[0]);
        verify(renderer, atLeastOnce()).setTexture(any());

        plateComponent.setEntityOnPlate(box, false);
        assertTrue(released[0]);
        verify(renderer, atLeast(2)).setTexture(any());
    }

    @Test
    void setEntityOnPlate_invalidEntity_doesNotTrigger() {
        Entity invalid = new Entity();
        invalid.setPosition(0, 1f);

        plateComponent.setEntityOnPlate(invalid, true);
        plateComponent.setEntityOnPlate(invalid, false);

        verify(renderer, atMost(1)).setTexture(any());
    }

    @Test
    void setEntityOnPlate_multipleEntities_onlyReleasesWhenAllRemoved() {
        Entity box1 = new Entity().addComponent(new MoveableBoxComponent());
        ColliderComponent boxCollider1 = mock(ColliderComponent.class);
        when(boxCollider1.getLayer()).thenReturn(PhysicsLayer.OBSTACLE);
        box1.addComponent(boxCollider1);
        box1.setPosition(0, 1f);

        Entity box2 = new Entity().addComponent(new MoveableBoxComponent());
        ColliderComponent boxCollider2 = mock(ColliderComponent.class);
        when(boxCollider2.getLayer()).thenReturn(PhysicsLayer.OBSTACLE);
        box2.addComponent(boxCollider2);
        box2.setPosition(0, 1.5f);

        boolean[] released = {false};
        plate.getEvents().addListener("plateReleased", () -> released[0] = true);

        plateComponent.setEntityOnPlate(box1, true);
        plateComponent.setEntityOnPlate(box2, true);

        plateComponent.setEntityOnPlate(box1, false);
        assertFalse(released[0]);

        plateComponent.setEntityOnPlate(box2, false);
        assertTrue(released[0]);
    }

    @Test
    void setEntityOnPlate_entityBelowPlate_doesNotPress() {
        Entity box = new Entity().addComponent(new MoveableBoxComponent());
        ColliderComponent boxCollider = mock(ColliderComponent.class);
        when(boxCollider.getLayer()).thenReturn(PhysicsLayer.OBSTACLE);
        box.addComponent(boxCollider);
        box.setPosition(0, -1f);

        plateComponent.setEntityOnPlate(box, true);
        verify(renderer, atMost(1)).setTexture(any());
    }
}
