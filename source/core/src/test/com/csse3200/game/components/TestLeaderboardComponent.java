package com.csse3200.game.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestLeaderboardComponent {
  private LeaderboardComponent leaderboardComponent;

  @SuppressWarnings("unchecked")
  private Map<String, Long> getBaseTimes() throws IllegalAccessException, NoSuchFieldException {
    Field field = leaderboardComponent.getClass().getDeclaredField("leaderboard");
    field.setAccessible(true);
    return (Map<String, Long>) field.get(leaderboardComponent);
  }

  @BeforeEach
  void beforeEach() throws IllegalAccessException, NoSuchFieldException {
    String filePath = "test/files/leaderboard.json";
    leaderboardComponent = new LeaderboardComponent(filePath);
    Map<String, Long> baseTimes = getBaseTimes();

    baseTimes.put("1", 100000L);
    baseTimes.put("2", 100000L);
    baseTimes.put("3", 100000L);
    baseTimes.put("4", 100000L);
    baseTimes.put("5", 100000L);

    leaderboardComponent.writeData();
  }

  @Test
  void testTimeSaves() {
    leaderboardComponent.updateLeaderboard("1", 50000);
    assertEquals(50000, leaderboardComponent.readData().get("1"));
  }

  @Test
  void testTimeTooBig() {
    leaderboardComponent.updateLeaderboard("1", 150000);
    assertEquals(100000L, leaderboardComponent.readData().get("1"));
  }

  @Test
  void testTimeSavesByForce() {
    leaderboardComponent.updateLeaderboard("1", 150000, true);
    assertEquals(150000L, leaderboardComponent.readData().get("1"));
  }

  @Test
  void testWriteData() throws Exception {
    Map<String, Long> baseTimes = getBaseTimes();
    baseTimes.put("6", 150000L);
    leaderboardComponent.writeData();

    assertEquals(150000, leaderboardComponent.readData().get("6"));
  }

  @Test
  void testReadData() {
    HashMap<String, Long> baseTimes = new HashMap<>();

    baseTimes.put("1", 100000L);
    baseTimes.put("2", 100000L);
    baseTimes.put("3", 100000L);
    baseTimes.put("4", 100000L);
    baseTimes.put("5", 100000L);

    for (Map.Entry<String, Long> entry : baseTimes.entrySet()) {
      assertEquals(entry.getValue(), leaderboardComponent.readData().get(entry.getKey()));
    }
  }

  @Test
  void testHashMapSize() throws Exception {
    assertEquals(5, leaderboardComponent.readData().size());

    Map<String, Long> baseTimes = getBaseTimes();
    baseTimes.put("1", 100000L);
    leaderboardComponent.writeData();

    assertEquals(1, leaderboardComponent.readData().size());
  }
}
