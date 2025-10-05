package com.csse3200.game.components.statisticspage;

import com.csse3200.game.files.FileLoader;

public class StatsTracker {
    private static long playtime; // in ms
    private static int upgradesCollected;
    private static int levelsCompleted;
    private static int deathCount;
    private static int achievementsUnlocked;

    private static long sessionStartTime;

    private static final String FILE_PATH = "configs/stats.json";

    public StatsTracker() {
        resetSession();
    }

    public static void startSession() {
        sessionStartTime = System.currentTimeMillis();
    }

    public static void endSession() {
        playtime += System.currentTimeMillis() - sessionStartTime;
        saveStats();
    }

    public static void resetSession() {
        playtime = 0;
        upgradesCollected = 0;
        levelsCompleted = 0;
        deathCount = 0;
        achievementsUnlocked = 0;
    }

    public static void addUpgrade(){
        upgradesCollected++;
        saveStats();
    }

    public static void completeLevel() {
        levelsCompleted++;
        saveStats();
    }

    public static void addDeath() {
        deathCount++;
        saveStats();
    }

    public static void unlockAchievement() {
        achievementsUnlocked++;
        saveStats();
    }

    public static long getPlaytimeMinutes() {
        long total = playtime;
        if (sessionStartTime > 0) {
            total += (System.currentTimeMillis() - sessionStartTime);
        }
        return total / 60000; // ms to minutes
    }

    public static int getUpgradesCollected() {
        return upgradesCollected;
    }

    public static int getLevelsCompleted() {
        return levelsCompleted;
    }

    public static int getDeathCount() {
        return deathCount;
    }

    public static int getAchievementsUnlocked() {
        return achievementsUnlocked;
    }

    public static void saveStats() {
        StatsData data = new StatsData();
        data.playtime = playtime;
        data.upgradesCollected = upgradesCollected;
        data.levelsCompleted = levelsCompleted;
        data.deathCount = deathCount;
        data.achievementsUnlocked = achievementsUnlocked;

        FileLoader.writeClass(data, FILE_PATH, FileLoader.Location.LOCAL);
    }

    public static void loadStats() {
        StatsData data = FileLoader.readClass(StatsData.class, FILE_PATH,
                FileLoader.Location.LOCAL);
        if (data != null) {
            playtime = data.playtime;
            upgradesCollected = data.upgradesCollected;
            levelsCompleted = data.levelsCompleted;
            deathCount = data.deathCount;
            achievementsUnlocked = data.achievementsUnlocked;
        } else {
            playtime = 0;
            upgradesCollected = 0;
            levelsCompleted = 0;
            deathCount = 0;
            achievementsUnlocked = 0;
        }
    }
}
