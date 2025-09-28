package com.csse3200.game.components.statisticspage;

public class StatsTracker {
    private static long playtime; // in ms
    private static int upgradesCollected;
    private static int levelsCompleted;
    private static int deathCount;
    private static int achievementsUnlocked;

    private static long sessionStartTime;

    public StatsTracker() {
        resetSession();
    }

    public void startSession() {
        sessionStartTime = System.currentTimeMillis();
    }

    public void endSession() {
        playtime += System.currentTimeMillis() - sessionStartTime;
    }

    public void resetSession() {
        playtime = 0;
        upgradesCollected = 0;
        levelsCompleted = 0;
        deathCount = 0;
        achievementsUnlocked = 0;
    }

    public static void addUpgrade(){
        upgradesCollected++;
    }

    public static void completeLevel() {
        levelsCompleted++;
    }

    public static void addDeath() {
        deathCount++;
    }

    public static void unlockAchievement() {
        achievementsUnlocked++;
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
}
