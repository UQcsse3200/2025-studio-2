package com.csse3200.game.lighting;

import box2dLight.RayHandler;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class LightingEngineTest {

    private CameraComponent camera;

    @BeforeEach
    void setUp() {
        camera = new CameraComponent();
    }

    @Test
    void getRayHandlerNotNull() {
        RayHandler rh = mock(RayHandler.class);
        LightingEngine engine = new LightingEngine(rh, camera);
        assertNotNull(engine.getRayHandler());
    }

    @Test
    void renderShouldUseCameraMatrixAndUpdate() throws Exception{
        RayHandler rh = mock(RayHandler.class);
        LightingEngine engine = new LightingEngine(rh, camera);

        engine.render();

        verify(rh).setCombinedMatrix(same(camera.getProjectionMatrix()));
        verify(rh).updateAndRender();
        verifyNoMoreInteractions(rh);
    }

    @Test
    void disposeShouldDispose() throws Exception {
        RayHandler rh = mock(RayHandler.class);
        LightingEngine engine = new LightingEngine(rh, camera);

        engine.dispose();
        verify(rh).dispose();
    }
}