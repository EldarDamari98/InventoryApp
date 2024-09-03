package com.example.excelnew;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import java.util.List;


public class Product {

    private final StringProperty sku;
    private final StringProperty name;
    private final DoubleProperty price;
    private final DoubleProperty buyingPrice;
    private final IntegerProperty quantity;
    private final StringProperty imageFileName;
    private final DoubleProperty weight;
    private final StringProperty goldKarat;
    private final StringProperty centerStoneCT;
    private final StringProperty sideStonesCT;
    private final StringProperty description;
    private final StringProperty shopifyStatus;
    private final ListProperty<String> collections;

    public Product(String sku, String name, double price, int quantity, String imageFileName, double buyingPrice, double weight, String goldKarat, String centerStoneCT, String sideStonesCT, String description, String shopifyStatus, List<String> collections) {
        this.sku = new SimpleStringProperty(sku);
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.buyingPrice = new SimpleDoubleProperty(buyingPrice);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.imageFileName = new SimpleStringProperty(imageFileName);
        this.weight = new SimpleDoubleProperty(weight);
        this.goldKarat = new SimpleStringProperty(goldKarat);
        this.centerStoneCT = new SimpleStringProperty(centerStoneCT);
        this.sideStonesCT = new SimpleStringProperty(sideStonesCT);
        this.description = new SimpleStringProperty(description);
        this.shopifyStatus = new SimpleStringProperty(shopifyStatus);
        this.collections = new SimpleListProperty<>(FXCollections.observableArrayList(collections));
    }

    public String getCollections() {
        return String.join(", ", collections);
    }

    public ListProperty<String> collectionsProperty() {
        return collections;
    }

    public void setCollections(List<String> collections) {
        this.collections.setAll(collections);
    }

    public String getShopifyStatus() {
        return shopifyStatus.get();
    }

    public StringProperty shopifyStatusProperty() {
        return shopifyStatus;
    }

    public void setShopifyStatus(String shopifyStatus) {
        this.shopifyStatus.set(shopifyStatus);
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public String getImageFileName() {
        return imageFileName.get();
    }

    public StringProperty imageFileNameProperty() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName.set(imageFileName);
    }

    public String getSku() {
        return sku.get();
    }

    public StringProperty skuProperty() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku.set(sku);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public double getPrice() {
        return price.get();
    }

    public DoubleProperty priceProperty() {
        return price;
    }
    public void setPrice(double price) {
        this.price.set(price);
    }
    public double getBuyingPrice() {
        return buyingPrice.get();
    }

    public DoubleProperty BuyingPriceProperty() {
        return buyingPrice;
    }

    public void setBuyingPrice(double BuyingPrice) {
        this.price.set(BuyingPrice);
    }

    public int getQuantity() {
        return quantity.get();
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public double getWeight() {
        return weight.get();
    }

    public DoubleProperty weightProperty() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight.set(weight);
    }

    public String getGoldKarat() {
        return goldKarat.get();
    }

    public StringProperty goldKaratProperty() {
        return goldKarat;
    }

    public void setGoldKarat(String goldKarat) {
        this.goldKarat.set(goldKarat);
    }

    public String getCenterStoneCT() {
        return centerStoneCT.get();
    }

    public StringProperty centerStoneCTProperty() {
        return centerStoneCT;
    }

    public void setCenterStoneCT(String centerStoneCT) {
        this.centerStoneCT.set(centerStoneCT);
    }

    public String getSideStonesCT() {
        return sideStonesCT.get();
    }

    public StringProperty sideStonesCTProperty() {
        return sideStonesCT;
    }

    public void setSideStonesCT(String sideStonesCT) {
        this.sideStonesCT.set(sideStonesCT);
    }

}
