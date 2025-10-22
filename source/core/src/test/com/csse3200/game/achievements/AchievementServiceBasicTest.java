package com.csse3200.game.achievements;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Gdx;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** Pure logic tests for AchievementService – no Scene2D, no real assets. */
public class AchievementServiceBasicTest {

    // Minimal in-memory Preferences so Gdx.app.getPreferences() has something to return.
    static class FakePreferences implements Preferences {
        private final Map<String, Object> map = new HashMap<>();
        @Override public Preferences putBoolean(String key, boolean val){ map.put(key, val); return this; }
        @Override public Preferences putInteger(String key, int val){ map.put(key, val); return this; }
        @Override public Preferences putLong(String key, long val){ map.put(key, val); return this; }
        @Override public Preferences putFloat(String key, float val){ map.put(key, val); return this; }
        @Override public Preferences putString(String key, String val){ map.put(key, val); return this; }
        @Override public Preferences put(Map<String, ?> vals){ map.putAll(vals); return this; }
        @Override public boolean getBoolean(String key){ return getBoolean(key, false); }
        @Override public boolean getBoolean(String key, boolean def){ Object v=map.get(key); return v instanceof Boolean?(Boolean)v:def; }
        @Override public int getInteger(String key){ return getInteger(key, 0); }
        @Override public int getInteger(String key, int def){ Object v=map.get(key); return v instanceof Integer?(Integer)v:def; }
        @Override public long getLong(String key){ return getLong(key, 0L); }
        @Override public long getLong(String key, long def){ Object v=map.get(key); return v instanceof Long?(Long)v:def; }
        @Override public float getFloat(String key){ return getFloat(key, 0f); }
        @Override public float getFloat(String key, float def){ Object v=map.get(key); return v instanceof Float?(Float)v:def; }
        @Override public String getString(String key){ return getString(key, ""); }
        @Override public String getString(String key, String def){ Object v=map.get(key); return v instanceof String?(String)v:def; }
        @Override public Map<String, ?> get(){ return map; }
        @Override public boolean contains(String key){ return map.containsKey(key); }
        @Override public void clear(){ map.clear(); }
        @Override public void remove(String key){ map.remove(key); }
        @Override public void flush(){ /* no-op for test */ }
    }

    @BeforeClass
    public static void bootGdxPrefs() {
        if (Gdx.app == null) {
            Application app = mock(Application.class);
            when(app.getPreferences(anyString())).thenAnswer(inv -> new FakePreferences());
            Gdx.app = app;
        }
    }

    @Before
    public void resetSingleton() throws Exception {
        // Force a fresh AchievementService instance each test
        Field f = AchievementService.class.getDeclaredField("INSTANCE");
        f.setAccessible(true);
        f.set(null, null);
    }

    @Test
    public void sprintTimeAccumulatesAndDoesNotCrash() {
        AchievementService svc = AchievementService.get();
        assertEquals(0f, svc.getSprintSeconds(), 0.0001f);

        svc.addSprintTime(1.25f);
        svc.addSprintTime(0.75f);
        assertEquals(2.0f, svc.getSprintSeconds(), 0.0001f);

        // Negative/zero deltas should not reduce total nor crash
        svc.addSprintTime(0f);
        svc.addSprintTime(-5f);
        assertEquals(2.0f, svc.getSprintSeconds(), 0.0001f);
    }

    @Test
    public void staminaMasterUnlocksOnlyIfNotExhausted() {
        AchievementService svc = AchievementService.get();

        // Case A: exhausted during level -> should NOT unlock STAMINA_MASTER
        svc.onLevelStarted();
        svc.markStaminaExhausted();
        svc.onLevelCompleted(1);
        assertFalse(svc.isUnlocked(AchievementId.STAMINA_MASTER));

        // Also level 1 completion should be recorded regardless of exhaustion
        assertTrue(svc.isUnlocked(AchievementId.LEVEL_1_COMPLETE));

        // Reset for next level run
        svc.onLevelStarted();
        // Do not exhaust this time
        svc.onLevelCompleted(2);
        assertTrue(svc.isUnlocked(AchievementId.STAMINA_MASTER));
        assertTrue(svc.isUnlocked(AchievementId.LEVEL_2_COMPLETE));
    }
}
