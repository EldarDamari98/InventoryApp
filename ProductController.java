package com.example.excelnew;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import java.util.List;
import java.util.ArrayList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.geometry.NodeOrientation;

public class ProductController {

    @FXML
    private TextField titleField, skuField, priceField, quantityField, BuyingPriceField;
    @FXML
    private TextArea descriptionField;

    @FXML
    private CheckBox uploadToExcel, uploadToShopify;
    @FXML
    private TextField goldKaratField, centerStoneField, sideStonesField, weightField;
    @FXML
    private Label goldKaratLabel, centerStoneLabel, sideStonesLabel, weightLabel;

    @FXML
    private CheckBox outletCheckBox, newArrivalCheckBox, ringsCheckBox, braceletsCheckBox, necklacesCheckBox, earringsCheckBox;
    @FXML
    private VBox collectionVBox;

    @FXML
    public void initialize() {
        descriptionField.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        goldKaratLabel.setVisible(true);
        goldKaratField.setVisible(true);
        centerStoneLabel.setVisible(true);
        centerStoneField.setVisible(true);
        sideStonesLabel.setVisible(true);
        sideStonesField.setVisible(true);
        weightLabel.setVisible(true);
        weightField.setVisible(true);
        uploadToExcel.selectedProperty().addListener((observable, oldValue, newValue) -> toggleExtraFields());
        uploadToShopify.selectedProperty().addListener((observable, oldValue, newValue) -> toggleExtraFields());
    }

    private void toggleExtraFields() {
        // Remove conditions that hide the fields
        goldKaratLabel.setVisible(true);
        goldKaratField.setVisible(true);
        centerStoneLabel.setVisible(true);
        centerStoneField.setVisible(true);
        sideStonesLabel.setVisible(true);
        sideStonesField.setVisible(true);
        weightLabel.setVisible(true);
        weightField.setVisible(true);
    }

    @FXML
    private void addProduct() {
        String title = titleField.getText();
        String sku = skuField.getText();
        double price = Double.parseDouble(priceField.getText());
        String description = descriptionField.getText().replace("\n", "<br>");
        int available = Integer.parseInt(quantityField.getText());
        double buyingPrice = Double.parseDouble(BuyingPriceField.getText());
        double weightInGrams = Double.parseDouble(weightField.getText());

        // Create a list of selected collections
        List<String> selectedCollections = new ArrayList<>();
        List<String> selectedCollectionIds = new ArrayList<>();

        if (outletCheckBox.isSelected()) {
            selectedCollections.add("OUTLET");
            selectedCollectionIds.add("283586232399");
        }
        if (newArrivalCheckBox.isSelected()) {
            selectedCollections.add("OUTLET ARRIVED");
            selectedCollectionIds.add("283650490447");
        }
        if (ringsCheckBox.isSelected()) {
            selectedCollections.add("OUTLET RINGS");
            selectedCollectionIds.add("283650424911");
        }
        if (braceletsCheckBox.isSelected()) {
            selectedCollections.add("OUTLET BRACELETS");
            selectedCollectionIds.add("283650555983");
        }
        if (necklacesCheckBox.isSelected()) {
            selectedCollections.add("OUTLET NECKLACES");
            selectedCollectionIds.add("283650523215");
        }
        if (earringsCheckBox.isSelected()) {
            selectedCollections.add("OUTLET EARRINGS");
            selectedCollectionIds.add("283650392143");
        }

        if (uploadToExcel.isSelected()) {
            String goldKarat = goldKaratField.getText();
            String centerStone = centerStoneField.getText();
            String sideStones = sideStonesField.getText();

            ExcelManager excelManager = new ExcelManager();
            String excelDescription = descriptionField.getText().replace("<br>", "\n");
            excelManager.addProductToExcel(title, sku, price, excelDescription, available, buyingPrice, goldKarat, centerStone, sideStones, weightInGrams, uploadToShopify.isSelected());

            // Add selected collections to the collections column in Excel
            excelManager.addCollectionToExcel(sku, selectedCollections);
        }

        if (uploadToShopify.isSelected()) {
            ShopifyAPI shopifyAPI = new ShopifyAPI();
            String formattedDescription = "<div style='text-align: right; direction: rtl;'>" + description + "</div>";
            String productId = shopifyAPI.addProductToShopify(title, sku, price, formattedDescription, available, null, weightInGrams);
            if (productId != null && !productId.isEmpty()) {
                // Add the product to selected collections
                for (String collectionId : selectedCollectionIds) {
                    shopifyAPI.addProductToCollection(productId, collectionId);
                }

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "מוצר " + sku + " נוסף לשופיפיי. האם תרצה להעלות תמונות?");
                alert.showAndWait().ifPresent(response -> {
                    if (response.getText().equals("OK")) {
                        openImageUploader(productId, sku);
                    }
                });
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "שגיאה בהעלאת המוצר לשופיפיי.");
                alert.showAndWait();
            }
        }
    }

    @FXML
    private Button backButton;

    @FXML
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/excelnew/inventory.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 800));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openImageUploader(String productId, String sku) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/excelnew/upload-images-view.fxml"));
            Parent root = loader.load();

            ShopifyImageUploaderController controller = loader.getController();
            controller.setSKU(productId); // Make sure to pass the Product ID here
            Stage stage = new Stage();
            stage.setTitle("העלאת תמונות למוצר " + sku);
            stage.setScene(new Scene(root, 800, 800));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
