package com.example.excelnew;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.stream.Collectors;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXMLLoader;
import java.util.Map;
import java.util.HashMap;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.geometry.NodeOrientation;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class InventoryController {

    @FXML
    private ScrollPane productContainer;

    @FXML
    private TextField searchField;

    private ExcelManager excelManager;

    private ObservableList<Product> products;

    @FXML
    private HBox buttonBox;

    @FXML
    private HBox searchBox;

    @FXML
    private Label loadingLabel;
    @FXML
    private CheckBox inStockCheckBox;

    @FXML
    private CheckBox soldOutCheckBox;
    @FXML
    private TextField quantityField;

    @FXML
    private HBox addQuantityBox;

    private String selectedSku;


    @FXML
    public void initialize() {
        searchField.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        excelManager = new ExcelManager();
        products = FXCollections.observableArrayList(excelManager.getAllProductsFromExcel());
        displayProducts(products);
    }

    public void loadProductsFromExcel() {
        products = FXCollections.observableArrayList(excelManager.getAllProductsFromExcel());
        displayProducts(products);
    }

    private void displayProducts(ObservableList<Product> productsToDisplay) {
        VBox vbox = new VBox(10); // Spacing of 10 pixels between elements
        File dir = new File("C:/Users/Eldar/Desktop/ITON/EXCEL/תמונות");

        for (Product product : productsToDisplay) {
            HBox hbox = new HBox(10); // Spacing of 10 pixels between elements
            Image image;

            // Creating the image path
            File imageFile = new File(dir, product.getSku() + ".jpg");
            if (imageFile.exists()) {
                image = new Image(imageFile.toURI().toString());
            } else {
                image = new Image("file:C:/Users/Eldar/Desktop/ITON/1.jpg"); // Default image
            }

            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(100);
            imageView.setFitWidth(100);

            VBox productDetails = new VBox(
                    new Label("SKU: " + product.getSku()),
                    new Label("Name: " + product.getName()),
                    new Label("Price: " + product.getPrice()),
                    new Label("Quantity: " + product.getQuantity()),
                    new Label("BuyingPrice: " + product.getBuyingPrice()),
                    new Label("Weight: " + product.getWeight()),
                    new Label("GoldKarat: " + product.getGoldKarat()),
                    new Label("CenterStoneCT: " + product.getCenterStoneCT()),
                    new Label("SideStonesCT: " + product.getSideStonesCT())
            );

            VBox addQuantityVBox = new VBox(5); // VBox for arranging the button, text field, and confirm button

            Button addButton = new Button("הוספת כמות");
            addQuantityVBox.getChildren().add(addButton);

            // Creating a text field for adding quantity
            TextField quantityField = new TextField();
            quantityField.setPromptText("הזן כמות להוספה");
            quantityField.setVisible(false); // Text field is hidden initially
            addQuantityVBox.getChildren().add(quantityField);

            // Creating a button to confirm quantity addition
            Button confirmButton = new Button("אשר הוספה");
            confirmButton.setVisible(false); // Confirm button is hidden initially
            addQuantityVBox.getChildren().add(confirmButton);

            addButton.setOnAction(event -> {
                selectedSku = product.getSku(); // Save the selected SKU
                quantityField.setVisible(true);
                confirmButton.setVisible(true);
            });

            // Action for confirm addition button
            confirmButton.setOnAction(confirmEvent -> {
                int quantityToAdd = Integer.parseInt(quantityField.getText());

                // Call the QuantityAdder class to add the quantity
                QuantityAdder quantityAdder = new QuantityAdder();
                quantityAdder.addQuantity(selectedSku, quantityToAdd);

                ShopifyInventoryUpdater shopifyUpdater = new ShopifyInventoryUpdater();
                try {
                    shopifyUpdater.updateAddQuantity(selectedSku, quantityToAdd);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Update product display after change
                loadProductsFromExcel();

                // Hide text field and confirm button after addition
                quantityField.setVisible(false);
                confirmButton.setVisible(false);
            });

            Button removeButton = new Button("מכירת פריט");
            removeButton.setOnAction(event -> removeProduct(product.getSku()));
            Button uploadImagesButton = new Button("העלאת תמונות");
            uploadImagesButton.setOnAction(event -> openUploadImages(product.getSku()));

            hbox.getChildren().addAll(imageView, productDetails, removeButton, addQuantityVBox, uploadImagesButton);

            // Add "Upload to Shopify" button only if the product is not uploaded to Shopify
            if ("Not Uploaded".equals(product.getShopifyStatus())) {
                Button uploadToShopifyButton = new Button("העלאה לשופייפי");
                uploadToShopifyButton.setOnAction(event -> uploadProductToShopify(product));
                hbox.getChildren().add(uploadToShopifyButton);
            }

            vbox.getChildren().add(hbox);
        }
        productContainer.setContent(vbox);
    }
    private void uploadProductToShopify(Product product) {
        ShopifyAPI shopifyAPI = new ShopifyAPI();
        String formattedDescription = "<div style='text-align: right; direction: rtl;'>" + product.getDescription() + "</div>";
        String productId = shopifyAPI.addProductToShopify(product.getName(), product.getSku(), product.getPrice(), formattedDescription, product.getQuantity(), null, product.getWeight());

        if (productId != null && !productId.isEmpty()) {
            // Get Collection IDs using the method
            List<String> collectionIds = excelManager.getCollectionIdsFromExcel(product.getSku());
            for (String collectionId : collectionIds) {
                shopifyAPI.addProductToCollection(productId, collectionId);
            }

            // Update status in Excel
            excelManager.markProductAsUploadedToShopify(product.getSku());

            // Update product display after upload
            loadProductsFromExcel();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "שגיאה בהעלאת המוצר לשופייפי.");
            alert.showAndWait();
        }
    }

    @FXML
    private void filterProductsInStock() {
        ObservableList<Product> filteredProducts = FXCollections.observableArrayList(products);

        if (inStockCheckBox.isSelected() && !soldOutCheckBox.isSelected()) {
            // Show only products in stock
            filteredProducts = filteredProducts.filtered(product -> product.getQuantity() > 0);
        } else if (!inStockCheckBox.isSelected() && soldOutCheckBox.isSelected()) {
            // Show only sold-out products (quantity 0)
            filteredProducts = filteredProducts.filtered(product -> product.getQuantity() == 0);
        }

        // Update display according to filtered products
        displayProducts(filteredProducts);
    }

    @FXML
    private void filterProducts() {
        String searchQuery = searchField.getText().trim().toLowerCase();
        if (searchQuery.isEmpty()) {
            displayProducts(products);
        } else {
            ObservableList<Product> filteredProducts = FXCollections.observableArrayList();
            for (Product product : products) {
                if (product.getSku().toLowerCase().contains(searchQuery)) {
                    filteredProducts.add(product);
                }
            }
            displayProducts(filteredProducts);
        }
    }

    @FXML
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/excelnew/hello-view.fxml"));
            Stage stage = (Stage) productContainer.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 800));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/excelnew/add-product.view.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) productContainer.getScene().getWindow();

            // Load the new content into the current scene
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openUploadImages(String sku) {
        ShopifyInventoryUpdater updater = new ShopifyInventoryUpdater();
        ShopifyAPI shopifyAPI = new ShopifyAPI(); // Create an instance of ShopifyAPI
        String variantId = null;
        String productId = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/excelnew/upload-images-view.fxml"));
            Parent root = loader.load();
            System.out.println("sku first is : " + sku);
            try {
                // Use the existing function to get the Product ID by SKU
                variantId = updater.getVariantIdBySKU(sku);
                System.out.println("the variant id is + " + variantId);
                productId = shopifyAPI.getProductIdByVariantId(variantId);
                System.out.println("the product id is " + productId);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(productId);

            // Pass the SKU to the controller
            ShopifyImageUploaderController controller = loader.getController();
            controller.setSKU(productId);

            Stage stage = new Stage();
            stage.setTitle("העלאת תמונות למוצר " + sku);
            stage.setScene(new Scene(root, 800, 800));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void downloadImages() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ShopifyAPI shopifyAPI = new ShopifyAPI();
                File dir = new File("C:/Users/Eldar/Desktop/ITON/EXCEL/תמונות");

                // Create the directory if it does not exist
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                // Get a list of SKUs from products in Excel
                List<String> excelSkus = products.stream()
                        .map(Product::getSku)
                        .collect(Collectors.toList());

                for (Product product : products) {
                    String sku = product.getSku();

                    // Check if the SKU exists in the SKU list from Excel
                    if (!excelSkus.contains(sku)) {
                        continue; // Skip products not found in Excel
                    }

                    File imageFile = new File(dir, sku + ".jpg");

                    // Check if the image already exists
                    if (!imageFile.exists()) {
                        String imageUrl = shopifyAPI.getImageUrlBySKU(sku);
                        if (imageUrl != null) {
                            try (InputStream in = new URL(imageUrl).openStream()) {
                                Files.copy(in, Paths.get(imageFile.getAbsolutePath()));
                                System.out.println("Image for SKU " + sku + " downloaded.");
                            } catch (IOException e) {
                                System.err.println("Failed to download image for SKU: " + sku);
                            }
                        }
                    } else {
                        System.out.println("Image for SKU " + sku + " already exists.");
                    }
                }
                return null;
            }

            @Override
            protected void succeeded() {
                // After image download is complete, return to inventory view and refresh products
                loadProductsFromExcel();
                switchToInventoryView();
            }

            @Override
            protected void failed() {
                // If an error occurs, display an error
                System.err.println("Failed to download images.");
                switchToInventoryView();
            }
        };

        switchToLoadingView(); // Switch to loading view
        new Thread(task).start();
    }

    public void switchToLoadingView() {
        loadingLabel.setVisible(true);
        searchBox.setVisible(false);
        productContainer.setVisible(false);
        buttonBox.setVisible(false);
    }

    public void switchToInventoryView() {
        loadingLabel.setVisible(false);
        searchBox.setVisible(true);
        productContainer.setVisible(true);
        buttonBox.setVisible(true);
    }

    @FXML
    private void confirmAddQuantity() {
        int quantityToAdd = Integer.parseInt(quantityField.getText());

        // Call the QuantityAdder class to add the quantity
        QuantityAdder quantityAdder = new QuantityAdder();
        quantityAdder.addQuantity(selectedSku, quantityToAdd);
        ShopifyInventoryUpdater shopifyUpdater = new ShopifyInventoryUpdater();
        try {
            shopifyUpdater.updateAddQuantity(selectedSku, quantityToAdd);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Update product display after change
        loadProductsFromExcel();

        // Hide the quantity box and button after addition
        addQuantityBox.setVisible(false);
    }

    private void removeProduct(String sku) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/excelnew/remove-product.view.fxml"));
            Parent root = loader.load();

            // Get the controller of remove-product.view.fxml
            RemoveProductController controller = loader.getController();

            // Pass the SKU to RemoveProductController
            controller.setSku(sku);

            // Display the new screen
            Stage stage = (Stage) productContainer.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 800));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
