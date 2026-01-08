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
        // 1. Setup Columns (Must match getters in PlayerScore.java)
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        
        // --- NEW: Link Date Column ---
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        // 2. Load Real Data
        List<PlayerScore> topPlayers = DataManager.getTopPlayers();
        
        // LIMIT TO TOP 10
        if (topPlayers.size() > 10) {
            topPlayers = topPlayers.subList(0, 10);
        }

        // 3. Put data in table
        ObservableList<PlayerScore> data = FXCollections.observableArrayList(topPlayers);
        scoreTable.setItems(data);

        // --- REMOVE EMPTY SPACE LOGIC ---
        int rowHeight = 35;  
        int headerHeight = 32; 
        
        scoreTable.setFixedCellSize(rowHeight);
        
        int totalHeight = (topPlayers.size() * rowHeight) + headerHeight + 5;
        
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