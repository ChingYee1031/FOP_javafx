package fop.assignment;

import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Button;

public class LeaderboardPage {

    @FXML
    private TableView<PlayerScore> scoreTable;

    @FXML
    private TableColumn<PlayerScore, String> nameColumn;

    @FXML
    private TableColumn<PlayerScore, Integer> levelColumn;

    @FXML
    private TableColumn<PlayerScore, Integer> scoreColumn;

    @FXML
    private Button backButton;

    @FXML
    public void initialize() {
        // 1. Tell the columns which data to look for (Must match Getter names!)
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

        // 2. Add some dummy data to test (Later, you will read this from a CSV file)
        ObservableList<PlayerScore> data = FXCollections.observableArrayList(
            new PlayerScore("Ching Yee", 10, 5000, "2025-12-20"),
            new PlayerScore("Tron", 99, 99999, "1982-07-09"),
            new PlayerScore("Clu", 50, 2500, "2010-12-17")
        );

        // 3. Put data in table
        scoreTable.setItems(data);
    }

    @FXML
    private void handleBack() throws IOException {
        App.setRoot("MenuPage");
    }
}

//player score class
class PlayerScore {
    private String name;
    private int level;
    private int score;

    // Constructor
    public PlayerScore(String name, int level, int score, String date) {
        this.name = name;
        this.level = level;
        this.score = score;
    }

    // Getters are REQUIRED for the Table to work!
    public String getName() { return name; }
    public int getLevel() { return level; }
    public int getScore() { return score; }
}