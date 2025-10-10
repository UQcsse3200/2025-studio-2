package com.csse3200.game.achievements;

/**
 * Thin helper that routes progress events into AchievementService.
 * Keep this static so areas/components can call it without holding state.
 */
public final class AchievementProgression {
    private AchievementProgression() {}

    /** Call when a level starts (resets per-level flags). */
    public static void onLevelStart() {
        AchievementService.get().onLevelStarted();
    }

    /** Call when a level completes successfully. */
    public static void onLevelComplete(String levelTag) {
        int levelNum = parseLevelNumber(levelTag);
        AchievementService.get().onLevelCompleted(levelNum);
    }

    /** Call once per frame while sprinting. */
    public static void onSprintTick(float dt) {
        if (dt > 0f) {
            AchievementService.get().addSprintTime(dt);
        }
    }

    /** Call when stamina hits zero at any time during a level. */
    public static void onStaminaExhausted() {
        AchievementService.get().markStaminaExhausted();
    }

    // --- helpers ---

    private static int parseLevelNumber(String tag) {
        if (tag == null) return -1;
        String t = tag.trim().toLowerCase();
        if (t.contains("level2") || t.equals("level 2") || t.equals("level_two")) return 2;
        if (t.contains("level1") || t.equals("level 1") || t.equals("level_one")) return 1;
        return -1; // unknown; AchievementService will ignore the number
    }
}
