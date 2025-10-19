package com.csse3200.game.utils;

public class CollectableCounter {
    private static int collectedLevel = 0;
    private static int collectedTotal = 0;
    private static final int LEVEL_TOTAL = 3;
    private static final int TOTAL = 9;

    private CollectableCounter() {}

    public static void reset() {
        collectedLevel = 0;
    }

    public static void increment() {
        collectedLevel++;
        collectedTotal++;
    }

    public static int getLevelCollected() {
        return collectedLevel;
    }

    public static int getTotalCollected() {
        return collectedTotal;
    }

    public static int getLevelTotal() {
        return LEVEL_TOTAL;
    }

    public static int getTotal() {
        return TOTAL;
    }
}
