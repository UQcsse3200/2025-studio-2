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

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;

import org.junit.AfterClass;

import java.io.File;


/** Pure logic tests for AchievementService – no Scene2D, no real assets. */
public class AchievementServiceBasicTest {
    // === Test-only Gdx.files shim so StatsTracker/FileLoader can write safely ===


    // --- Minimal Gdx.files stub for unit tests (no real LibGDX backend) ---
    private static Files prevFiles;

    /** Install a fake Files so code that calls Gdx.files.external()/local()/absolute() won’t NPE. */
    @BeforeClass
    public static void installGdxFilesStub() {
        prevFiles = Gdx.files;
        Gdx.files = new Files() {
            private final File root = new File("build/test-tmp");

            private File ensure(String path) {
                File out = new File(root, path);
                File parent = out.getParentFile();
                if (parent != null) parent.mkdirs();
                return out;
            }

            // ==== required by Files interface (gdx 1.13.x) ====
            @Override
            public FileHandle getFileHandle(String path, FileType type) {
                switch (type) {
                    case External: return external(path);
                    case Local:    return local(path);
                    case Absolute: return absolute(path);
                    case Internal: return internal(path);
                    case Classpath:return classpath(path);
                    default:       throw new UnsupportedOperationException("Unknown type " + type);
                }
            }

            @Override public FileHandle external(String path) { return new FileHandle(ensure(path)); }
            @Override public FileHandle local(String path)    { return new FileHandle(ensure(path)); }
            @Override public FileHandle absolute(String path) { return new FileHandle(new File(path)); }

            // We don’t use these in this test; keep them explicit so failures are obvious if called.
            @Override public FileHandle classpath(String path) { throw new UnsupportedOperationException("classpath() not supported in tests"); }
            @Override public FileHandle internal(String path)  { throw new UnsupportedOperationException("internal() not supported in tests"); }

            @Override public String  getExternalStoragePath()     { return root.getAbsolutePath(); }
            @Override public boolean isExternalStorageAvailable() { return true; }
            @Override public String  getLocalStoragePath()        { return root.getAbsolutePath(); }
            @Override public boolean isLocalStorageAvailable()    { return true; }
        };
    }

    /** Restore the original Files after the suite. */
    @AfterClass
    public static void restoreGdxFiles() {
        Gdx.files = prevFiles;
    }


    /** Minimal FileHandle that wraps a java.io.File (constructor in FileHandle is protected). */
    private static class TestFileHandle extends FileHandle {
        public TestFileHandle(File file) { super(file); }
    }


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
