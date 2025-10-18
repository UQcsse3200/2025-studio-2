package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

public class LeaderboardComponent {
  private final String filePath;
  private final Map<String, Long> leaderboard;

  public LeaderboardComponent() {
    this("configs/leaderboard.json");
  }

  public LeaderboardComponent(String filePath) {
    this.filePath = filePath;
    leaderboard = readData();
  }

  private FileHandle getFile() {
    return Gdx.files.external("CSSE3200Game/" + filePath);
  }

  private Map<String, Long> readData() {
    try {
      return  new Gson().fromJson(getFile().readString(), new TypeToken<HashMap<String, Long>>() {}.getType());
    } catch (Exception e) {
      return new HashMap<>();
    }
  }

  public Map<String, Long> getData() {
    return leaderboard;
  }

  public void writeData() {
    getFile().writeString(
        "{\n" + leaderboard
            .entrySet()
            .stream()
            .map(entry -> "\t\"" + entry.getKey() + "\": " + entry.getValue())
            .reduce((str, current) -> str + ",\n" + current).orElse("{}") + "\n}",
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
