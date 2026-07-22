package com.divarclone.client.controller;

import com.divarclone.client.model.Ad;
import com.divarclone.client.model.Category;
import com.divarclone.client.network.ApiClient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

public class NewAdController {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField priceField;

    @FXML
    private TextField cityField;

    @FXML
    private ComboBox<Category> categoryComboBox;

    @FXML
    private Button submitButton;

    @FXML
    private Label messageLabel;

    @FXML
    private ImageView imagePreview;

    private final ApiClient apiClient = new ApiClient();

    private File selectedImageFile;

    @FXML
    public void initialize() {
        loadCategories();
    }

    private void loadCategories() {
        try {
            List<Category> categories = apiClient.getCategories();
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));

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
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Failed to load categories");
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(titleField.getScene().getWindow());

        if (file != null) {
            selectedImageFile = file;
            imagePreview.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void handleSubmit() {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        String priceText = priceField.getText();
        String city = cityField.getText();
        Category category = categoryComboBox.getValue();

        if (title.isBlank() || priceText.isBlank() || city.isBlank() || category == null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please fill in all required fields");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Price must be a valid number");
            return;
        }

        try {
            Ad createdAd = apiClient.createAd(title, description, price, city, category.getId());

            if (selectedImageFile != null) {
                try {
                    HttpResponse<String> imageResponse = apiClient.uploadAdImage(createdAd.getId(), selectedImageFile);

                    if (imageResponse.statusCode() != 200) {
                        messageLabel.setStyle("-fx-text-fill: red;");
                        messageLabel.setText("Ad created, but image upload failed: " + imageResponse.body());
                        return;
                    }

                } catch (IOException | InterruptedException e) {
                    messageLabel.setStyle("-fx-text-fill: red;");
                    messageLabel.setText("Ad created, but image upload failed: " + e.getMessage());
                    return;
                }
            }

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Ad submitted successfully! Waiting for admin approval.");

        } catch (IOException | InterruptedException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Cannot connect to server");
        }
    }

    @FXML
    private void handleCancel() {
        goToMainScreen();
    }

    private void goToMainScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/divarclone/client/main.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Failed to load main screen");
        }
    }
}