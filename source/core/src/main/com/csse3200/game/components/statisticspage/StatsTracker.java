package com.csse3200.game.components.statisticspage;

import com.csse3200.game.files.FileLoader;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stats logic handling class
 */
public class StatsTracker {
    private static final Logger logger = Logger.getLogger(StatsTracker.class.getName());
    private static long playtime; // in ms
    private static int upgradesCollected;
    private static int levelsCompleted;
    private static int deathCount;
    private static int achievementsUnlocked;
    private static int jumpCount;
    private static int codexReads;

    private static long sessionStartTime;

    private static final String FILE_PATH = "configs/stats.json";

    private StatsTracker() {
        throw new IllegalStateException("Instantiating static util class");
    }

    /**
     * Starts timer for session playtime
     */
    public static void startSession() {
        loadStats();
        sessionStartTime = System.currentTimeMillis();
        logger.info("Session start");
    }

    /**
     * End timer for session playtime
     */
    public static void endSession() {
        playtime += System.currentTimeMillis() - sessionStartTime;
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Session duration: " + (System.currentTimeMillis() - sessionStartTime));
            logger.info("Total playtime before save: " + playtime);
        }
        saveStats();
    }

    /**
     * Reset session and all fields to 0
     */
    public static void resetSession() {
        playtime = 0;
        upgradesCollected = 0;
        levelsCompleted = 0;
        deathCount = 0;
        achievementsUnlocked = 0;
        jumpCount = 0;
        codexReads = 0;
    }

    /**
     * Increment upgrade counter
     */
    public static void addUpgrade(){
        upgradesCollected++;
        saveStats();
    }

    /**
     * Increment complete level counter
     */
    public static void completeLevel() {
        levelsCompleted++;
        saveStats();
    }

    /**
     * Increment death counter
     */
    public static void addDeath() {
        deathCount++;
        saveStats();
    }

    /**
     * Increment achievement counter
     */
    public static void unlockAchievement() {
        achievementsUnlocked++;
        saveStats();
    }

    /**
     * Increment jump counter
     */
    public static void addJump() {
        jumpCount++;
    }

    /**
     * Increment codex reads
     */
    public static void addCodex() {
        codexReads++;
        saveStats();
    }

    /**
     * Retrieve playtime in minutes
     */
    public static long getPlaytimeMinutes() {
        long total = playtime;
        return total / 60000; // ms to minutes
    }

    /**
     * Retrieve upgrade counter
     */
    public static int getUpgradesCollected() {
        return upgradesCollected;
    }

    /**
     * Retrieve levels completed
     */
    public static int getLevelsCompleted() {
        return levelsCompleted;
    }

    /**
     * Retrieve death counter
     */
    public static int getDeathCount() {
        return deathCount;
    }

    /**
     * Retrieve achievement counter
     */
    public static int getAchievementsUnlocked() {
        return achievementsUnlocked;
    }

    /**
     * Retrieve jump counter
     */
    public static int getJumpCount() {
        return jumpCount;
    }

    /**
     * Retrieve codex reads
     */
    public static int getCodexReads() {
        return codexReads;
    }

    /**
     * Save stats to JSON
     */
    public static void saveStats() {
        StatsData data = new StatsData();
        data.playtime = playtime;
        data.upgradesCollected = upgradesCollected;
        data.levelsCompleted = levelsCompleted;
        data.deathCount = deathCount;
        data.achievementsUnlocked = achievementsUnlocked;
        data.jumpCount = jumpCount;
        data.codexReads = codexReads;

        FileLoader.writeClass(data, FILE_PATH, FileLoader.Location.EXTERNAL);
    }

    /**
     * Retrieve stats from JSON and load into game
     */
    public static void loadStats() {
        StatsData data = FileLoader.readClass(StatsData.class, FILE_PATH,
                FileLoader.Location.EXTERNAL);
        if (data != null) {
            playtime = data.playtime;
            upgradesCollected = data.upgradesCollected;
            levelsCompleted = data.levelsCompleted;
            deathCount = data.deathCount;
            achievementsUnlocked = data.achievementsUnlocked;
            jumpCount = data.jumpCount;
            codexReads = data.codexReads;
        } else {
            resetSession();
        }
    }

    /**
     * Resets stats file back to default all 0's
     */
    public static void resetStatsFile() {
        resetSession();
        saveStats();
    }
}
