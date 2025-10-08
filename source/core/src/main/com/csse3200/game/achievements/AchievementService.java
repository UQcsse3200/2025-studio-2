package com.csse3200.game.achievements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Minimal achievements service for:
 *  - ADRENALINE_RUSH: sprint for 30 seconds (per-level accumulation)
 *  - STAMINA_MASTER: finish a level without ever hitting 0 stamina
 *  - LEVEL_1_COMPLETE
 *  - LEVEL_2_COMPLETE
 *
 * Persists unlocks and sprint time using LibGDX Preferences.
 */
public class AchievementService {
    private static final String PREF_NAME = "achievements";
    private static final String KEY_UNLOCKED = "unlocked";       // csv list
    private static final String KEY_SPRINT_SEC = "sprint.totalSeconds";

    private static AchievementService INSTANCE;

    public static AchievementService get() {
        if (INSTANCE == null) INSTANCE = new AchievementService();
        return INSTANCE;
    }

    /** Consumers (e.g., toast UI) can subscribe to be notified on unlocks. */
    public interface Listener {
        void onUnlocked(AchievementId id, String title, String description);
    }

    private final Array<Listener> listeners = new Array<>();
    private final ObjectMap<AchievementId, Boolean> unlocked = new ObjectMap<>();

    // Session/per-level state:
    private float sprintSeconds = 0f;
    private boolean staminaEverExhausted = false;

    // Prefs
    private final Preferences prefs;

    private AchievementService() {
        prefs = Gdx.app.getPreferences(PREF_NAME);
        load();
    }

    // ---------------- Public API ----------------

    public void addListener(Listener l) {
        if (l != null && !listeners.contains(l, true)) listeners.add(l);
    }
    public void removeListener(Listener l) { listeners.removeValue(l, true); }

    public boolean isUnlocked(AchievementId id) { return unlocked.get(id, false); }
    public float getSprintSeconds() { return sprintSeconds; }
    public boolean getStaminaEverExhausted() { return staminaEverExhausted; }

    /** Call when the level begins (resets per-level flags, keeps lifetime sprintSeconds). */
    public void onLevelStarted() {
        staminaEverExhausted = false;
    }

    /** Call every frame while sprinting to accumulate time for Adrenaline Rush. */
    public void addSprintTime(float dt) {
        if (dt <= 0f) return;
        sprintSeconds += dt;

        // ADRENALINE_RUSH: 30s total
        if (!isUnlocked(AchievementId.ADRENALINE_RUSH) && sprintSeconds >= 30f) {
            unlock(AchievementId.ADRENALINE_RUSH,
                    "Adrenaline Rush",
                    "Sprint for 30 seconds total.");
        }
        autosave();
    }

    /** Mark that stamina hit zero sometime this level. */
    public void markStaminaExhausted() {
        staminaEverExhausted = true;
        autosave();
    }

    /**
     * Call when the level is completed.
     * Also evaluates STAMINA_MASTER for this level.
     */
    public void onLevelCompleted(int levelNumber) {
        if (levelNumber == 1) {
            maybeUnlock(AchievementId.LEVEL_1_COMPLETE, "Level 1 Complete", "Finish Level 1.");
        } else if (levelNumber == 2) {
            maybeUnlock(AchievementId.LEVEL_2_COMPLETE, "Level 2 Complete", "Finish Level 2.");
        }

        // STAMINA_MASTER: finish a level without stamina=0 at any point this level
        if (!staminaEverExhausted) {
            maybeUnlock(AchievementId.STAMINA_MASTER,
                    "Stamina Master",
                    "Finish a level without exhausting stamina.");
        }

        autosave();
    }

    /** Call periodically if the service is active and you want background autosave. */
    private float autosaveTimer = 0f;
    public void update(float dt) {
        autosaveTimer += dt;
        if (autosaveTimer >= 10f) { // autosave every 10s while active
            autosaveTimer = 0f;
            save();
        }
    }

    // ---------------- Internals ----------------

    private void maybeUnlock(AchievementId id, String title, String desc) {
        if (!isUnlocked(id)) unlock(id, title, desc);
    }

    private void unlock(AchievementId id, String title, String description) {
        unlocked.put(id, true);
        save(); // persist immediately
        for (Listener l : listeners) {
            l.onUnlocked(id, title, description);
        }
    }

    private void load() {
        unlocked.clear();

        // Load unlocked CSV -> enum values
        String csv = prefs.getString(KEY_UNLOCKED, "");
        if (!csv.isEmpty()) {
            for (String s : csv.split(",")) {
                try {
                    AchievementId id = AchievementId.valueOf(s.trim());
                    unlocked.put(id, true);
                } catch (IllegalArgumentException ignored) {
                    // Unknown/old enum name — skip
                }
            }
        }

        sprintSeconds = prefs.getFloat(KEY_SPRINT_SEC, 0f);
    }

    private void save() {
        // unlocked -> CSV
        StringBuilder sb = new StringBuilder();
        for (AchievementId id : AchievementId.values()) {
            if (isUnlocked(id)) {
                if (sb.length() > 0) sb.append(',');
                sb.append(id.name());
            }
        }
        prefs.putString(KEY_UNLOCKED, sb.toString());
        prefs.putFloat(KEY_SPRINT_SEC, sprintSeconds);
        prefs.flush();
    }

    private void autosave() {
        save();
    }
}
