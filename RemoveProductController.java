package com.example.excelnew;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;

public class RemoveProductController {

    @FXML
    private TextField skuField, dateSoldField, customerNameField, statusField, soldPriceField, invoiceNumberField;

    public void setSku(String sku) {
        skuField.setText(sku);
    }

    @FXML
    private Button goBackButton;

    @FXML
    private void removeProduct() {
        String sku = skuField.getText();
        String dateSold = dateSoldField.getText();
        String customerName = customerNameField.getText();
        String status = statusField.getText();
        double soldPrice = Double.parseDouble(soldPriceField.getText());
        String invoiceNumber = invoiceNumberField.getText();

        // Remove product from Excel and Shopify
        ProductRemover remover = new ProductRemover();
        remover.removeProduct(sku);

        // Update sales workbook with details
        remover.updateSalesSheet(sku, dateSold, customerName, status, soldPrice, invoiceNumber);

        // Update quantity on Shopify
        ShopifyInventoryUpdater shopifyUpdater = new ShopifyInventoryUpdater();
        try {
            shopifyUpdater.updateSellQuantity(sku);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getSku() {
        return skuField.getText();
    }

    @FXML
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/excelnew/inventory.fxml"));
            Stage stage = (Stage) goBackButton.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 800));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
