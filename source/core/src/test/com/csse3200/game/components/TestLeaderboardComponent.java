package com.csse3200.game.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

public class TestLeaderboardComponent {
    private LeaderboardComponent leaderboardComponent;
    private String filePath = "test/files/leaderboard.json";

    @BeforeEach
    void beforeEach() {
        leaderboardComponent = new LeaderboardComponent();
        HashMap<String, Long> baseTimes = new HashMap<>();

        baseTimes.put("1", (long) 100000);
        baseTimes.put("2", (long) 100000);
        baseTimes.put("3", (long) 100000);
        baseTimes.put("4", (long) 100000);
        baseTimes.put("5", (long) 100000);

        leaderboardComponent.writeData(baseTimes, filePath);
    }

    @Test
    public void testTimeSaves() {
        leaderboardComponent.updateLeaderboard("1", 50000, filePath);
        assertEquals(50000, leaderboardComponent.readData(filePath).get("1"));
    }

    @Test
    public void testTimeTooBig() {
        leaderboardComponent.updateLeaderboard("1", 150000, filePath);
        assertEquals(100000L, leaderboardComponent.readData(filePath).get("1"));
    }

    @Test
    public void testTimeSavesByForce() {
        leaderboardComponent.updateLeaderboard("1", 150000, true, filePath);
        assertEquals(150000L, leaderboardComponent.readData(filePath).get("1"));
    }

    @Test
    public void testWriteData() {
        HashMap<String, Long> baseTimes = new HashMap<>();
        baseTimes.put("6", (long) 150000);
        leaderboardComponent.writeData(baseTimes, filePath);

        assertEquals(150000, leaderboardComponent.readData(filePath).get("6"));
    }

    @Test
    public void testReadData() {
        HashMap<String, Long> baseTimes = new HashMap<>();

        baseTimes.put("1", (long) 100000);
        baseTimes.put("2", (long) 100000);
        baseTimes.put("3", (long) 100000);
        baseTimes.put("4", (long) 100000);
        baseTimes.put("5", (long) 100000);

        for (Map.Entry<String, Long> entry : baseTimes.entrySet()) {
            assertEquals(entry.getValue(), leaderboardComponent.readData(filePath).get(entry.getKey()));
        }
    }

    @Test
    public void testHashMapSize() {
        assertEquals(5, leaderboardComponent.readData(filePath).size());

        HashMap<String, Long> baseTimes = new HashMap<>();
        baseTimes.put("1", (long) 100000);
        leaderboardComponent.writeData(baseTimes, filePath);

        assertEquals(1, leaderboardComponent.readData(filePath).size());
    }
}
