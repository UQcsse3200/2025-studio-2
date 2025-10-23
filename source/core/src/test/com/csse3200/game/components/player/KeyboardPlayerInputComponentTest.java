package com.csse3200.game.components.player;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.ladders.LadderComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import com.csse3200.game.input.Keymap;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(GameExtension.class)
class KeyboardPlayerInputComponentTest {

    @Test
    void testInFrontOfLadder() throws Exception {

        KeyboardPlayerInputComponent input = new KeyboardPlayerInputComponent();

        Entity ladder = new Entity();
        ladder.setPosition(5f, 0f);
        ladder.setScale(1f, 3f);
        ladder.addComponent(new LadderComponent());

        Entity player = new Entity();
        player.setPosition(5f, 1f); // approx in front with ladder
        player.setScale(1f, 2f);
        input.setEntity(player);

        Array<Entity> ladders = new Array<>();
        ladders.add(ladder);

        // Use reflection to access private method
        Method method = KeyboardPlayerInputComponent.class
                .getDeclaredMethod("inFrontOfLadder", Array.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(input, ladders);
        assertTrue(result);
    }

    @Test
    void testNotInFrontOfLadder_HorizontalError() throws Exception {
        KeyboardPlayerInputComponent input = new KeyboardPlayerInputComponent();

        Entity ladder = new Entity();
        ladder.setPosition(5f, 0f);
        ladder.setScale(1f, 3f);
        ladder.addComponent(new LadderComponent());

        Entity player = new Entity();
        player.setPosition(10f, 1f); //  away horizontally
        player.setScale(1f, 2f);
        input.setEntity(player);

        Array<Entity> ladders = new Array<>();
        ladders.add(ladder);

        Method method = KeyboardPlayerInputComponent.class
                .getDeclaredMethod("inFrontOfLadder", Array.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(input, ladders);
        assertFalse(result);
        assertFalse(input.getOnLadder());
    }
    @Test
    void glideDoesNotFireWithoutInventory() {
        if (ServiceLocator.getEntityService() == null) {
            ServiceLocator.registerEntityService(new EntityService());
        }

        Entity player = new Entity();
        var input = new KeyboardPlayerInputComponent();

        // bind component without creating the entity (avoids InputService etc.)
        input.setEntity(player);

        // register player so findLadders() can iterate safely
        ServiceLocator.getEntityService().register(player);

        AtomicInteger glideCalls = new AtomicInteger(0);
        player.getEvents().addListener("glide", (Boolean b) -> glideCalls.incrementAndGet());

        int glideKey = Keymap.getActionKeyCode("Glide");
        input.keyDown(glideKey);
        input.keyUp(glideKey);

        assertEquals(0, glideCalls.get());
    }


    @Test
    void dashDoesNotFireWithoutInventory() {
        if (ServiceLocator.getEntityService() == null) {
            ServiceLocator.registerEntityService(new EntityService());
        }

        Entity player = new Entity();
        var input = new KeyboardPlayerInputComponent();

        // bind component without full create()
        input.setEntity(player);
        ServiceLocator.getEntityService().register(player);

        AtomicBoolean fired = new AtomicBoolean(false);
        player.getEvents().addListener("dash", () -> fired.set(true));

        int dashKey = Keymap.getActionKeyCode("PlayerDash");
        input.keyDown(dashKey);

        assertFalse(fired.get(), "Dash must not fire without the 'dash' upgrade");
    }


    @Test
    void testNotInFrontOfLadder_VerticalError() throws Exception {
        KeyboardPlayerInputComponent input = new KeyboardPlayerInputComponent();

        Entity ladder = new Entity();
        ladder.setPosition(5f, 0f);
        ladder.setScale(1f, 3f);
        ladder.addComponent(new LadderComponent());

        Entity player = new Entity();
        player.setPosition(5f, 4f); // above ladder
        player.setScale(1f, 2f);
        input.setEntity(player);

        Array<Entity> ladders = new Array<>();
        ladders.add(ladder);

        Method method = KeyboardPlayerInputComponent.class
                .getDeclaredMethod("inFrontOfLadder", Array.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(input, ladders);
        assertFalse(result);
        assertFalse(input.getOnLadder());
    }

    @Test
    void testMultipleLadders() throws Exception {
        KeyboardPlayerInputComponent input = new KeyboardPlayerInputComponent();

        Entity ladder1 = new Entity();
        ladder1.setPosition(0f, 0f);
        ladder1.setScale(1f, 3f);
        ladder1.addComponent(new LadderComponent());

        Entity ladder2 = new Entity();
        ladder2.setPosition(5f, 0f);
        ladder2.setScale(1f, 3f);
        ladder2.addComponent(new LadderComponent());

        Entity player = new Entity();
        player.setPosition(5f, 1f); // in front of ladder2
        player.setScale(1f, 2f);
        input.setEntity(player);

        Array<Entity> ladders = new Array<>();
        ladders.add(ladder1);
        ladders.add(ladder2);

        Method method = KeyboardPlayerInputComponent.class
                .getDeclaredMethod("inFrontOfLadder", Array.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(input, ladders);
        assertTrue(result);
        assertTrue(input.getOnLadder());
    }

    @Test
    void testNoLadders() throws Exception {
        KeyboardPlayerInputComponent input = new KeyboardPlayerInputComponent();

        Entity player = new Entity();
        player.setPosition(5f, 1f);
        player.setScale(1f, 2f);
        input.setEntity(player);

        Array<Entity> ladders = new Array<>();

        Method method = KeyboardPlayerInputComponent.class
                .getDeclaredMethod("inFrontOfLadder", Array.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(input, ladders);
        assertFalse(result);
        assertFalse(input.getOnLadder());
    }
}
