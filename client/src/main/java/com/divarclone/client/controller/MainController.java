package com.divarclone.client.controller;

import com.divarclone.client.model.Ad;
import com.divarclone.client.model.Category;
import com.divarclone.client.network.ApiClient;
import com.divarclone.client.util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {

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
    private Button adminPanelButton;

    @FXML
    private Button allAdsButton;

    private final ApiClient apiClient = new ApiClient();

    private Map<Integer, String> categoryNames = new HashMap<>();

    @FXML
    public void initialize() {

        sortComboBox.setItems(FXCollections.observableArrayList(
                "Price: Low to High",
                "Price: High to Low"
        ));

        adminPanelButton.setVisible(Session.isAdmin());
        adminPanelButton.setManaged(Session.isAdmin());

        allAdsButton.setVisible(Session.isAdmin());
        allAdsButton.setManaged(Session.isAdmin());

        loadCategories();

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

            List<Ad> ads = apiClient.getAds(
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
            AdCardController controller = loader.getController();

            controller.setAd(ad, categoryNames.get(ad.getCategoryId()));

            root.setOnMouseClicked(event -> openAdDetails(ad));

            return root;

        } catch (IOException e) {
            e.printStackTrace();
            return new HBox(); // fallback خالی که برنامه کرش نکنه
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
    private void handleNewAd() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/divarclone/client/newad.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) adminPanelButton.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMyAds() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/divarclone/client/my_ads.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) adminPanelButton.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAllAds() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/divarclone/client/all_ads.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) adminPanelButton.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdminPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/divarclone/client/admin.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) adminPanelButton.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Session.clear(); // اگه متدی برای پاک کردن سشن داری، اسمش رو اینجا بذار

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/divarclone/client/login.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) adminPanelButton.getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
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
}