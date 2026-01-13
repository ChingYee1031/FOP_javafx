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
    
    // Inject the Date Column 
    @FXML private TableColumn<PlayerScore, String> dateColumn; 

    @FXML
    public void initialize() {
        // Setup Columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Load Real Data
        List<PlayerScore> topPlayers = DataManager.getTopPlayers();
        
        // FORCE 10 ROWS LOGIC
        if (topPlayers.size() > 10) {
            topPlayers = topPlayers.subList(0, 10);
        }

        // If LESS than 10, add "Empty" placeholders until hit 10
        while (topPlayers.size() < 10) {
            // Using 0 for numbers because int cannot be null
            topPlayers.add(new PlayerScore("---", 0, 0, "---"));
        }

        //Put data in table
        ObservableList<PlayerScore> data = FXCollections.observableArrayList(topPlayers);
        scoreTable.setItems(data);

        //TABLE HEIGHT FOR 10 ROWS 
        int rowHeight = 35;  
        int headerHeight = 32; 
        scoreTable.setFixedCellSize(rowHeight);
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