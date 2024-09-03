package com.example.excelnew;

import okhttp3.*;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class ShopifyAPI {
    private static final String API_KEY = "***";
    private static final String SHOP_DOMAIN = "***";
    private static final OkHttpClient client = new OkHttpClient();

    public String addProductToShopify(String title, String sku, double price, String description, int available, String collectionId, double weightInGrams) {
        // Prints to check the received values
        System.out.println("Adding product to Shopify with the following details:");
        System.out.println("Title: " + title);
        System.out.println("SKU: " + sku);
        System.out.println("Price: " + price);
        System.out.println("Available: " + available);

        MediaType mediaType = MediaType.parse("application/json");
        String json = "{\"product\": {\"title\": \"" + title + "\", \"variants\": [{\"sku\": \"" + sku + "\", \"price\": " + price + ", \"inventory_quantity\": " + available + ", \"weight\": " + weightInGrams + ", \"weight_unit\": \"g\", \"inventory_management\": \"shopify\"}], \"body_html\": \"" + description + "\", \"collections\": [{\"id\": \"" + collectionId + "\"}]}}";
        RequestBody body = RequestBody.create(mediaType, json);
        Request request = new Request.Builder()
                .url("https://" + SHOP_DOMAIN + "/admin/api/2021-04/products.json")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Shopify-Access-Token", API_KEY)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            System.out.println("Full JSON response: " + responseBody);

            if (response.isSuccessful()) {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                JsonObject product = jsonObject.getAsJsonObject("product");
                String productId = product.get("id").getAsString();

                // Print the Product ID after product creation
                System.out.println("Product ID: " + productId);

                // Add the product to the collection using the Collect API
                addProductToCollection(productId, collectionId);

                return productId;
            } else {
                System.out.println("Failed to create product. Response: " + responseBody);
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addProductToCollection(String productId, String collectionId) {
        MediaType mediaType = MediaType.parse("application/json");
        String json = "{\"collect\": {\"product_id\": \"" + productId + "\", \"collection_id\": \"" + collectionId + "\"}}";

        RequestBody body = RequestBody.create(mediaType, json);
        Request request = new Request.Builder()
                .url("https://" + SHOP_DOMAIN + "/admin/api/2021-04/collects.json")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Shopify-Access-Token", API_KEY)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            System.out.println("Full JSON response from Collect API: " + responseBody);

            if (response.isSuccessful()) {
                System.out.println("Product successfully added to the collection.");
            } else {
                System.out.println("Failed to add product to the collection. Response: " + responseBody);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProductIdByVariantId(String variantId) throws IOException {
        List<JSONObject> allProducts = getAllProducts();

        for (JSONObject product : allProducts) {
            JSONArray variants = product.getJSONArray("variants");

            for (int j = 0; j < variants.length(); j++) {
                JSONObject variant = variants.getJSONObject(j);
                String id = String.valueOf(variant.getLong("id"));
                if (id.equals(variantId)) {
                    return String.valueOf(product.getLong("id"));
                }
            }
        }

        return null;
    }

    public List<JSONObject> getAllProducts() throws IOException {
        List<JSONObject> allProducts = new ArrayList<>();
        String pageInfo = null;

        do {
            HttpUrl.Builder urlBuilder = HttpUrl.parse("https://" + SHOP_DOMAIN + "/admin/api/2021-04/products.json").newBuilder();
            urlBuilder.addQueryParameter("limit", "250"); // maximum limit per request

            if (pageInfo != null) {
                urlBuilder.addQueryParameter("page_info", pageInfo);
            }

            Request request = new Request.Builder()
                    .url(urlBuilder.build().toString())
                    .get()
                    .addHeader("X-Shopify-Access-Token", API_KEY)
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

    public String getImageUrlBySKU(String sku) {
        ShopifyInventoryUpdater updater = new ShopifyInventoryUpdater();
        try {
            String variantId = updater.getVariantIdBySKU(sku);
            if (variantId != null) {
                String productId = getProductIdByVariantId(variantId);
                if (productId != null) {
                    return getFirstImageUrl(productId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getFirstImageUrl(String productId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://" + SHOP_DOMAIN + "/admin/api/2023-07/products/" + productId + ".json")
                .get()
                .addHeader("X-Shopify-Access-Token", API_KEY)
                .build();

        Response response = client.newCall(request).execute();
        String jsonResponse = response.body().string();

        JSONObject productJson = new JSONObject(jsonResponse);
        JSONArray imagesArray = productJson.getJSONObject("product").getJSONArray("images");

        if (imagesArray.length() > 0) {
            return imagesArray.getJSONObject(0).getString("src");
        } else {
            return null;
        }
    }

}
