package com.divarclone.client.controller;

import com.divarclone.client.model.Ad;
import com.divarclone.client.model.Category;
import com.divarclone.client.model.User;
import com.divarclone.client.network.ApiClient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class AdminPanelController {

    // ---------- تب Pending Ads ----------
    @FXML
    private TableView<Ad> pendingAdsTable;
    @FXML
    private TableColumn<Ad, String> pendingTitleColumn;
    @FXML
    private TableColumn<Ad, Double> pendingPriceColumn;
    @FXML
    private TableColumn<Ad, String> pendingCityColumn;
    @FXML
    private TableColumn<Ad, Integer> pendingOwnerColumn;
    @FXML
    private TableColumn<Ad, Void> pendingActionsColumn;

    // ---------- تب Users ----------
    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> phoneColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, Void> userActionsColumn;

    // ---------- تب Categories ----------
    @FXML
    private TextField newCategoryField;
    @FXML
    private ListView<Category> categoriesListView;

    private final ApiClient apiClient = new ApiClient();

    @FXML
    public void initialize() {
        setupPendingAdsTable();
        setupUsersTable();

        loadPendingAds();
        loadUsers();
        loadCategories();
    }

    // ==================== Pending Ads ====================

    private void setupPendingAdsTable() {
        pendingTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        pendingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        pendingCityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        pendingOwnerColumn.setCellValueFactory(new PropertyValueFactory<>("ownerId"));

        pendingActionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button approveButton = new Button("Approve");
            private final Button rejectButton = new Button("Reject");
            private final HBox container = new HBox(5, approveButton, rejectButton);

            {
                approveButton.setOnAction(event -> {
                    Ad ad = getTableView().getItems().get(getIndex());
                    handleApprove(ad);
                });
                rejectButton.setOnAction(event -> {
                    Ad ad = getTableView().getItems().get(getIndex());
                    handleReject(ad);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void loadPendingAds() {
        try {
            List<Ad> pending = apiClient.getPendingAds();
            pendingAdsTable.setItems(FXCollections.observableArrayList(pending));
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to load pending ads: " + e.getMessage());
        }
    }

    private void handleApprove(Ad ad) {
        try {
            apiClient.approveAd(ad.getId());
            loadPendingAds();
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to approve ad: " + e.getMessage());
        }
    }

    private void handleReject(Ad ad) {
        try {
            apiClient.rejectAd(ad.getId());
            loadPendingAds();
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to reject ad: " + e.getMessage());
        }
    }

    // ==================== Users ====================

    private void setupUsersTable() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        userActionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button promoteButton = new Button("Promote to Admin");
            private final Button deleteButton = new Button("Delete");
            private final HBox container = new HBox(5, promoteButton, deleteButton);

            {
                promoteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handlePromote(user);
                });
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                User user = getTableView().getItems().get(getIndex());
                promoteButton.setVisible(!"ADMIN".equals(user.getRole()));

                setGraphic(container);
            }
        });
    }

    private void loadUsers() {
        try {
            List<User> users = apiClient.getAllUsers();
            usersTable.setItems(FXCollections.observableArrayList(users));
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to load users: " + e.getMessage());
        }
    }

    private void handlePromote(User user) {
        try {
            apiClient.promoteUser(user.getId());
            loadUsers();
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to promote user: " + e.getMessage());
        }
    }

    private void handleDeleteUser(User user) {
        try {
            apiClient.deleteUser(user.getId());
            loadUsers();
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to delete user: " + e.getMessage());
        }
    }

    // ==================== Categories ====================

    private void loadCategories() {

        try {

            List<Category> categories = apiClient.getCategories();

            categoriesListView.setItems(
                    FXCollections.observableArrayList(categories));

            categoriesListView.setCellFactory(list -> new ListCell<>() {

                @Override
                protected void updateItem(Category item, boolean empty) {

                    super.updateItem(item, empty);

                    setText(empty || item == null ? null : item.getName());

                }

            });

        } catch (IOException | InterruptedException e) {

            System.out.println("Failed to load categories");

        }

    }
    @FXML
    private void handleDeleteCategory() {

        Category category =
                categoriesListView.getSelectionModel().getSelectedItem();

        if (category == null)
            return;

        try {

            apiClient.deleteCategory(category.getId());

            loadCategories();

        } catch (IOException | InterruptedException e) {

            System.out.println("Delete failed");

        }

    }

    @FXML
    private void handleAddCategory() {
        String name = newCategoryField.getText();
        if (name == null || name.isBlank()) {
            return;
        }

        try {
            apiClient.createCategory(name);
            newCategoryField.clear();
            loadCategories();
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to add category: " + e.getMessage());
        }
    }

    // ==================== Navigation ====================

    @FXML
    private void handleBackToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/divarclone/client/main.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) newCategoryField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println("Failed to load main screen: " + e.getMessage());
        }
    }
}