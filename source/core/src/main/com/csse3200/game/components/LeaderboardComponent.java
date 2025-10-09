package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.utils.GsonJsonUtils;

import java.util.HashMap;
import java.util.stream.Collectors;


public class LeaderboardComponent {
  private final String filePath = "configs/leaderboard.json";
  private final GsonJsonUtils converter = new GsonJsonUtils();
  private final HashMap<String, Long> leaderboard = new HashMap<>();

  public HashMap<String, Long> readData() {
    String file;
    try {
      file = Gdx.files.external("CSSE3200Game" + filePath).readString();
    } catch (Exception e) {
      file = Gdx.files.internal(filePath).readString();
    }
    return this.converter.jsonToHashMap(file);
  }

  public void writeData() {
    Gdx.files.external("CSSE3200Game" + filePath).writeString(
        "{\n" + leaderboard
            .entrySet()
            .stream()
            .map(entry -> String.format("\t\"%s\": %s}", entry.getKey(), entry.getValue()))
            .reduce((str, current) -> str + ",\n" + current) + "\n}",
        false
    );
  }

  public void updateLeaderboard(String name, long score, boolean force) {
    leaderboard.compute(name, (k, v) -> ((force || v == null) ? score : Math.max(v, score)));
    writeData();
  }

  public void updateLeaderboard(String name, long score) {
    updateLeaderboard(name, score, false);
  }
}