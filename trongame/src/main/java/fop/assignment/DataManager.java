package fop.assignment;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

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
                String[] parts = line.split(",");
                // If username matches, update their data
                if (parts[0].equals(player.getName())) {
                    String updatedLine = String.format("%s,%s,%d,%d,%d,%s",
                            player.getName(),
                            password, 
                            player.getLevel(),
                            player.getXP(),
                            calculateScore(player), // Update Score
                            LocalDate.now().toString()
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
            String newLine = String.format("%s,%s,%d,%d,%d,%s",
                    player.getName(), password, player.getLevel(), player.getXP(), calculateScore(player), LocalDate.now().toString());
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
                String[] parts = line.split(",");
                
                // Check if Username AND Password match
                if (parts.length >= 4 && parts[0].equals(username) && parts[1].equals(password)) {
                    int level = Integer.parseInt(parts[2]);
                    int xp = Integer.parseInt(parts[3]);
                    
                    // Load the saved stats into a new Player object
                    Player p = new Player(username, "#00FFFF", 3.0, 1.5);
                    p.setLevel(level); 
                    p.setXP(xp);       
                    
                    return p;
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
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    scores.add(new PlayerScore(parts[0], Integer.parseInt(parts[2]), Integer.parseInt(parts[4]), parts[5]));
                }
            }
        } catch (IOException e) { e.printStackTrace(); }

        // Sort by Score (Highest first)
        scores.sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));
        
        // Return only the Top 10
        return scores.subList(0, Math.min(scores.size(), 10));
    }

    private static int calculateScore(Player p) {
        // Score Formula: (Level * 1000) + XP
        return (p.getLevel() * 1000) + p.getXP();
    }
}