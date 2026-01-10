package fop.assignment;

import java.io.IOException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class LeaderboardPage {

    @FXML private TableView<PlayerScore> scoreTable;
    @FXML private TableColumn<PlayerScore, String> nameColumn;
    @FXML private TableColumn<PlayerScore, Integer> levelColumn;
    @FXML private TableColumn<PlayerScore, Integer> scoreColumn;
    
    // --- NEW: Inject the Date Column ---
    @FXML private TableColumn<PlayerScore, String> dateColumn; 

    @FXML
    public void initialize() {
        // 1. Setup Columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        // 2. Load Real Data
        List<PlayerScore> topPlayers = DataManager.getTopPlayers();
        
        // 3. --- NEW: FORCE 10 ROWS LOGIC ---
        
        // If we have more than 10, cut it to 10.
        if (topPlayers.size() > 10) {
            topPlayers = topPlayers.subList(0, 10);
        }

        // If we have LESS than 10, add "Empty" placeholders until we hit 10.
        while (topPlayers.size() < 10) {
            // Add a placeholder row. 
            // You can change "---" to "" if you want it completely blank.
            // Using 0 for numbers because int cannot be null.
            topPlayers.add(new PlayerScore("---", 0, 0, "---"));
        }

        // 4. Put data in table
        ObservableList<PlayerScore> data = FXCollections.observableArrayList(topPlayers);
        scoreTable.setItems(data);

        // 5. --- FIX TABLE HEIGHT FOR 10 ROWS ---
        int rowHeight = 35;  
        int headerHeight = 32; 
        
        scoreTable.setFixedCellSize(rowHeight);
        
        // Calculate height based on 10 rows EXACTLY (not topPlayers.size())
        int totalHeight = (10 * rowHeight) + headerHeight + 5;
        
        scoreTable.setPrefHeight(totalHeight);
        scoreTable.setMinHeight(totalHeight);
        scoreTable.setMaxHeight(totalHeight);
    }

    @FXML
    private void handleBack() {
        try {
            if (App.globalPlayer != null) {
                App.setRoot("MenuPage"); 
            } else {
                App.setRoot("StartPage"); 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}