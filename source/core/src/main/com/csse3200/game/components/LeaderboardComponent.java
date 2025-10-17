package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.csse3200.game.utils.GsonJsonUtils;

import java.util.HashMap;
import java.util.Map;

public class LeaderboardComponent {
  private final String filePath;
  private final GsonJsonUtils converter = new GsonJsonUtils();
  private final HashMap<String, Long> leaderboard = new HashMap<>();

  public LeaderboardComponent() {
    this("configs/leaderboard.json");
  }

  public LeaderboardComponent(String filePath) {
    this.filePath = filePath;
  }

  private FileHandle getFile() {
    return Gdx.files.external("CSSE3200Game/" + filePath);
  }

  public Map<String, Long> readData() {
    String file;
    try {
      file = getFile().readString();
    } catch (Exception e) {
      file = "{}";
    }
    return converter.jsonToHashMap(file);
  }

  public void writeData() {
    getFile().writeString(
        "{\n" + leaderboard
            .entrySet()
            .stream()
            .map(entry -> "\t\"" + entry.getKey() + "\": " + entry.getValue())
            .reduce((str, current) -> str + ",\n" + current).orElseGet(() -> "{}") + "\n}",
        false
    );
  }

  public void updateLeaderboard(String name, long time, boolean force) {
    leaderboard.compute(name, (k, v) -> ((force || null == v) ? time : Math.min(v, time)));
    writeData();
  }

  public void updateLeaderboard(String name, long score) {
    updateLeaderboard(name, score, false);
  }
}