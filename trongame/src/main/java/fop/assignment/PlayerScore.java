package fop.assignment;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class PlayerScore {
    private final SimpleStringProperty name;
    private final SimpleIntegerProperty level;
    private final SimpleIntegerProperty score;
    private final SimpleStringProperty date;

    public PlayerScore(String name, int level, int score, String date) {
        this.name = new SimpleStringProperty(name);
        this.level = new SimpleIntegerProperty(level);
        this.score = new SimpleIntegerProperty(score);
        this.date = new SimpleStringProperty(date);
    }

    // JavaFX TableView needs these specific "Property" getters to update automatically
    public String getName() { return name.get(); }
    public int getLevel() { return level.get(); }
    public int getScore() { return score.get(); }
    public String getDate() { return date.get(); }
}