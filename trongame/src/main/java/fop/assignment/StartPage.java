package fop.assignment;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class StartPage {

    @FXML
    private Button startButton;

    @FXML
    void handleStartButton() throws IOException {
        App.setRoot("LoginPage");

        //print a message to the console
        System.out.println("Start Button Clicked");
    }
}
