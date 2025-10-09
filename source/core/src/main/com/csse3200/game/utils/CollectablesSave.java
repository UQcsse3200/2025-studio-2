package com.csse3200.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.Vector2;

public class CollectablesSave {
    private static final String PREFS_NAME = "playerData";
    private static final String COLLECTABLE_COUNT = "collectableCount";
    private static final String COLLECTABLE_POS_PREFIX = "collectablePos";
    //private Vector2[] collected = new Vector2[9];

    private static Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);

    public static int getCollectedCount() {
        return prefs.getInteger(COLLECTABLE_COUNT, 0);
    }
    /*
    public static int getCollectedCount(int level) {
        return prefs.getInteger("collectableCount_level" + level, 0);
    }
    */

    public static void incrementCollectedCount() {
        prefs.putInteger(COLLECTABLE_COUNT, getCollectedCount() + 1);
        prefs.flush(); // Saves value to disk
    }
    /*
    public static void incrementCollectedCount(int level) {
        prefs.putInteger("collectableCount_level" + level, getCollectedCount(level));
        prefs.flush();
    }
    */

    // For testing and debugging
    public static void resetCollectedCount() {
        prefs.putInteger(COLLECTABLE_COUNT, 0);
        for (int i = 0; i < 9; i++) {
            prefs.remove(COLLECTABLE_POS_PREFIX + i);
        }
        prefs.flush(); // Saves value to disk
    }
    /*
    public static void resetCollectedCount(int level) {
        prefs.putInteger("collectableCount_level" + level, 0);
        prefs.flush();
    }
    */

    public static void saveCollectedPositions(Vector2[] positions) {
        for (int i = 0; i < positions.length; i++) {
            Vector2 pos = positions[i];
            if (pos.x != 0 && pos.y != 0) {
                prefs.putString(COLLECTABLE_POS_PREFIX + i, pos.x + "," + pos.y);
            }
        }
        prefs.flush();
    }

    public static Vector2[] loadCollectedPositions() {
        Vector2[] positions = new Vector2[9];
        for (int i = 0; i < 9; i++) {
            String stored = prefs.getString(COLLECTABLE_POS_PREFIX + i, "0,0");
            String[] parts = stored.split(",");
            float x = Float.parseFloat(parts[0]);
            float y = Float.parseFloat(parts[1]);
            positions[i] = new Vector2(x, y);
        }
        return positions;
    }

    public static void saveCollectedPositions(int index, Vector2 pos) {
        if (index >= 0 && index < 9) {
            prefs.putString(COLLECTABLE_POS_PREFIX + index, pos.x + "," + pos.y);
            prefs.flush();
        }
    }

    public static Vector2 getCollectedPosition(int index) {
        if (index < 0 || index >= 9) {
            return new Vector2(0, 0);
        }
        String stored = prefs.getString(COLLECTABLE_POS_PREFIX + index, "0,0");
        String[] parts = stored.split(",");
        return new Vector2(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]));
    }
}
