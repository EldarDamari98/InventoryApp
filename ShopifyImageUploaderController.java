package com.example.excelnew;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

public class ShopifyImageUploaderController {

    @FXML
    private VBox imageButtonsContainer;

    @FXML
    private TextField imageCountField;

    private String sku;

    public void setSKU(String sku) {
        this.sku = sku;
    }

    @FXML
    private void createImageButtons() {
        int numberOfImages = Integer.parseInt(imageCountField.getText());
        imageButtonsContainer.getChildren().clear();

        for (int i = 0; i < numberOfImages; i++) {
            Button imageButton = new Button("תמונה " + (i + 1));
            imageButton.setOnAction(event -> selectImageAndUpload(imageButton));
            imageButtonsContainer.getChildren().add(imageButton);
        }
    }

    private void selectImageAndUpload(Button button) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("בחר תמונה");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(button.getScene().getWindow());

        if (file != null) {
            try {
                ShopifyImageUploader uploader = new ShopifyImageUploader();
                uploader.uploadImage(sku, file);

                button.setText("תמונה הועלתה בהצלחה");
                button.setDisable(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void confirmUpload() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "התמונות הועלו למוצר " + sku);
        alert.showAndWait();
    }
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) imageButtonsContainer.getScene().getWindow();
        stage.close();
    }


}
