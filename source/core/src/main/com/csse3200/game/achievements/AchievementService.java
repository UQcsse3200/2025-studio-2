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
 * <p>
 * Persists unlocks and sprint time using LibGDX Preferences.
 */
public class AchievementService {
    private static final boolean DISABLE_PERSISTENCE = true;
    private static final String PREF_NAME = "achievements";

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

        // DEBUG: keep this while testing
        Gdx.app.log("Achv", String.format("Sprint total = %.3f", sprintSeconds));

        // Use a small threshold for dev; bump to 30f for production.
        final float THRESHOLD = 3f; // <-- set to 30f when you’re done testing

        if (!isUnlocked(AchievementId.ADRENALINE_RUSH) && sprintSeconds >= THRESHOLD) {
            unlock(
                    AchievementId.ADRENALINE_RUSH,
                    "Adrenaline Rush",
                    "Sprint for 3 seconds total."
            );
            Gdx.app.log("Achv", String.format("Adrenaline Rush threshold hit at %.3f", sprintSeconds));
            autosave(); // persist immediately
        }
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
    public void devReset() {
        unlocked.clear();
        sprintSeconds = 0f;
        save();
        Gdx.app.log("Achv", "DEV RESET: cleared achievements + sprint time");
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
        if (DISABLE_PERSISTENCE) {
            unlocked.clear();
            sprintSeconds = 0f;
            staminaEverExhausted = false;
            return;
        }
        // (If you keep persistence later, your old prefs-reading code can live here)
    }

    private void save() {
        if (DISABLE_PERSISTENCE) return;
        // (If you keep persistence later, your old prefs-writing code can live here)
    }

    private void autosave() {
        if (!DISABLE_PERSISTENCE) save();
    }

}
