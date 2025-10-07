package com.csse3200.game.components;

import com.csse3200.game.utils.GsonJsonUtils;

import java.io.*;
import java.util.HashMap;


public class LeaderboardComponent {
    private final String filePath;
    private final GsonJsonUtils converter;

    public LeaderboardComponent() {
        this.filePath = "leaderboard.json";
        this.converter = new GsonJsonUtils();
    }

    public HashMap<String, Long> readData() {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(this.filePath))) {

            // Read hashmap
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + this.filePath);
            return null;
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return null;
        }

        return this.converter.jsonToHashMap(sb.toString());
    }

    public void writeData(HashMap<String, Long> map) {
        String json = converter.hashMapToJson(map);
        String[] lines = json.substring(1, json.length() - 1).split(",");

        try (FileWriter fw = new FileWriter(filePath)) {
            fw.write("{\n");
            for (int i = 0; i < lines.length; i++) {
                fw.write("  " + lines[i].trim());
                if (i != lines.length - 1) {
                    fw.write(",");
                }
                fw.write("\n");
            }
            fw.write("}\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateLeaderboard(String name, long score) {
        HashMap<String, Long> leaderboard = readData();
        Long time = leaderboard.get(name);

        if (time == null || time > score) {
            leaderboard.put(name, score);
        }
//            leaderboard.put(name, score);

        writeData(leaderboard);
    }
}