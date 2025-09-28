package com.csse3200.game.components.statisticspage;

public class StatsTracker {
    private long playtime; // in ms
    private int upgradesCollected;
    private int levelsCompleted;
    private int deathCount;
    private int achievementsUnlocked;

    private long sessionStartTime;

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

    public void addUpgrade(){
        upgradesCollected++;
    }

    public void completeLevel() {
        levelsCompleted++;
    }

    public void addDeath() {
        deathCount++;
    }

    public void unlockAchievement() {
        achievementsUnlocked++;
    }

    public long getPlaytimeMinutes() {
        long total = playtime;
        if (sessionStartTime > 0) {
            total += (System.currentTimeMillis() - sessionStartTime);
        }
        return total / 60000; // ms to minutes
    }

    public int getUpgradesCollected() {
        return upgradesCollected;
    }

    public int getLevelsCompleted() {
        return levelsCompleted;
    }

    public int getDeathCount() {
        return deathCount;
    }

    public int getAchievementsUnlocked() {
        return achievementsUnlocked;
    }
}
