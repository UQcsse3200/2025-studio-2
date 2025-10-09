package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.utils.GsonJsonUtils;

import java.io.*;
import java.util.HashMap;
import java.util.stream.Collectors;


public class LeaderboardComponent {
    private final String filePath = "configs/leaderboard.json";
    private final GsonJsonUtils converter = new GsonJsonUtils();

    public HashMap<String, Long> readData() {
        return readData(this.filePath);
    }

    public HashMap<String, Long> readData(String filePath) {
        String file;
        try {
            file = Gdx.files.external(filePath).readString();
        } catch (Exception e) {
            file = Gdx.files.internal(filePath).readString();
        }
        return this.converter.jsonToHashMap(file);
    }

    public void writeData(HashMap<String, Long> map) {
        writeData(map, this.filePath);
    }

    public void writeData(HashMap<String, Long> map, String filePath) {
      Gdx.files.external(filePath).writeString(
          "{\n" + String.join(map
              .entrySet()
              .stream()
              .map(entry -> String.format("\t\"%s\": %s}", entry.getKey(), entry.getValue()))
              .collect(Collectors.joining()), ",\n") + "\n}",
          false
      );
    }


    public void updateLeaderboard(String name, long score, boolean force) {
        HashMap<String, Long> leaderboard = readData();
        Long time = leaderboard.get(name);

        if (force) {
            leaderboard.put(name, score);
        } else if (time == null || time > score) {
            leaderboard.put(name, score);
        }

        writeData(leaderboard);
    }

    public void updateLeaderboard(String name, long score) {
        HashMap<String, Long> leaderboard = readData();
        Long time = leaderboard.get(name);

        System.out.println(time);
        System.out.println(score);

        if (time == null || time > score) {
            leaderboard.put(name, score);
        }

        writeData(leaderboard);
    }

    public void updateLeaderboard(String name, long score, boolean force, String filePath) {
        HashMap<String, Long> leaderboard = readData(filePath);
        Long time = leaderboard.get(name);

        if (force) {
            leaderboard.put(name, score);
        } else if (time == null || time > score) {
            leaderboard.put(name, score);
        }

        writeData(leaderboard, filePath);
    }

    public void updateLeaderboard(String name, long score, String filePath) {
        HashMap<String, Long> leaderboard = readData(filePath);
        Long time = leaderboard.get(name);

        System.out.println(time);
        System.out.println(score);

        if (time == null || time > score) {
            leaderboard.put(name, score);
        }

        writeData(leaderboard, filePath);
    }
}