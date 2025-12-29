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

    @FXML
    public void initialize() {
        // 1. Setup Columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

        // 2. Load Real Data from DataManager
        System.out.println("Attempting to load leaderboard data...");
        List<PlayerScore> topPlayers = DataManager.getTopPlayers();
        
        if (topPlayers.isEmpty()) {
            System.out.println("WARNING: No players found in users.csv!");
        } else {
            System.out.println("Loaded " + topPlayers.size() + " players.");
        }

        // 3. Put data in table
        ObservableList<PlayerScore> data = FXCollections.observableArrayList(topPlayers);
        scoreTable.setItems(data);
    }

    @FXML
    private void handleBack() throws IOException {
        App.setRoot("MenuPage");
    }
}