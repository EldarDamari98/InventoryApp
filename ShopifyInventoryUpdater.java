package com.example.excelnew;

import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ShopifyInventoryUpdater {

    private static final String SHOPIFY_DOMAIN = "***";
    private static final String SHOPIFY_ACCESS_TOKEN = "***";

    public void updateSellQuantity(String sku) throws IOException {
        // Fetch variant ID by SKU
        System.out.println("the sku is now: " + sku);
        String variantId = getVariantIdBySKU(sku);
        String inventoryItemId = getInventoryItemIdByVariantId(variantId);

        if (inventoryItemId != null) {
            // Print the inventory item ID
            System.out.println("Inventory Item ID: " + inventoryItemId);

            // Get current inventory quantity
            int availableQuantity = getAvailableInventoryQuantity(inventoryItemId);

            // Print the current available quantity
            System.out.println("Current available quantity: " + availableQuantity);

            if (availableQuantity > 0) {
                // Update quantity by reducing 1
                updateInventory(inventoryItemId, availableQuantity - 1);
                System.out.println("Inventory updated. New available quantity: " + (availableQuantity - 1));
            }
        } else {
            System.out.println("Product with SKU " + sku + " not found.");
        }
    }
    public void updateAddQuantity(String sku, int num) throws IOException {
        // Fetch variant ID by SKU
        System.out.println("the sku is now: " + sku);
        String variantId = getVariantIdBySKU(sku);
        String inventoryItemId = getInventoryItemIdByVariantId(variantId);

        if (inventoryItemId != null) {
            // Print the inventory item ID
            System.out.println("Inventory Item ID: " + inventoryItemId);

            // Get current inventory quantity
            int availableQuantity = getAvailableInventoryQuantity(inventoryItemId);

            // Print the current available quantity
            System.out.println("Current available quantity: " + availableQuantity);

            updateInventory(inventoryItemId, availableQuantity + num);
            System.out.println("Inventory updated. New available quantity: " + (availableQuantity + num));
        } else {
            System.out.println("Product with SKU " + sku + " not found.");
        }
    }

    private List<JSONObject> getAllProducts() throws IOException {
        OkHttpClient client = new OkHttpClient();
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

    public String getVariantIdBySKU(String sku) throws IOException {
        List<JSONObject> allProducts = getAllProducts();

        for (JSONObject product : allProducts) {
            JSONArray variants = product.getJSONArray("variants");

            for (int j = 0; j < variants.length(); j++) {
                JSONObject variant = variants.getJSONObject(j);
                if (variant.isNull("sku")) {
                    continue;
                }

                String productSku = variant.getString("sku");

                if (sku.equals(productSku)) {
                    System.out.println("Found Variant ID: " + variant.getLong("id"));
                    return String.valueOf(variant.getLong("id"));
                }
            }
        }

        return null;
    }

    public
    String getInventoryItemIdByVariantId(String variantId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://" + SHOPIFY_DOMAIN + "/admin/api/2023-07/variants/" + variantId + ".json")
                .get()
                .addHeader("X-Shopify-Access-Token", SHOPIFY_ACCESS_TOKEN)
                .build();

        Response response = client.newCall(request).execute();
        String jsonResponse = response.body().string();

        JSONObject jsonObject = new JSONObject(jsonResponse);
        if (jsonObject.has("variant")) {
            return String.valueOf(jsonObject.getJSONObject("variant").getLong("inventory_item_id"));
        } else {
            System.out.println("Key 'variant' not found in the response");
            return null;
        }
    }

    private int getAvailableInventoryQuantity(String inventoryItemId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // Find the location ID first
        String locationId = getLocationId(client);
        if (locationId == null) {
            System.out.println("Failed to find location ID.");
            return -1;
        }

        Request request = new Request.Builder()
                .url("https://" + SHOPIFY_DOMAIN + "/admin/api/2023-07/inventory_levels.json?inventory_item_ids=" + inventoryItemId + "&location_ids=" + locationId)
                .get()
                .addHeader("X-Shopify-Access-Token", SHOPIFY_ACCESS_TOKEN)
                .build();

        Response response = client.newCall(request).execute();
        String jsonResponse = response.body().string();

        // Print the full response for debugging
        System.out.println("Full response: " + jsonResponse);

        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray inventoryLevels = jsonObject.getJSONArray("inventory_levels");

        if (inventoryLevels.length() > 0) {
            return inventoryLevels.getJSONObject(0).getInt("available");
        } else {
            System.out.println("No inventory levels found for this item.");
            return -1;
        }
    }

    private String getLocationId(OkHttpClient client) throws IOException {
        String url = "https://" + SHOPIFY_DOMAIN + "/admin/api/2023-07/locations.json";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("X-Shopify-Access-Token", SHOPIFY_ACCESS_TOKEN)
                .build();

        Response response = client.newCall(request).execute();
        String jsonResponse = response.body().string();

        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray locations = jsonObject.getJSONArray("locations");

        if (locations.length() > 0) {
            // Convert the ID from Long to String
            return String.valueOf(locations.getJSONObject(0).getLong("id"));
        } else {
            return null;
        }
    }

    private void updateInventory(String inventoryItemId, int newQuantity) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // First, find the location ID
        String locationId = getLocationId(client);

        if (locationId == null) {
            System.out.println("Failed to find location ID.");
            return;
        }

        // Construct the payload for updating the inventory
        JSONObject inventoryPayload = new JSONObject();
        inventoryPayload.put("location_id", locationId);
        inventoryPayload.put("inventory_item_id", inventoryItemId);
        inventoryPayload.put("available", newQuantity);

        // Print the payload and inventoryItemId for debugging
        System.out.println("Inventory Item ID: " + inventoryItemId);
        System.out.println("Payload: " + inventoryPayload.toString());

        RequestBody body = RequestBody.create(inventoryPayload.toString(), MediaType.get("application/json"));

        // Ensure the URL is correct
        String url = "https://" + SHOPIFY_DOMAIN + "/admin/api/2023-07/inventory_levels/set.json";
        System.out.println("URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("X-Shopify-Access-Token", SHOPIFY_ACCESS_TOKEN)
                .build();

        Response response = client.newCall(request).execute();
        System.out.println("Response code: " + response.code());
        System.out.println("Response body: " + response.body().string());
    }
}
