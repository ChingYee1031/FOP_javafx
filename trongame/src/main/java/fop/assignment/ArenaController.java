package fop.assignment;

import fop.assignment.Player;
import fop.assignment.Enemy;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner; // Needed for file reading
import java.io.File;      // Needed for file reading
import java.io.FileNotFoundException;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;


public class ArenaController {
    @FXML private VBox characterMenu; 
    @FXML private VBox arenaMenu;     
    @FXML private Canvas gameCanvas;
// Change this line in your ArenaController
    private CharacterSelection selectedChar;
    private ArenaModel model = new ArenaModel();
// Replace 'int playerX, playerY' with actual Objects
private Player player;
private ArrayList<Enemy> enemies = new ArrayList<>();
    private final int CELL = 15;

    private enum Direction { UP, DOWN, LEFT, RIGHT, NONE }
    private Direction currentDir = Direction.NONE;
    
    private long lastUpdate = 0;
    private long speedNanos = 100_000_000; 
    private boolean gameStarted = false;

    // Add this at the top with your other variables
private String gameMessage = ""; 
private long messageTimer = 0; // To make the message disappear after a few seconds
public void initialize() {
    // --- 1. SETUP PLAYER ---
    String pName = "Tron";
    String pColor = "#00FFFF";
    double pLives = 3.0;
    double pSpeed = 1.5;

    if (App.globalSelectedCharacter != null) {
        pColor = App.globalSelectedCharacter.getColor();
        pLives = App.globalSelectedCharacter.getLives();
        pSpeed = App.globalSelectedCharacter.getSpeed();
    } 

    this.player = new Player(pName, pColor, pLives, pSpeed);
    this.player.setPosition(20, 20); 

    // --- 2. SETUP ENEMIES (Updated for File I/O) ---
    enemies.clear(); 
    loadEnemiesFromFile(); // <--- Call the new function here!

    // Safety check: Only set positions if enemies loaded successfully
    if (enemies.size() >= 4) {
        enemies.get(0).setPosition(5, 5);   
        enemies.get(1).setPosition(35, 5);  
        enemies.get(2).setPosition(5, 35);  
        enemies.get(3).setPosition(35, 35); 
    }

    // --- 3. UI HANDLING ---
    if (characterMenu != null) characterMenu.setVisible(false);
    if (arenaMenu != null) arenaMenu.setVisible(true);

    // --- 4. START GAME ---
    model.loadArena1(); 
    draw();
    startTaskTimer();
}

    // --- Arena Button Actions ---
    @FXML
    private void loadArena1Action() {
        model.loadArena1();
        startGameSession();
    }

    @FXML
    private void loadArena2Action() {
        model.loadArena2();
        startGameSession();
    }

    @FXML
    private void loadArena3Action() {
        model.loadArena3();
        startGameSession();
    }

    @FXML
    private void loadRandomArenaAction() {
        model.loadRandomArena();
        startGameSession();
    } 

    private void startGameSession() {
        resetPlayer();
        gameStarted = true;
        
        // Hide all menu overlays to play
        if (characterMenu != null) characterMenu.setVisible(false);
        if (arenaMenu != null) arenaMenu.setVisible(false);
        
        gameCanvas.requestFocus(); 
        draw();
    }

    private void startTaskTimer() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameStarted) {
                    long currentDelay = model.isSpeedBoostActive() ? speedNanos / 2 : speedNanos;
                    if (now - lastUpdate >= currentDelay) {
                        updateGame();
                        draw();
                        lastUpdate = now;
                    }
                }
            }
        }.start();
    }   
private void updateGame() {
    // 1. Check Player Status
    // If player is dead, stop the loop so the game doesn't crash or continue
    if (player == null || !player.isAlive()) return;

    // --- 2. UPDATE PLAYER MOVEMENT ---
    int nextX = player.getX();
    int nextY = player.getY();

    // Calculate next position based on the input direction
    // (Ensure you still have the 'currentDir' variable in your Controller!)
    switch (currentDir) {
        case UP:    nextY--; break;
        case DOWN:  nextY++; break;
        case LEFT:  nextX--; break;
        case RIGHT: nextX++; break;
        default:    break; // Do nothing if NONE
    }

    // Only move if a direction is pressed
    if (currentDir != Direction.NONE) {
        // Send the PLAYER object to the model to handle collisions/movement
        model.processMove(player, nextX, nextY);
    }

    // --- 3. UPDATE ENEMY AI (The New Part) ---
   // --- 3. UPDATE ENEMY AI ---
    for (Enemy e : enemies) {
        // Skip already dead enemies
        if (!e.isAlive()) continue;

        // A. Ask AI for Move
        int moveDir = e.makeMove(model.getGrid());
        int ex = e.getX();
        int ey = e.getY();

        if (moveDir == 0) ey--;      
        else if (moveDir == 1) ey++; 
        else if (moveDir == 2) ex--; 
        else if (moveDir == 3) ex++; 

        // B. Process Move
        model.processMove(e, ex, ey);
        
        // --- NEW: CHECK FOR DEATH & REWARD XP ---
       // Inside updateGame(), inside the Enemy loop:
     if (!e.isAlive()) {
    int reward = e.getXPReward();
    player.addXP(reward);
    
    // START: NEW UI CODE
    gameMessage = "Defeated " + e.getName() + "! +" + reward + " XP";
    messageTimer = System.nanoTime(); // Reset timer
    // END: NEW UI CODE
}
    }
}

    @FXML
    public void handleKeyPress(KeyEvent event) {
        if (!gameStarted) return;
        switch (event.getCode()) {
            case W: if (currentDir != Direction.DOWN) currentDir = Direction.UP; break;
            case S: if (currentDir != Direction.UP)   currentDir = Direction.DOWN; break;
            case A: if (currentDir != Direction.RIGHT) currentDir = Direction.LEFT; break;
            case D: if (currentDir != Direction.LEFT)  currentDir = Direction.RIGHT; break;
            default: break;
        }
    }

    private void resetPlayer() {
        currentDir = Direction.NONE;
    }

private void draw() {
    GraphicsContext gc = gameCanvas.getGraphicsContext2D();
    
    // 1. Clear Screen
    gc.setFill(Color.BLACK);
    gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

    // 2. Draw the Grid (Walls, Jetwalls)
    int[][] grid = model.getGrid();
    for (int x = 0; x < 40; x++) {
        for (int y = 0; y < 40; y++) {
            if (grid[x][y] == 1) gc.setFill(Color.WHITE);                     // Wall
            else if (grid[x][y] == 2) gc.setFill(Color.web("#00FFFF", 0.5)); // Jetwall
            else if (grid[x][y] == 3) gc.setFill(Color.YELLOW);              // Speed Ramp
            else continue;
            
            gc.fillRect(x * CELL, y * CELL, CELL - 1, CELL - 1);
        }
    }

    // 3. Draw Player Head
    // We use 'player' object now, not 'selectedChar' or 'playerX' variables
    if (player != null && player.isAlive()) {
        gc.setFill(Color.web(player.getColor())); 
        gc.fillRect(player.getX() * CELL, player.getY() * CELL, CELL - 1, CELL - 1);
        
        // Optional: White center to see head direction easier
        gc.setFill(Color.WHITE);
        gc.fillRect(player.getX() * CELL + 4, player.getY() * CELL + 4, CELL - 9, CELL - 9);
    }

    // 4. Draw Enemies (NEW REQUIREMENT)
    // This loops through the list we created in initialize()
    for (Enemy e : enemies) {
        if (e.isAlive()) {
            gc.setFill(Color.web(e.getColor())); // Uses enemy specific color
            gc.fillRect(e.getX() * CELL, e.getY() * CELL, CELL - 1, CELL - 1);
        }
    }

    // 5. Handle Death / Game Over
    if (player != null && !player.isAlive()) {
        // Stop the game loop logic
        gameStarted = false;

        // Hide menus if necessary
        if (characterMenu != null) characterMenu.setVisible(false);
        if (arenaMenu != null) arenaMenu.setVisible(true);

        // Draw "DEREZZED" Text
        gc.setFill(Color.RED);
        gc.fillText("DEREZZED - SELECT AN ARENA TO TRY AGAIN", 180, 300);
    }

    // --- 6. DRAW HUD (Heads-Up Display) ---
    // Draw a semi-transparent box at the top for stats
    gc.setFill(Color.rgb(0, 0, 0, 0.7)); 
    gc.fillRect(0, 0, 600, 40); // Top bar

    gc.setFill(Color.WHITE);
    gc.setFont(new javafx.scene.text.Font("Consolas", 16));
    
    // Display Stats: Name | Level | XP | Lives
    if (player != null) {
        String stats = String.format("NAME: %-8s  LVL: %d  XP: %d  LIVES: %.1f", 
                                     player.getName(), 
                                     player.getLevel(), 
                                     player.getXP(), 
                                     player.getLives());
        gc.fillText(stats, 20, 25);
    }

    // --- 7. DRAW GAME MESSAGES ---
    // Only draw if the message is new (less than 2 seconds old)
    if (!gameMessage.isEmpty() && (System.nanoTime() - messageTimer) < 2_000_000_000L) {
        gc.setFill(Color.YELLOW);
        gc.setFont(new javafx.scene.text.Font("Verdana", 20));
        // Draw in the center of the screen
        gc.fillText(gameMessage, 180, 100);
    }

    // ... (Keep your Game Over check here) ...

}

private void loadEnemiesFromFile() {
    try {
        // This looks for enemies.txt in your project root folder
        File file = new File("enemies.txt");
        Scanner scanner = new Scanner(file);
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // Split by comma: Name,Color,Difficulty,XP,Speed,Intelligence
            String[] data = line.split(",");
            
            // Check if line has all 6 required parts
            if (data.length >= 6) {
                String name = data[0].trim();
                String colorName = data[1].trim();
                String difficulty = data[2].trim();
                int xp = Integer.parseInt(data[3].trim());
                double speed = Double.parseDouble(data[4].trim());
                String intelligence = data[5].trim();
                
                // Convert simple color names to Hex codes for JavaFX
                String hexColor = "#FFFFFF"; // Default white
                if (colorName.equalsIgnoreCase("Gold")) hexColor = "#FFD700";
                else if (colorName.equalsIgnoreCase("Red")) hexColor = "#FF0000";
                else if (colorName.equalsIgnoreCase("Yellow")) hexColor = "#FFFF00";
                else if (colorName.equalsIgnoreCase("Green")) hexColor = "#00FF00";

                // Create the Enemy Object and add to list
                // Note: We give them 3.0 lives by default, or you can add lives to txt file
                enemies.add(new Enemy(name, hexColor, 3.0, speed, difficulty, xp, intelligence));
            }
        }
        scanner.close();
        System.out.println("Loaded " + enemies.size() + " enemies from file.");
        
    } catch (FileNotFoundException e) {
        System.out.println("Error: enemies.txt not found!");
        e.printStackTrace();
    } catch (Exception e) {
        System.out.println("Error parsing enemies.txt. Check format.");
        e.printStackTrace();
    }
}
}