package com.example.excelnew;

import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.nio.file.Files;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

public class ShopifyImageUploader extends Application {

    private static final String SHOPIFY_DOMAIN = "***";
    private static final String SHOPIFY_ACCESS_TOKEN = "***";
    private static final OkHttpClient client = new OkHttpClient(); // Here, the variable 'client' is defined

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        ShopifyImageUploader uploader = new ShopifyImageUploader();
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("Please enter the SKU: ");
            String sku = scanner.nextLine(); // Getting the SKU from the user

            System.out.print("How many images would you like to upload? ");
            int numberOfImages = scanner.nextInt(); // Getting the number of images the user wants to upload
            scanner.nextLine(); // Clearing the buffer

            uploader.uploadImagesToProduct(sku, primaryStage, numberOfImages); // Executing the logic on the SKU entered by the user
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close(); // Closing the scanner
        }
    }

    public void uploadImagesToProduct(String sku, Stage stage, int numberOfImages) throws IOException {
        // Fetch variant ID by SKU
        String productId = getProductIdBySKU(sku);

        if (productId != null) {
            // Print the product ID
            System.out.println("Product ID: " + productId);

            for (int i = 0; i < numberOfImages; i++) {
                // Let user choose an image
                List<File> images = chooseImages(stage);

                if (images != null && !images.isEmpty()) {
                    for (File image : images) {
                        uploadImage(productId, image);
                    }
                    System.out.println("Image " + (i + 1) + " uploaded successfully.");
                } else {
                    System.out.println("No image selected.");
                }
            }
        } else {
            System.out.println("Product with SKU " + sku + " not found.");
        }
    }

    private List<File> chooseImages(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            List<File> files = new ArrayList<>();
            files.add(selectedFile);
            return files;
        }
        return null;
    }

    public void uploadImage(String productId, File image) throws IOException {
        System.out.println("Uploading image for product ID: " + productId);

        // Reading the file content and converting it to Base64
        byte[] fileContent = Files.readAllBytes(image.toPath());
        String encodedString = Base64.getEncoder().encodeToString(fileContent);

        // Creating a JSON object with the Base64 encoded image
        JSONObject imageJson = new JSONObject();
        imageJson.put("image", new JSONObject().put("attachment", encodedString).put("product_id", productId));

        RequestBody body = RequestBody.create(
                MediaType.get("application/json"),
                imageJson.toString()
        );

        Request request = new Request.Builder()
                .url("https://" + SHOPIFY_DOMAIN + "/admin/api/2023-07/products/" + productId + "/images.json")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Shopify-Access-Token", SHOPIFY_ACCESS_TOKEN)
                .build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            System.out.println("Image uploaded: " + image.getName());
        } else {
            System.out.println("Failed to upload image: " + image.getName() + ". Response: " + response.body().string());
        }
    }

    private String getProductIdBySKU(String sku) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://" + SHOPIFY_DOMAIN + "/admin/api/2023-07/products.json").newBuilder();
        urlBuilder.addQueryParameter("limit", "250"); // maximum limit per request
        urlBuilder.addQueryParameter("handle", sku); // filter by SKU

        Request request = new Request.Builder()
                .url(urlBuilder.build().toString())
                .get()
                .addHeader("X-Shopify-Access-Token", SHOPIFY_ACCESS_TOKEN)
                .build();

        Response response = client.newCall(request).execute();
        String jsonResponse = response.body().string();
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray productsArray = jsonObject.getJSONArray("products");

        for (int i = 0; i < productsArray.length(); i++) {
            JSONObject product = productsArray.getJSONObject(i);
            JSONArray variants = product.getJSONArray("variants");

            for (int j = 0; j < variants.length(); j++) {
                JSONObject variant = variants.getJSONObject(j);

                if (sku.equals(variant.getString("sku"))) {
                    return String.valueOf(product.getLong("id"));
                }
            }
        }

        return null; // or another default value that fits your case when SKU is not found
    }

    private List<JSONObject> getAllProducts() throws IOException {
        List<JSONObject> allProducts = new ArrayList<>();
        String pageInfo = null;

        do {
            HttpUrl.Builder urlBuilder = HttpUrl.parse("https://" + SHOPIFY_DOMAIN + "/admin/api/2023-07/products.json").newBuilder();
            urlBuilder.addQueryParameter("limit", "250"); // maximum limit per request

            if (pageInfo != null) {
                urlBuilder.addQueryParameter("page_info", pageInfo);
            }

            Request request = new Request.Builder()
                    .url(urlBuilder.build().toString())
                    .get()
                    .addHeader("X-Shopify-Access-Token", SHOPIFY_ACCESS_TOKEN)
                    .build();

            Response response = client.newCall(request).execute();
            String jsonResponse = response.body().string();
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray productsArray = jsonObject.getJSONArray("products");

            for (int i = 0; i < productsArray.length(); i++) {
                allProducts.add(productsArray.getJSONObject(i));
            }

            // Check if there is a next page by examining the "Link" header
            String linkHeader = response.header("Link");
            pageInfo = null;
            if (linkHeader != null) {
                String[] links = linkHeader.split(",");
                for (String link : links) {
                    if (link.contains("rel=\"next\"")) {
                        pageInfo = link.substring(link.indexOf("<") + 1, link.indexOf(">"));
                        break;
                    }
                }
            }
        } while (pageInfo != null);

        return allProducts;
    }
}
