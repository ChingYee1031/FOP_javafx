package fop.assignment;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class MenuPage {

    @FXML
    private Button startGameButton;

    @FXML
    private Button leaderboardButton;

    @FXML
    private Button exitGameButton;

    @FXML
    private void handleStartGame() throws IOException {
        // According to the assignment, you need to choose a character (Tron/Kevin)
        // So let's go to that page next.
        System.out.println("Going to Character Selection...");
        App.setRoot("CharacterSelection"); 
    }

    @FXML
    private void handleLeaderboard() throws IOException {
        // This is for the Top 10 Leaderboard feature (Assignment 3.1)
        System.out.println("Going to Leaderboard...");
        App.setRoot("LeaderboardPage");
    }

    @FXML
    private void handleExit() {
        System.out.println("Exiting Game...");
        System.exit(0);
    }
}