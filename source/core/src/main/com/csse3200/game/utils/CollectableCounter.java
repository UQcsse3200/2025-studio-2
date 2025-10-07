package com.csse3200.game.utils;

public class CollectableCounter {
    private static int collected_level = 0;
    private static int collected_total = 0;
    private static int level_total = 3;
    private static int total = 9;

    private CollectableCounter() {}

    public static void reset() {
        collected_level = 0;
    }

    public static void increment() {
        collected_level++;
        collected_total++;
    }

    public static int getLevelCollected() {
        return collected_level;
    }

    public static int getTotalCollected() {
        return collected_total;
    }

    public static int getLevelTotal() {
        return level_total;
    }

    public static int getTotal() {
        return total;
    }
}
