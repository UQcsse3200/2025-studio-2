package com.csse3200.game.components;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ButtonManagerComponentTest {
    private ButtonManagerComponent manager;
    private GameTime mockTime;

    @BeforeEach
    void setUp() {
        mockTime = mock(GameTime.class);
        when(mockTime.getDeltaTime()).thenReturn(1f);
        ServiceLocator.registerTimeSource(mockTime);

        manager = new ButtonManagerComponent();

        Entity dummyEntity = new Entity().addComponent(manager);
        dummyEntity.create();
        
        EntityService entityService = new EntityService();
        ServiceLocator.registerEntityService(entityService);
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    private ButtonComponent createMockButton(boolean pushed) {
        ButtonComponent button = mock(ButtonComponent.class);
        when(button.isPushed()).thenReturn(pushed);
        return button;
    }

    @Test
    void addButton_addsCorrectly() {
        ButtonComponent button = createMockButton(false);
        manager.addButton(button);

        List<ButtonComponent> buttons = manager.getButtons();
        assertEquals(1, buttons.size());
        assertTrue(buttons.contains(button));
    }

    @Test
    void onButtonPressed_startsTimer() {
        manager.onButtonPressed();
        manager.update();
        assertTrue(manager.getTimeLeft() < 15f, "Puzzle timer should have started and decremented");
    }

    @Test
    void update_allButtonsPressed_triggersPuzzleCompleted() {
        ButtonComponent btn1 = createMockButton(true);
        ButtonComponent btn2 = createMockButton(true);

        manager.addButton(btn1);
        manager.addButton(btn2);

        final boolean[] triggered = {false};
        manager.getEntity().getEvents().addListener("puzzleCompleted", () -> triggered[0] = true);
        manager.onButtonPressed();
        manager.update();

        assertTrue(triggered[0], "Should trigger puzzleCompleted event");
    }

    @Test
    void update_notAllPressed_timerExpires_unpressesAll() {
        ButtonComponent btn1 = createMockButton(false);
        ButtonComponent btn2 = createMockButton(true);

        doNothing().when(btn1).forceUnpress();
        doNothing().when(btn2).forceUnpress();

        manager.addButton(btn1);
        manager.addButton(btn2);
        manager.onButtonPressed();

        when(mockTime.getDeltaTime()).thenReturn(16f);
        manager.update();

        verify(btn1).forceUnpress();
        verify(btn2).forceUnpress();
    }

    @Test
    void resetPuzzle_resetsAllButtonsAndTimer() {
        ButtonComponent btn = createMockButton(true);
        doNothing().when(btn).forceUnpress();

        manager.addButton(btn);
        manager.onButtonPressed();
        manager.update();
        manager.resetPuzzle();

        assertEquals(0f, manager.getTimeLeft());
        assertFalse(manager.isPuzzleCompleted());
        verify(btn).forceUnpress();
    }

    @Test
    void getTimeLeft_returnsZeroIfInactive() {
        assertEquals(0f, manager.getTimeLeft(), "Should return 0 if puzzle inactive");
    }

    @Test
    void isPuzzleCompleted_returnsFalseInitially() {
        assertFalse(manager.isPuzzleCompleted(), "Puzzle should not be completed initially");
    }

    @Test
    void isPuzzleCompleted_returnsTrueAfterSuccess() {
        ButtonComponent btn1 = createMockButton(true);
        ButtonComponent btn2 = createMockButton(true);
        manager.addButton(btn1);
        manager.addButton(btn2);
        manager.onButtonPressed();
        manager.update();
        assertTrue(manager.isPuzzleCompleted(), "Puzzle should be marked completed");
    }
}