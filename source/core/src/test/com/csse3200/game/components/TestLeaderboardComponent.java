package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TestLeaderboardComponent {
  private LeaderboardComponent leaderboardComponent;
  private String data;

  @SuppressWarnings("unchecked")
  private Map<String, Long> getBaseTimes() throws IllegalAccessException, NoSuchFieldException {
    Field field = leaderboardComponent.getClass().getDeclaredField("leaderboard");
    field.setAccessible(true);
    return (Map<String, Long>) field.get(leaderboardComponent);
  }

  @BeforeEach
  void beforeEach() throws IllegalAccessException, NoSuchFieldException {
    data = "{}";
    FileHandle fileHandle = mock(FileHandle.class);
    when(fileHandle.readString()).thenAnswer(invocation -> data);
    doAnswer(invocation -> {
      data = invocation.getArgument(0);
      return null;
    }).when(fileHandle).writeString(anyString(), anyBoolean());

    Gdx.files = mock(com.badlogic.gdx.Files.class);
    when(Gdx.files.external(anyString())).thenReturn(fileHandle);

    leaderboardComponent = new LeaderboardComponent();
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
    baseTimes.clear();
    baseTimes.put("1", 100000L);
    leaderboardComponent.writeData();

    assertEquals(1, leaderboardComponent.readData().size());
  }
}
