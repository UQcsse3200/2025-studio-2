package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

public class LeaderboardComponent {
  private Map<String, Long> leaderboard = new HashMap<>();
  private static final LeaderboardComponent instance = new LeaderboardComponent();

  private LeaderboardComponent() {
    try {
      leaderboard = new Gson().fromJson(getFile().readString(), new TypeToken<HashMap<String, Long>>() {}.getType());
    } catch (Exception e) { /* Ignored */ }
  }

  public static LeaderboardComponent getInstance() {
    return instance;
  }

  private FileHandle getFile() {
    return Gdx.files.external("CSSE3200Game/leaderboard.json");
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
            .reduce((str, current) -> str + ",\n" + current).orElse("") + "\n}",
        false
    );
  }

  public void updateLeaderboard(String name, long time) {
    leaderboard.compute(name, (k, v) -> ((null == v) ? time : Math.min(v, time)));
    writeData();
  }
}
