package com.divarclone.client.controller;

import com.divarclone.client.model.Ad;
import com.divarclone.client.model.Category;
import com.divarclone.client.network.ApiClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.divarclone.client.util.Session;
import java.util.Map;

public class MyAdsController {

    @FXML
    private ComboBox<Category> categoryComboBox;

    @FXML
    private TextField cityField;

    @FXML
    private TextField minPriceField;

    @FXML
    private TextField maxPriceField;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private FlowPane adsFlowPane;
    @FXML
    private Label myRatingLabel;

    private final ApiClient apiClient = new ApiClient();

    private Map<Integer, String> categoryNames = new HashMap<>();


    @FXML
    public void initialize() {

        sortComboBox.setItems(FXCollections.observableArrayList(
                "Price: Low to High",
                "Price: High to Low"
        ));

        loadCategories();
        loadMyRating();

        loadAds(null, null, null, null, null);
    }

    private void loadCategories() {

        try {

            List<Category> categories = apiClient.getCategories();

            categoryNames.clear();
            for (Category c : categories) {
                categoryNames.put(c.getId(), c.getName());
            }

            ObservableList<Category> items = FXCollections.observableArrayList();

            Category all = new Category();
            all.setId(-1);
            all.setName("All Categories");

            items.add(all);
            items.addAll(categories);

            categoryComboBox.setItems(items);

            categoryComboBox.getSelectionModel().selectFirst();

            categoryComboBox.setCellFactory(cb -> new ListCell<>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });

            categoryComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });

        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    private void loadAds(Integer categoryId,
                         String city,
                         Double minPrice,
                         Double maxPrice,
                         String sortBy) {

        try {

            List<Ad> ads = apiClient.getMyAds(
                    categoryId,
                    city,
                    minPrice,
                    maxPrice,
                    sortBy
            );

            adsFlowPane.getChildren().clear();

            for (Ad ad : ads) {
                adsFlowPane.getChildren().add(createAdCard(ad));
            }

        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    private Parent createAdCard(Ad ad) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/divarclone/client/ad_card.fxml")
            );

            Parent root = loader.load();
            root.setOnMouseClicked(event -> openAdDetails(ad));
            AdCardController controller = loader.getController();

            boolean canMarkSold = ad.getStatus().equals("APPROVED");

            controller.setAd(
                    ad,
                    categoryNames.get(ad.getCategoryId()),
                    true,           // showStatus
                    true,           // showDelete (توی My Ads همیشه خودتیه)
                    canMarkSold,    // showMarkSold فقط اگه APPROVED باشه
                    () -> handleDeleteAd(ad),
                    () -> handleMarkSold(ad)
            );

            return root;

        } catch (IOException e) {
            e.printStackTrace();
            return new javafx.scene.layout.HBox();
        }
    }
    private void openAdDetails(Ad ad) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/divarclone/client/ad_details.fxml")
            );

            Parent root = loader.load();

            AdDetailsController controller = loader.getController();
            controller.setAd(ad);

            Stage stage = new Stage();

            stage.setTitle(ad.getTitle());

            stage.setScene(new Scene(root));

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteAd(Ad ad) {
        try {
            apiClient.deleteAd(ad.getId());
            loadAds(null, null, null, null, null);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleMarkSold(Ad ad) {
        try {
            apiClient.markAdAsSold(ad.getId());
            loadAds(null, null, null, null, null);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {

        Category selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        Integer categoryId = (selectedCategory != null && selectedCategory.getId() != -1)
                ? selectedCategory.getId()
                : null;

        String city = cityField.getText();
        city = (city == null || city.isBlank()) ? null : city.trim();

        Double minPrice = parseOrNull(minPriceField.getText());
        Double maxPrice = parseOrNull(maxPriceField.getText());

        String sortSelected = sortComboBox.getSelectionModel().getSelectedItem();
        String sortBy = null;
        if ("Price: Low to High".equals(sortSelected)) {
            sortBy = "price_asc";
        } else if ("Price: High to Low".equals(sortSelected)) {
            sortBy = "price_desc";
        }

        loadAds(categoryId, city, minPrice, maxPrice, sortBy);
    }

    private Double parseOrNull(String text) {
        try {
            return (text == null || text.isBlank()) ? null : Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/divarclone/client/main.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) adsFlowPane.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadMyRating() {
        try {
            Map<String, Object> summary = apiClient.getRatingSummary(Session.getUserId());
            double average = ((Number) summary.get("average")).doubleValue();
            int count = ((Number) summary.get("count")).intValue();
            myRatingLabel.setText(String.format("★ %.1f (%d votes)", average, count));
        } catch (IOException | InterruptedException e) {
            myRatingLabel.setText("");
        }
    }

}