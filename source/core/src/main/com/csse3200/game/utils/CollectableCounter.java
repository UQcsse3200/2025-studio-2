package com.csse3200.game.utils;

public class CollectableCounter {
    private static int collected = 0;
    private static int total = 3;

    private CollectableCounter() {}

    public static void setTotal(int t) {
        total = t;
    }

    public static void reset() {
        collected = 0;
    }

    public static void add() {
        collected++;
    }

    public static int getCollected() {
        return collected;
    }

    public static int getTotal() {
        return total;
    }
}
