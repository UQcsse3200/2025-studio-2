package com.csse3200.game.components.obstacles;

import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.CollectablesConfig;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.CollectableService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DoorComponentTest {

    private GameArea area;
    private EventHandler areaEvents;
    private MockedStatic<CollectableService> svcMock;

    @BeforeEach
    void setUp() {
        area = mock(GameArea.class);
        areaEvents = mock(EventHandler.class);
        when(area.getEvents()).thenReturn(areaEvents);

        // Mock static CollectableService so InventoryComponent can add items
        svcMock = mockStatic(CollectableService.class, withSettings());
        svcMock.when(() -> CollectableService.get(anyString()))
                .thenAnswer(inv -> {
                    String id = inv.getArgument(0);
                    CollectablesConfig c = new CollectablesConfig();
                    c.id = id;
                    c.sprite = "";
                    c.effects = List.of();
                    c.autoConsume = false; // keys
                    return c;
                });
    }

    @AfterEach
    void tearDown() {
        if (svcMock != null) svcMock.close();
    }

    // --- helpers ---

    /** Player with mocked PLAYER hitbox and inventory. */
    private Entity newPlayer(boolean withInventory) {
        Entity player = new Entity();

        HitboxComponent hb = mock(HitboxComponent.class);
        when(hb.getLayer()).thenReturn(PhysicsLayer.PLAYER);
        player.addComponent(hb);

        if (withInventory) {
            player.addComponent(new InventoryComponent());
        }
        player.create();
        return player;
    }

    /** Door with mocked animator + collider + OBSTACLE hitbox. */
    private Entity newDoor(String keyId, boolean isStatic, String targetArea) {
        Entity door = new Entity();

        AnimationRenderComponent anim = mock(AnimationRenderComponent.class);
        when(anim.isFinished()).thenReturn(false);

        ColliderComponent collider = mock(ColliderComponent.class);

        HitboxComponent hb = mock(HitboxComponent.class);
        when(hb.getLayer()).thenReturn(PhysicsLayer.OBSTACLE);

        door.addComponent(new DoorComponent(keyId, area, isStatic, targetArea));
        door.addComponent(anim);
        door.addComponent(collider);
        door.addComponent(hb);
        door.create();
        return door;
    }

    /** Door with NO animator (to hit null-anim guards). */
    private Entity newDoorNoAnim(String keyId, boolean isStatic, String targetArea) {
        Entity door = new Entity();

        ColliderComponent collider = mock(ColliderComponent.class);
        HitboxComponent hb = mock(HitboxComponent.class);
        when(hb.getLayer()).thenReturn(PhysicsLayer.OBSTACLE);

        door.addComponent(new DoorComponent(keyId, area, isStatic, targetArea));
        door.addComponent(collider);
        door.addComponent(hb);
        door.create();
        return door;
    }

    private void collide(Entity door, Entity player) {
        door.getEvents().trigger("onCollisionStart", player);
    }

    private void finishOpening(Entity door) {
        AnimationRenderComponent anim = door.getComponent(AnimationRenderComponent.class);
        if (anim != null) when(anim.isFinished()).thenReturn(true);
        door.getComponent(DoorComponent.class).update();
    }

    // --- tests ---

    @Test
    void create_setsClosedAnimation() {
        Entity door = newDoor("key:door", true, "");
        verify(door.getComponent(AnimationRenderComponent.class), atLeastOnce())
                .startAnimation("door_closed");
    }

    @Test
    void remainsLockedWithoutKey_onCollision() {
        Entity player = newPlayer(true); // inventory present but no key
        Entity door = newDoor("key:door", true, "");
        DoorComponent dc = door.getComponent(DoorComponent.class);

        collide(door, player);

        assertTrue(dc.isLocked(), "Should remain locked without key");
        verify(area.getEvents(), never()).trigger(eq("doorEntered"), any(), any());
    }

    @Test
    void staticDoor_unlocks_consumesKey_animates_and_noTransition() {
        Entity player = newPlayer(true);
        player.getComponent(InventoryComponent.class)
                .addItems(InventoryComponent.Bag.INVENTORY, "key:door", 1);

        Entity door = newDoor("key:door", true, "");
        DoorComponent dc = door.getComponent(DoorComponent.class);
        AnimationRenderComponent anim = door.getComponent(AnimationRenderComponent.class);
        ColliderComponent collider = door.getComponent(ColliderComponent.class);

        collide(door, player);

        verify(anim, atLeastOnce()).startAnimation("door_opening");
        assertFalse(player.getComponent(InventoryComponent.class)
                .hasItem(InventoryComponent.Bag.INVENTORY, "key:door"));

        finishOpening(door);

        verify(anim, atLeastOnce()).startAnimation("door_open");
        verify(collider, atLeastOnce()).setSensor(true);
        assertFalse(dc.isLocked(), "Door unlocked");
        verify(area.getEvents(), never()).trigger(eq("doorEntered"), any(), any());
    }

    @Test
    void transitionDoor_emitsDoorEntered_once_onAnimationFinish() {
        Entity player = newPlayer(true);
        player.getComponent(InventoryComponent.class)
                .addItems(InventoryComponent.Bag.INVENTORY, "key:door", 1);
        when(area.getPlayer()).thenReturn(player);

        Entity door = newDoor("key:door", false, "level2");
        AnimationRenderComponent anim = door.getComponent(AnimationRenderComponent.class);
        ColliderComponent collider = door.getComponent(ColliderComponent.class);

        collide(door, player);
        verify(anim, atLeastOnce()).startAnimation("door_opening");

        finishOpening(door);

        verify(anim, atLeastOnce()).startAnimation("door_open");
        verify(collider, atLeastOnce()).setSensor(true);
        verify(area.getEvents(), times(1)).trigger(eq("doorEntered"), eq(player), eq(door));

        // Re-colliding should not emit again
        collide(door, player);
        verify(area.getEvents(), times(1)).trigger(eq("doorEntered"), any(), any());
    }

    @Test
    void transitionDoor_withEmptyTarget_doesNotEmitDoorEntered() {
        Entity player = newPlayer(true);
        player.getComponent(InventoryComponent.class)
                .addItems(InventoryComponent.Bag.INVENTORY, "key:door", 1);
        when(area.getPlayer()).thenReturn(player);

        Entity door = newDoor("key:door", false, "   ");
        collide(door, player);
        finishOpening(door);

        verify(area.getEvents(), never()).trigger(eq("doorEntered"), any(), any());
    }

    @Test
    void transitionDoor_withoutAreaPlayer_doesNotEmitDoorEntered() {
        Entity player = newPlayer(true);
        player.getComponent(InventoryComponent.class)
                .addItems(InventoryComponent.Bag.INVENTORY, "key:door", 1);
        when(area.getPlayer()).thenReturn(null); // suppress emission

        Entity door = newDoor("key:door", false, "levelX");
        collide(door, player);
        finishOpening(door);

        verify(area.getEvents(), never()).trigger(eq("doorEntered"), any(), any());
    }

    @Test
    void tryUnlock_noInventory_doesNothing() {
        Entity playerNoInv = newPlayer(false); // no inventory
        Entity door = newDoor("key:door", true, "");
        DoorComponent dc = door.getComponent(DoorComponent.class);
        AnimationRenderComponent anim = door.getComponent(AnimationRenderComponent.class);

        dc.tryUnlock(playerNoInv);
        assertTrue(dc.isLocked(), "Still locked without an inventory");
        verify(anim, never()).startAnimation("door_opening");
    }

    @Test
    void tryUnlock_missingKey_doesNothing() {
        Entity player = newPlayer(true); // inventory present, no key
        Entity door = newDoor("key:door", true, "");
        DoorComponent dc = door.getComponent(DoorComponent.class);
        AnimationRenderComponent anim = door.getComponent(AnimationRenderComponent.class);

        dc.tryUnlock(player);
        assertTrue(dc.isLocked(), "Still locked when key missing");
        verify(anim, never()).startAnimation("door_opening");
    }

    @Test
    void openDoor_thenUpdate_unfinishedAnim_noTransition() {
        Entity player = newPlayer(true);
        when(area.getPlayer()).thenReturn(player);

        Entity door = newDoor("key:door", false, "next");
        DoorComponent dc = door.getComponent(DoorComponent.class);
        AnimationRenderComponent anim = door.getComponent(AnimationRenderComponent.class);

        dc.openDoor();
        verify(anim, atLeastOnce()).startAnimation("door_opening");
        assertFalse(dc.isLocked());

        dc.update(); // not finished yet
        verify(anim, never()).startAnimation("door_open");
        verify(area.getEvents(), never()).trigger(eq("doorEntered"), any(), any());
    }

    @Test
    void closeDoor_resetsClosedAnim_andLocks() {
        Entity door = newDoor("key:door", false, "next");
        DoorComponent dc = door.getComponent(DoorComponent.class);
        AnimationRenderComponent anim = door.getComponent(AnimationRenderComponent.class);

        dc.openDoor();
        verify(anim, atLeastOnce()).startAnimation("door_opening");

        door.getEvents().trigger("closeDoor");
        verify(anim, atLeastOnce()).startAnimation("door_closed");
        assertTrue(dc.isLocked(), "Door locked after close");
    }

    @Test
    void noAnimator_unlocksAndSurvivesUpdate_noEvent() {
        Entity player = newPlayer(true);
        player.getComponent(InventoryComponent.class)
                .addItems(InventoryComponent.Bag.INVENTORY, "key:door", 1);
        when(area.getPlayer()).thenReturn(player);

        Entity door = newDoorNoAnim("key:door", false, "levelX");
        DoorComponent dc = door.getComponent(DoorComponent.class);

        collide(door, player);
        dc.update(); // no animator -> no onAnimationFinished -> no event
        assertFalse(dc.isLocked(), "Door unlocked without animator present");
        verify(area.getEvents(), never()).trigger(eq("doorEntered"), any(), any());
    }
}
