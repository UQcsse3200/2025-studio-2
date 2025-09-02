package com.csse3200.game.components;


import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class DisposalComponentTest {
    private GameTime mockTime;

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();
        mockTime = mock(GameTime.class);
        ServiceLocator.registerTimeSource(mockTime);
    }

    @Test
    void shouldRegisterEventOnCreate() {
        Entity e = new Entity().addComponent(new DisposalComponent(1f));
        e.create();

        e.getEvents().trigger("scheduleDisposal");
        verify(mockTime, times(1)).getTime();
    }

    @Test
    void shouldDisposeAfterDelay() {
        when(mockTime.getTime()).thenReturn(100L);
        when(mockTime.getTimeSince(100L)).thenReturn(20000L);

        final boolean[] disposed = {false};
        Entity e = new Entity() {
            @Override public void dispose() {disposed[0] = true;}
        };
        e.addComponent(new DisposalComponent(1f));
        e.create();
        e.getEvents().trigger("scheduleDisposal");
        e.update();
        assertTrue(disposed[0], "Entity should be disposed after delay");
    }

    @Test
    void shouldNotDisposeBeforeDelay() {
        when(mockTime.getTime()).thenReturn(100L);
        when(mockTime.getTimeSince(100L)).thenReturn(500L); // 0.5 sec

        final boolean[] disposed = {false};
        Entity e = new Entity() {
            @Override public void dispose() {disposed[0] = true;}
        };
        e.addComponent(new DisposalComponent(1f));
        e.create();
        e.getEvents().trigger("scheduleDisposal");
        e.update();
        assertFalse(disposed[0], "Entity should not be disposed before delay finishes");
    }
}
