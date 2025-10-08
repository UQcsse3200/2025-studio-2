package com.csse3200.game.components.lasers;

import com.csse3200.game.components.lighting.ConeLightComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class LaserDetectorComponentTest {

    private Entity detectorEntity;
    private LaserDetectorComponent detector;
    private ConeLightComponent light;

    @BeforeEach
    void setUp() throws Exception {
        detectorEntity = new Entity();
        detector = new LaserDetectorComponent();
        light = mock(ConeLightComponent.class);

        detectorEntity.addComponent(detector);
        detectorEntity.addComponent(light);

        // inject mock light forcefully to avoid create() calls
        Field f = LaserDetectorComponent.class.getDeclaredField("light");
        f.setAccessible(true);
        f.set(detector, light);
    }

    @Test
    void update_firstTickTurnsLightOffOnce() {
        reset(light);
        detector.update();
        verify(light, times(1)).setActive(false);

        // second update should not call it again
        detector.update();
        verifyNoMoreInteractions(light);
    }

    @Test
    void updateDetection_true_turnsLightOnOnce() throws Exception {
        AtomicInteger startCount = new AtomicInteger(0);
        AtomicInteger endCount = new AtomicInteger(0);
        detectorEntity.getEvents().addListener("detectingStart", startCount::incrementAndGet);
        detectorEntity.getEvents().addListener("detectingEnd", endCount::incrementAndGet);

        // call the private updateDetection(boolean) directly
        callUpdateDetection(detector, true);

        // ensure light is only turned on once
        verify(light, times(1)).setActive(true);
        assertEquals(1, startCount.get());
        assertEquals(0, endCount.get());

        // calling updateDetection again shouldn't trigger another event
        callUpdateDetection(detector, true);
        assertEquals(1, startCount.get());
        assertEquals(0, endCount.get());
    }

    @Test
    void updateDetection_false_turnsLightOffOnce()  throws Exception {
        AtomicInteger startCount = new AtomicInteger(0);
        AtomicInteger endCount = new AtomicInteger(0);
        detectorEntity.getEvents().addListener("detectingStart", startCount::incrementAndGet);
        detectorEntity.getEvents().addListener("detectingEnd", endCount::incrementAndGet);

        // first go to 'true' so the internal state flips to detecting
        callUpdateDetection(detector, true);
        startCount.set(0);
        endCount.set(0);

        // now flip to false
        callUpdateDetection(detector, false);

        verify(light, times(1)).setActive(false);
        assertEquals(0, startCount.get());
        assertEquals(1, endCount.get());

        // calling updateDetection again doesn't trigger another event
        callUpdateDetection(detector, false);
        assertEquals(0, startCount.get());
        assertEquals(1, endCount.get());

    }

    private static void callUpdateDetection(LaserDetectorComponent comp, boolean v) throws Exception {
        Method m = LaserDetectorComponent.class.getDeclaredMethod("updateDetection", boolean.class);
        m.setAccessible(true);
        m.invoke(comp, v);
    }

}