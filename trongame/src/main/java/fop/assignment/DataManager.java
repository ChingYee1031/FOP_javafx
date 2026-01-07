package fop.assignment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DataManager {

    private static final String FILE_PATH = "users.csv";

    // --- 1. SAVE PROGRESS (Updates existing user or creates new one) ---
    public static void savePlayer(Player player, String password) {
        List<String> lines = new ArrayList<>();
        boolean userFound = false;

        // Read all existing lines
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                
                // If username matches, update their data
                if (parts.length > 0 && parts[0].equals(player.getName())) {
                    // FORMAT: Name, Pass, Level, XP, Score, Date, SeenTutorial
                    String updatedLine = String.format("%s,%s,%d,%d,%d,%s,%b",
                            player.getName(),
                            password, 
                            player.getLevel(),
                            player.getXP(),
                            calculateScore(player), 
                            LocalDate.now().toString(),
                            player.hasSeenTutorial() // <--- NEW DATA POINT
                    );
                    lines.add(updatedLine);
                    userFound = true;
                } else {
                    lines.add(line); // Keep other users unchanged
                }
            }
        } catch (IOException e) {
            // File might not exist yet, that's fine
        }

        // If it's a new user, add them to the list
        if (!userFound) {
            String newLine = String.format("%s,%s,%d,%d,%d,%s,%b",
                    player.getName(), password, player.getLevel(), player.getXP(), 
                    calculateScore(player), LocalDate.now().toString(),
                    player.hasSeenTutorial());
            lines.add(newLine);
        }

        // Write everything back to the file
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (String l : lines) pw.println(l);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- 2. LOGIN (LOAD PLAYER) ---
    public static Player login(String username, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; 

                String[] parts = line.split(",");
                if (parts.length < 4) continue; 
                
                // Check if Username AND Password match
                if (parts[0].equals(username) && parts[1].equals(password)) {
                    try {
                        int level = Integer.parseInt(parts[2]);
                        int xp = Integer.parseInt(parts[3]);
                        
                        // Check for Tutorial Flag (Index 6)
                        boolean seenTut = false;
                        if (parts.length >= 7) {
                            seenTut = Boolean.parseBoolean(parts[6]);
                        }
                        
                        Player p = new Player(username, "#00FFFF", 3.0, 1.5);
                        p.setLevel(level); 
                        p.setXP(xp);
                        p.setSeenTutorial(seenTut); // Load the flag
                        
                        return p;
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing user data for: " + username);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Login failed
    }

    // --- 3. LEADERBOARD (Top 10) ---
    public static List<PlayerScore> getTopPlayers() {
        List<PlayerScore> scores = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    try {
                        scores.add(new PlayerScore(parts[0], Integer.parseInt(parts[2]), Integer.parseInt(parts[4]), parts[5]));
                    } catch (NumberFormatException e) {
                        continue; 
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }

        // Sort by Level then Score
        scores.sort((p1, p2) -> {
            int levelCompare = Integer.compare(p2.getLevel(), p1.getLevel());
            if (levelCompare != 0) return levelCompare;
            return Integer.compare(p2.getScore(), p1.getScore());
        });
        
        // Cut to Top 10
        if (scores.size() > 10) {
            return scores.subList(0, 10);
        } else {
            return scores;
        }
    }

    private static int calculateScore(Player p) {
        return (p.getLevel() * 1000) + p.getXP();
    }
}