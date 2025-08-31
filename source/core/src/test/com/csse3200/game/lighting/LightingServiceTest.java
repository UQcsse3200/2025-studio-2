package com.csse3200.game.lighting;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(GameExtension.class)
class LightingServiceTest {
    @Test
    void getEngine_shouldReturnInjectedEngine() {
        LightingEngine engine = mock(LightingEngine.class);
        LightingService lightingService = new LightingService(engine);
        assertEquals(engine, lightingService.getEngine());
    }

}