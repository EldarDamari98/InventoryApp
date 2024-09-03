package com.example.excelnew;


import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LoadingController {

    @FXML
    private Label loadingLabel;

    @FXML
    public void initialize() {
        loadingLabel.setText("טוען אנא המתן...");
    }


}
