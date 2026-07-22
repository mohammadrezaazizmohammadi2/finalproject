package com.divarclone.client.controller;

import com.divarclone.client.network.ApiClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Hyperlink registerLink;

    private final ApiClient apiClient = new ApiClient();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            errorLabel.setText("Please enter both username and password");
            return;
        }

        try {
            boolean success = apiClient.login(username, password);

            if (success) {
                goToMainScreen();
            } else {
                errorLabel.setStyle("-fx-text-fill: red;");
                errorLabel.setText("Invalid username or password");
            }
        } catch (IOException | InterruptedException e) {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Cannot connect to server");
        }
    }

    private void goToMainScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/divarclone/client/main.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();

            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Failed to load main screen");
        }
    }

    @FXML
    private void handleGoToRegister() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/divarclone/client/register.fxml"));

            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();

            stage.getScene().setRoot(root);

        } catch (IOException e) {

            errorLabel.setStyle("-fx-text-fill:red;");
            errorLabel.setText("Cannot load register page");

        }

    }
}